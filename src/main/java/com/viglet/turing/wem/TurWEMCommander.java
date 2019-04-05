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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
import com.vignette.config.client.common.ConfigException;
import com.vignette.config.util.ConfigLog;
import com.vignette.logging.LoggingManager;
import com.vignette.logging.context.ContextLogger;
import com.vignette.util.VgnException;
import com.vignette.util.VgnIllegalArgumentException;

public class TurWEMCommander {
	private static ContextLogger logger = LoggingManager.getContextLogger(TurWEMCommander.class);

	private static final String WORKING_DIR = "com.vignette.workingDir";
	private static final String STFL = "STFL";
	private static final String RCRD = "RCRD";
	private IHandlerConfiguration turingConfig = null;
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

	@Parameter(names = { "--page-size",
			"-z" }, description = "The page size. After processing a page the processed count is written to an offset file."
					+ " This helps the indexer to resume from that page even after failure. ")
	private int pageSize = 500;

	@Parameter(names = "--debug", description = "Change the log level to debug", help = true)
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
			if (allObjectTypes) {

				IPagingList contentTypeIPagingList = ContentType.findAll();
				@SuppressWarnings("unchecked")
				List<Object> contentTypes = contentTypeIPagingList.asList();
				contentTypes.add(StaticFile.getTypeObjectTypeRef().getObjectType());

				System.out.println(String.format("Total number of Object Types: %d", contentTypes.size()));
				for (Object objectType : contentTypes) {
					ObjectType ot = (ObjectType) objectType;
					System.out.println(String.format("Retrieved Object Type: %s %s", ot.getData().getName(),
							ot.getContentManagementId().toString()));
					this.indexByContentType(ot);
				}
			} else if (contentType != null) {
				ObjectType objectType = ObjectType.findByName((String) contentType);
				if (objectType != null)
					this.indexByContentType(objectType);
			} else if (guidFilePath != null) {
				ArrayList<String> contentInstances = new ArrayList<String>();
				BufferedReader br = null;
				FileReader fr = null;
				try {
					fr = new FileReader(guidFilePath);
					br = new BufferedReader(fr);
					String sCurrentLine;

					while ((sCurrentLine = br.readLine()) != null) {
						if (sCurrentLine.endsWith(STFL) || sCurrentLine.endsWith(RCRD))
							contentInstances.add(sCurrentLine);

						if (contentInstances.size() != pageSize)
							continue;
						if (contentInstances.size() > 0) {
							this.indexGUIDList(contentInstances);
							contentInstances = new ArrayList<String>();
						}
					}
					if (contentInstances.size() > 0)
						this.indexGUIDList(contentInstances);

				} catch (IOException e) {
					logger.error(e);
				} finally {
					try {
						if (br != null)
							br.close();
						if (fr != null)
							fr.close();
					} catch (IOException ex) {
						logger.error(ex);
					}
				}
			}

		} catch (ConfigException exception) {
			if (logger.isDebugEnabled())
				logger.debug("Error into ConfigSpace configuration", exception);
		} catch (VgnException vgnException) {
			System.err.println("Logging does not started");
		} catch (Exception e) {
			logger.error("Viglet Turing Index Error: ", e);

		}
	}

	private void indexByContentType(ObjectType objectType)
			throws ApplicationException, ContentIndexException, ConfigException, MalformedURLException {
		int totalPages = 0;
		Iterator<?> it = null;
		int totalEntries;
		try {
			TurWEMIndexer.indexDeleteByType(objectType.getData().getName(), turingConfig);
			MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.getMappingDefinitions(turingConfig);
			RequestParameters rp = new RequestParameters();
			rp.setTopRelationOnly(false);
			IPagingList results = null;
			AsObjectType aot = AsObjectType.getInstance((ObjectTypeRef) new ObjectTypeRef((ManagedObject) objectType));
			IValidToIndex instance = mappingDefinitions.validToIndex(objectType, turingConfig);
			if (aot.isStaticFile()) {
				StaticFileWhereClause clause = new StaticFileWhereClause();
				StaticFileDBQuery query = new StaticFileDBQuery();
				if (instance != null)
					instance.whereToValid(clause, turingConfig);
				query.setWhereClause((WhereClause) clause);
				results = QueryManager.execute((Query) query, (AsObjectRequestParameters) rp);
			} else {
				ContentInstanceWhereClause clause = new ContentInstanceWhereClause();
				ContentInstanceDBQuery query = new ContentInstanceDBQuery(new ContentTypeRef(objectType.getId()));
				if (instance != null)
					instance.whereToValid(clause, turingConfig);

				query.setWhereClause((WhereClause) clause);
				results = QueryManager.execute((Query) query, (AsObjectRequestParameters) rp);
			}
			totalEntries = results.size();
			System.out.println(String.format("Number of Content Instances of type %s %s = %d",
					objectType.getData().getName(), objectType.getContentManagementId().toString(), totalEntries));
			totalPages = totalEntries > 0 ? (totalEntries + pageSize - 1) / pageSize : totalEntries / pageSize;
			it = results.pageIterator(pageSize);
		} catch (Exception e) {
			logger.error(e);
		}
		int currentPage = 1;
		if (it != null) {
			while (it.hasNext()) {
				List<?> managedObjects = (List<?>) it.next();
				System.out.println(String.format("Processing Page %d of %d pages", currentPage++, totalPages));
				long start = System.currentTimeMillis();
				try {
					HashSet<ManagedObjectVCMRef> validGuids = new HashSet<ManagedObjectVCMRef>();
					HashMap<String, ManagedObject> objectMap = new HashMap<String, ManagedObject>(
							managedObjects.size());
					for (Object object : managedObjects) {
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
					this.indexContentInstances(guids, objectMap);
				} catch (Exception e) {
					logger.error(e);
				}
				long elapsed = System.currentTimeMillis() - start;
				System.out.println(String.format("%d items processed in %dms", managedObjects.size(), elapsed));

			}
		}
	}

	private void indexGUIDList(List<String> guids)
			throws ValidationException, ApplicationException, ContentIndexException, ConfigException {
		System.out.println(String.format("Processing a total of %d GUID Strings", guids.size()));

		ArrayList<ManagedObjectVCMRef> validGuids = new ArrayList<ManagedObjectVCMRef>();
		for (String guid : guids) {
			if (guid != null && guid.length() > 0) {
				try {
					ManagedObjectVCMRef ref = new ManagedObjectVCMRef(guid);
					validGuids.add(ref);
				} catch (VgnIllegalArgumentException e) {
					logger.error(e);
				}
				continue;
			}
		}
		ManagedObjectVCMRef[] managedObjectVCMRefs = null;
		if (validGuids.size() > 0)
			managedObjectVCMRefs = validGuids.toArray(new ManagedObjectVCMRef[0]);

		if (managedObjectVCMRefs == null || managedObjectVCMRefs.length == 0)
			logger.error("No GUIDs");
		else {
			RequestParameters params = new RequestParameters();
			params.setTopRelationOnly(false);
			IPagingList managedObjects = ManagedObject.findByContentManagementIds(
					(ManagedObjectVCMRef[]) managedObjectVCMRefs, (RequestParameters) params);
			List<?> moList = managedObjects.asList();
			HashMap<String, ManagedObject> objectMap = new HashMap<String, ManagedObject>(moList.size());
			for (Object object : moList) {
				ManagedObject mo = (ManagedObject) object;
				objectMap.put(mo.getContentManagementId().getId(), mo);
			}
			System.out.println(String.format("Processing the registration of %d assets", managedObjects.size()));
			this.indexContentInstances(managedObjectVCMRefs, objectMap);
		}
	}

	private void indexContentInstances(ManagedObjectVCMRef[] refs, HashMap<String, ?> objects)
			throws ApplicationException, ConfigException, ContentIndexException {
		for (ManagedObjectVCMRef ref : refs) {
			ManagedObject mo = (ManagedObject) objects.get(ref.getId());
			if (mo != null && mo instanceof ContentInstance) {
				if (logger.isDebugEnabled())
					logger.debug(String.format("Attempting to index the Content Instance: %s",
							mo.getContentManagementId().getId()));
				TurWEMIndexer.indexCreate(mo, turingConfig, null, null);
			}
		}
	}
}
