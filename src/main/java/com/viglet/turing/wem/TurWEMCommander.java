/*
 * Copyright (C) 2016-2019 Alexandre Oliveira <alexandre.oliveira@viglet.com> 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viglet.turing.wem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.viglet.turing.wem.broker.indexer.TurWEMIndexer;
import com.viglet.turing.wem.config.GenericResourceHandlerConfiguration;
import com.viglet.turing.wem.config.IHandlerConfiguration;
import com.viglet.turing.wem.index.IValidToIndex;
import com.viglet.turing.wem.mappers.MappingDefinitions;
import com.viglet.turing.wem.mappers.MappingDefinitionsProcess;
import com.vignette.as.apps.contentIndex.ContentIndexException;
import com.vignette.as.apps.contentIndex.SearchBundleUtils;
import com.vignette.as.client.common.AsObjectRequestParameters;
import com.vignette.as.client.common.AsObjectType;
import com.vignette.as.client.common.ContentInstanceDBQuery;
import com.vignette.as.client.common.ContentInstanceWhereClause;
import com.vignette.as.client.common.Query;
import com.vignette.as.client.common.RequestParameters;
import com.vignette.as.client.common.StaticFileDBQuery;
import com.vignette.as.client.common.StaticFileWhereClause;
import com.vignette.as.client.common.WhereClause;
import com.vignette.as.client.common.ref.ContentTypeRef;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.common.ref.ObjectTypeRef;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.AuthorizationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ContentItem;
import com.vignette.as.client.javabean.ContentType;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.client.javabean.ObjectType;
import com.vignette.as.client.javabean.QueryManager;
import com.vignette.as.client.javabean.StaticFile;
import com.vignette.as.config.ConfigUtil;
import com.vignette.cms.client.common.CMSObjectBundle;
import com.vignette.cms.client.common.CMSObjectBundleFlags;
import com.vignette.config.client.common.ConfigException;
import com.vignette.config.util.ConfigLog;
import com.vignette.logging.LoggingManager;
import com.vignette.logging.context.ContextLogger;
import com.vignette.util.VgnException;
import com.vignette.util.VgnIllegalArgumentException;

public class TurWEMCommander {
	private static ContextLogger logger = LoggingManager.getContextLogger(TurWEMCommander.class);

	private static final String WORKING_DIR = "com.vignette.workingDir";

	private IHandlerConfiguration turingConfig = null;
	private CMSObjectBundleFlags staticFileBundleFlags;
	private CMSObjectBundleFlags recordBundleFlags;
	@Parameter(names = { "--host",
			"-h" }, description = "The host on which Content Management server is installed.", required = true)
	private String hostAndPort = null;

	@Parameter(names = { "--username",
			"-u" }, description = "A username to log in to the Content Management Server.", required = true)
	private String username = null;

	@Parameter(names = { "--password", "-p" }, description = "The password for the user name.", required = true)
	private String password = null;

	@Parameter(names = { "--working-dir",
			"-w" }, description = "The working directory where the vgncfg.properties file is located.", required = true)
	private String workingDir = null;

	@Parameter(names = { "--all", "-a" }, description = "Index all instances of all content types and object types.")
	private boolean allObjectTypes = false;

	@Parameter(names = { "--content-type",
			"-c" }, description = "The XML name of the content type or object type whose instances are to be indexed.")
	private String contentType = null;

	@Parameter(names = { "--guids",
			"-g" }, description = "The path to a file containing the GUID(s) of content instances or static files to be indexed.")
	private String guidFilePath = null;

	@Parameter(names = { "--chunk",
			"-z" }, description = "The page size. After processing a page the processed count is written to an offset file."
					+ " This helps the indexer to resume from that page even after failure. ")
	private int chunk = 500;

	@Parameter(names = "-debug", description = "Change the log level to debug", help = true)
	private boolean debug = false;

	@Parameter(names = "--help", description = "Print usage instructions", help = true)
	private boolean help = false;

	public static void main(String... argv) {

		TurWEMCommander main = new TurWEMCommander();
		JCommander jCommander = new JCommander();
		jCommander.addObject(main);

		try {
			jCommander.parse(argv);
			if (main.help) {
				jCommander.usage();
				return;
			}

			System.out.println("Viglet Turing WEM Indexer Tool.");

			main.run();
		} catch (ParameterException e) {
			logger.info("Error: " + e.getLocalizedMessage());
			jCommander.usage();
		}

	}

	private void run() {
		String logLevel = debug ? "DEBUG" : "INFO";

		try {
			ConfigLog.initializeLogging("turing-wem.log", logLevel);
			System.setProperty(WORKING_DIR, workingDir);
			ConfigUtil.setHasDataSource(false);
			ConfigUtil.setContainerType(ConfigUtil.CONTAINER_TYPE_SERVLET);

			turingConfig = new GenericResourceHandlerConfiguration();
			if (allObjectTypes)
				indexByObjectTypes(getAllOT());
			else if (contentType != null)
				indexByObjectType(contentType);
			else if (guidFilePath != null)
				indexGUIDsFromFile(guidFilePath);

		} catch (ConfigException exception) {
			if (logger.isDebugEnabled())
				logger.debug("Error into ConfigSpace configuration", exception);
		} catch (VgnException vgnException) {
			System.err.println("Logging does not started");
		} catch (Exception e) {
			logger.error("Viglet Turing Index Error: ", e);

		}
	}

	@SuppressWarnings("unchecked")
	private List<?> getAllOT() throws ApplicationException, ValidationException {
		IPagingList contentTypes = ContentType.findAll();
		List<Object> contentTypeList = contentTypes.asList();
		contentTypeList.add(StaticFile.getTypeObjectTypeRef().getObjectType());
		return contentTypeList;
	}

	private void indexByObjectType(String objectTypeName) throws Exception {
		ObjectType ot;
		if (null == (ot = ObjectType.findByName((String) objectTypeName))) {
			return;

		}
		this.indexByObjectType(ot);
	}

	private void indexByObjectType(ObjectType ot)
			throws ApplicationException, ContentIndexException, ConfigException, MalformedURLException {
		this.retrieveAndRegisterInstances(ot);
	}

	private boolean indexGUIDsFromFile(String filePath)
			throws ApplicationException, ValidationException, ConfigException, ContentIndexException {

		File vgnGUIDsFile = new File(filePath);
		RandomAccessFile raFile = null;
		boolean validGUIDsRead = false;
		ArrayList<String> recordGuids = new ArrayList<String>();
		ArrayList<String> staticfileGuids = new ArrayList<String>();
		long offset = this.getOffset(vgnGUIDsFile);
		int pageSize = chunk;
		try {
			String temp = "";
			raFile = new RandomAccessFile(vgnGUIDsFile, "r");
			raFile.seek(offset);
			while ((temp = raFile.readLine()) != null) {
				if (temp.length() <= 0 || "EOF".equals(temp))
					continue;
				if (temp.endsWith("STFL"))
					staticfileGuids.add(temp);
				else if (temp.endsWith("RCRD"))
					recordGuids.add(temp);

				if (recordGuids.size() + staticfileGuids.size() != pageSize)
					continue;
				if (recordGuids.size() > 0) {
					this.indexByGUIDs(recordGuids, this.getRecordBundleFlags());
					recordGuids = new ArrayList<String>();
				}
				if (staticfileGuids.size() > 0) {
					this.indexByGUIDs(staticfileGuids, this.getStaticFileBundleFlags());
					staticfileGuids = new ArrayList<String>();
				}
				this.writeOutPageOffset(vgnGUIDsFile, raFile.getFilePointer());
			}
			if (recordGuids.size() > 0)
				this.indexByGUIDs(recordGuids, this.getRecordBundleFlags());

			if (staticfileGuids.size() > 0)
				this.indexByGUIDs(staticfileGuids, this.getStaticFileBundleFlags());

			this.deleteOffsetFile(vgnGUIDsFile);
		} catch (IOException e) {
			validGUIDsRead = false;
		} finally {
			try {
				if (null != raFile)
					raFile.close();
			} catch (IOException e) {
				validGUIDsRead = false;
			}
		}
		return validGUIDsRead;
	}

	private void indexByObjectTypes(List<?> otList) throws ValidationException, ApplicationException,
			AuthorizationException, ContentIndexException, ConfigException, MalformedURLException {

		System.out.println(String.format("Total number of Object Types: %d", otList.size()));

		for (Object type : otList) {
			ObjectType ot = (ObjectType) type;
			System.out.println(String.format("Retrieved Object Type: %s %s", ot.getData().getName(),
					ot.getContentManagementId().toString()));
			this.indexByObjectType(ot);
		}
	}

	private long getOffset(File vgnGUIDsFile) {
		long offset = 0;
		File offsetFile = new File(vgnGUIDsFile.getParentFile(), vgnGUIDsFile.getName() + ".offset");
		if (offsetFile.isFile()) {
			FileInputStream fis = null;
			ObjectInputStream ois = null;
			try {
				fis = new FileInputStream(offsetFile);
				ois = new ObjectInputStream(fis);
				offset = ois.readLong();
			} catch (IOException e) {
				logger.error(e);
			} finally {
				try {
					if (fis != null)
						fis.close();
					if (ois != null)
						ois.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		return offset;
	}

	private void retrieveAndRegisterInstances(ObjectType ot)
			throws ApplicationException, ContentIndexException, ConfigException, MalformedURLException {
		int totalPages = 0;
		Iterator<?> it = null;
		int totalEntries;
		try {
			TurWEMIndexer.IndexDeleteByType(ot.getData().getName(), turingConfig);
			IPagingList results = this.retrieveInstances(ot);
			totalEntries = results.size();
			System.out.println(String.format("Number of Content Instances of type %s %s = %d", ot.getData().getName(),
					ot.getContentManagementId().toString(), totalEntries));
			int pageSize = chunk;
			totalPages = totalEntries > 0 ? (totalEntries + pageSize - 1) / pageSize : totalEntries / pageSize;
			it = results.pageIterator(pageSize);
		} catch (Exception e) {
			logger.error(e);
		}
		int currentPage = 1;
		if (it != null) {
			while (it.hasNext()) {
				List<?> moList = (List<?>) it.next();
				System.out.println(String.format("Processing Page %d of %d pages", currentPage++, totalPages));
				long start = System.currentTimeMillis();
				try {
					this.indexByManagedObjects(moList, this.getBundleFlags(ot));
				} catch (Exception e) {
					logger.error(e);
				}
				long elapsed = System.currentTimeMillis() - start;
				System.out.println(String.format("%d items processed in %dms", moList.size(), elapsed));

			}
		}
	}

	private IPagingList retrieveInstances(ObjectType ot) throws Exception {
		MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.getMappingDefinitions(turingConfig);
		RequestParameters rp = new RequestParameters();
		rp.setTopRelationOnly(false);
		IPagingList results = null;
		AsObjectType aot = AsObjectType.getInstance((ObjectTypeRef) new ObjectTypeRef((ManagedObject) ot));
		IValidToIndex instance = mappingDefinitions.validToIndex(ot, turingConfig);
		if (aot.isStaticFile()) {
			StaticFileWhereClause clause = new StaticFileWhereClause();
			StaticFileDBQuery query = new StaticFileDBQuery();
			if (instance != null) {
				instance.whereToValid(clause, turingConfig);
			}
			query.setWhereClause((WhereClause) clause);
			results = QueryManager.execute((Query) query, (AsObjectRequestParameters) rp);
		} else {
			ContentInstanceWhereClause clause = new ContentInstanceWhereClause();
			ContentInstanceDBQuery query = new ContentInstanceDBQuery(new ContentTypeRef(ot.getId()));
			if (instance != null) {
				instance.whereToValid(clause, turingConfig);
			}
			query.setWhereClause((WhereClause) clause);
			results = QueryManager.execute((Query) query, (AsObjectRequestParameters) rp);
		}
		return results;
	}

	private void indexByManagedObjects(List<?> moList, CMSObjectBundleFlags flags) throws Exception {
		HashSet<ManagedObjectVCMRef> validGuids = new HashSet<ManagedObjectVCMRef>();
		HashMap<String, ManagedObject> objectMap = new HashMap<String, ManagedObject>(moList.size());
		for (Object object : moList) {
			ManagedObject mo = (ManagedObject) object;
			if (mo instanceof ContentItem) {
				ContentItem ci = (ContentItem) mo;
				if (ci.getChannelAssociations() == null || ci.getChannelAssociations().length == 0)
					continue;

			}
			objectMap.put(mo.getContentManagementId().getId(), mo);
			validGuids.add(mo.getContentManagementId());
		}
		ManagedObjectVCMRef[] guids = null;
		if (validGuids.size() > 0)
			guids = validGuids.toArray(new ManagedObjectVCMRef[0]);

		System.out.println(String.format("Processing the registration of %d assets", validGuids.size()));
		this.registerObjects(guids, objectMap, flags);
	}

	private void indexByGUIDs(List<String> ids, CMSObjectBundleFlags flags)
			throws ValidationException, ApplicationException, ContentIndexException, ConfigException {
		System.out.println(String.format("Processing a total of %d GUID Strings", ids.size()));
		ManagedObjectVCMRef[] validGUIDs = this.getManagedObjectVCMRefsFromStringIds(ids);
		if (validGUIDs == null || validGUIDs.length == 0) {
			logger.error("No GUIDs");
			return;
		}
		RequestParameters params = new RequestParameters();
		params.setTopRelationOnly(false);
		IPagingList managedObjects = ManagedObject.findByContentManagementIds((ManagedObjectVCMRef[]) validGUIDs,
				(RequestParameters) params);
		List<?> moList = managedObjects.asList();
		HashMap<String, ManagedObject> objectMap = new HashMap<String, ManagedObject>(moList.size());
		for (Object object : moList) {
			ManagedObject mo = (ManagedObject) object;
			objectMap.put(mo.getContentManagementId().getId(), mo);
		}
		System.out.println(String.format("Processing the registration of %d assets", managedObjects.size()));
		this.registerObjects(validGUIDs, objectMap, flags);
	}

	private void registerObjects(ManagedObjectVCMRef[] refs, HashMap<String, ?> objects, CMSObjectBundleFlags flags)
			throws ApplicationException, ConfigException, ContentIndexException {
		SearchBundleUtils utils = new SearchBundleUtils();
		HashMap<?, ?> bundleMap = utils.getBundles(objects.values(), flags);
		for (ManagedObjectVCMRef ref : refs) {
			ManagedObject mo = (ManagedObject) objects.get(ref.getId());
			if (mo != null) {
				CMSObjectBundle bundle = (CMSObjectBundle) bundleMap.get(ref.getId());
				this.registerObject(mo, bundle);
				continue;
			}
		}
	}

	private void writeOutPageOffset(File vgnGUIDFile, long filePointer) {
		block14: {
			File fileDirectory = vgnGUIDFile.getParentFile();
			File offsetFile = new File(fileDirectory, vgnGUIDFile.getName() + ".offset");
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			try {
				if (offsetFile.exists() || offsetFile.createNewFile()) {
					fos = new FileOutputStream(offsetFile);
					oos = new ObjectOutputStream(fos);
					oos.writeLong(filePointer);
					break block14;
				}

			} catch (IOException e) {
				logger.error(e);
			} finally {
				try {
					if (oos != null)
						oos.close();
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

	private void deleteOffsetFile(File vgnGUIDsFile) {
		File offsetFile = new File(vgnGUIDsFile.getParentFile(), vgnGUIDsFile.getName() + ".offset");
		boolean deleted = offsetFile.delete();
		if (!deleted && offsetFile.isFile()) {
			logger.error("File was not deleted");
		}
	}

	private void registerObject(ManagedObject mo, CMSObjectBundle bundle)
			throws ApplicationException, ContentIndexException {
		String guid = "";
		boolean indexed = false;
		while (!indexed) {
			guid = mo.getContentManagementId().getId();
			logger.debug("Attempting to register object: " + guid);
			if (mo instanceof ContentInstance) {
				indexed = TurWEMIndexer.IndexCreate(mo, turingConfig, null, null);
				continue;
			}
			if (!(mo instanceof StaticFile))
				continue;
			indexed = true;
			continue;

		}
	}

	private ManagedObjectVCMRef[] getManagedObjectVCMRefsFromStringIds(List<String> ids) {
		ArrayList<ManagedObjectVCMRef> validGuids = new ArrayList<ManagedObjectVCMRef>();
		for (Object guid : ids) {
			String id = (String) guid;
			if (id != null && id.length() > 0) {
				try {
					ManagedObjectVCMRef ref = new ManagedObjectVCMRef(id);
					validGuids.add(ref);
				} catch (VgnIllegalArgumentException e) {
					logger.error(e);
				}
				continue;
			}
		}
		ManagedObjectVCMRef[] guids = null;
		if (validGuids.size() > 0)
			guids = validGuids.toArray(new ManagedObjectVCMRef[0]);
		return guids;
	}

	private CMSObjectBundleFlags getBundleFlags(ObjectType ot) throws ApplicationException, ValidationException {
		AsObjectType aot = AsObjectType.getInstance((ObjectTypeRef) new ObjectTypeRef((ManagedObject) ot));
		if (aot == null)
			return null;
		if (aot.isStaticFile())
			return this.getStaticFileBundleFlags();

		return this.getRecordBundleFlags();
	}

	private CMSObjectBundleFlags getRecordBundleFlags() {
		if (this.recordBundleFlags == null)
			this.recordBundleFlags = SearchBundleUtils.getRecordBundleFlags();
		return this.recordBundleFlags;
	}

	private CMSObjectBundleFlags getStaticFileBundleFlags() {
		if (this.staticFileBundleFlags == null)
			this.staticFileBundleFlags = SearchBundleUtils.getStaticFileBundleFlags();
		return this.staticFileBundleFlags;
	}
}
