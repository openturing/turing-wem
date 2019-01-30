
package com.viglet.turing.apps.contentIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.viglet.turing.broker.TurWEM;
import com.viglet.turing.broker.indexer.TurWEMIndex;
import com.viglet.turing.broker.indexer.TurWEMIndexer;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.index.IExternalResource;
import com.viglet.turing.index.IValidToIndex;
import com.vignette.as.apps.contentIndex.ContentIndexException;
import com.vignette.as.apps.contentIndex.ContentIndexMsg;
import com.vignette.as.apps.contentIndex.ContentIndexerMsg;
import com.vignette.as.apps.contentIndex.SearchBundleUtils;
import com.vignette.as.client.common.AsObjectRequestParameters;
import com.vignette.as.client.common.AsObjectType;
import com.vignette.as.client.common.ContentInstanceDBQuery;
import com.vignette.as.client.common.ContentInstanceWhereClause;
import com.vignette.as.client.common.Query;
import com.vignette.as.client.common.RequestParameters;
import com.vignette.as.client.common.SearchResult;
import com.vignette.as.client.common.StaticFileDBQuery;
import com.vignette.as.client.common.StaticFileWhereClause;
import com.vignette.as.client.common.WhereClause;
import com.vignette.as.client.common.ref.ContentTypeRef;
import com.vignette.as.client.common.ref.IManagedObjectRef;
import com.vignette.as.client.common.ref.ManagedObjectRef;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.common.ref.ObjectTypeRef;
import com.vignette.as.client.common.types.SearchTypeEnum;
import com.vignette.as.client.exception.ASErrorCode;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.AuthorizationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.AsLocale;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ContentItem;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.client.javabean.ObjectType;
import com.vignette.as.client.javabean.QueryManager;
import com.vignette.as.client.javabean.SearchQuery;
import com.vignette.as.client.javabean.StaticFile;
import com.vignette.as.common.pluggable.PluggableInterfaceFactory;
import com.vignette.as.config.ConfigUtil;
import com.vignette.as.server.pluggable.search.ISearchIndex;
import com.vignette.as.server.pluggable.search.opentext.OpenTextFactory;
import com.vignette.cms.client.common.CMSObjectBundle;
import com.vignette.cms.client.common.CMSObjectBundleFlags;
import com.vignette.cms.client.common.ObjectId;
import com.vignette.cms.client.common.RecordBundle;
import com.vignette.cms.client.common.StaticFileBundle;
import com.vignette.config.client.common.ConfigException;
import com.vignette.logging.LoggingManager;
import com.vignette.logging.context.ContextLogger;
import com.vignette.util.FileIOUtil;
import com.vignette.util.IVgnErrorCode;
import com.vignette.util.MsgObject;
import com.vignette.util.ObjectQueryOp;
import com.vignette.util.StringQueryOp;
import com.vignette.util.VgnIllegalArgumentException;

public class ContentIndexer {
	private static ContextLogger logger = LoggingManager.getContextLogger((Class) ContentIndexer.class);
	private static final int MAX_INDEXING_TRIES = 2;
	public static final String EOF = "EOF";
	private boolean surveyMode = false;
	private String surveyFileName;
	private String dateFormat = "MM/dd/yyyy:hh:mm:ss";
	private SimpleDateFormat formatter = new SimpleDateFormat(this.dateFormat);
	private CMSObjectBundleFlags staticFileBundleFlags;
	private CMSObjectBundleFlags recordBundleFlags;
	private int mPageSize = ContentIndexer.getBulkActionChunkSize();
	private PrintStream output = System.out;
	private ISearchIndex registrar = null;
	private boolean mSearchEngineConnection = false;
	private static final String TAB = "    ";
	private boolean forceReIndex = false;

	IHandlerConfiguration turingConfig;

	public boolean isForceReIndex() {
		return this.forceReIndex;
	}

	public void setTuringConfig(IHandlerConfiguration turingConfig) {
		this.turingConfig = turingConfig;
	}

	public void setForceReIndex(boolean forceReIndex) {
		this.forceReIndex = forceReIndex;
	}

	public void indexByObjectType(String objectTypeName, String outputFileName) throws Exception {
		ObjectType ot;
		if (null != outputFileName) {
			this.surveyFileName = outputFileName;
			this.surveyMode = true;
		}
		if (null == (ot = ObjectType.findByName((String) objectTypeName))) {
			if (!TurWEM.isExternalResource(objectTypeName, turingConfig)) {
				MsgObject mo = ContentIndexerMsg.getMsgObject("4", (Object) objectTypeName);
				this.consoleOut(mo);
				logger.error((Object) mo);
				return;
			}
			this.consoleOut(" ");
			this.consoleOut(ContentIndexerMsg.getMsgObject("20", (Object) (objectTypeName + "    ")));
			this.indexByObjectType(objectTypeName);
			return;
		}
		this.consoleOut(" ");
		this.consoleOut(ContentIndexerMsg.getMsgObject("20",
				(Object) (objectTypeName + "    " + ot.getContentManagementId().toString())));
		this.indexByObjectType(ot, this.surveyMode);
	}

	public void indexByObjectType(String objectTypeName, String[] vcmids) throws Exception {
		ObjectType ot;
		if (null == (ot = ObjectType.findByName((String) objectTypeName))) {
			if (!TurWEM.isExternalResource(objectTypeName, turingConfig)) {
				MsgObject mo = ContentIndexerMsg.getMsgObject("4", (Object) objectTypeName);
				this.consoleOut(mo);
				logger.error((Object) mo);
				return;
			}
			this.consoleOut(" ");
			this.consoleOut(ContentIndexerMsg.getMsgObject("20", (Object) (objectTypeName + "    ")));
			this.retrieveAndRegisterInstances(objectTypeName, vcmids);
			return;
		}
		this.consoleOut(" ");
		this.consoleOut(ContentIndexerMsg.getMsgObject("20",
				(Object) (objectTypeName + "    " + ot.getContentManagementId().toString())));
		this.retrieveAndRegisterInstances(ot, null, vcmids);
	}

	public void indexByObjectTypes(List otList, String outputFileName, String locale)
			throws ValidationException, ApplicationException, AuthorizationException, ContentIndexException,
			ConfigException, MalformedURLException {
		if (null != outputFileName) {
			this.surveyFileName = outputFileName;
			this.surveyMode = true;
		}
		if (this.isForceReIndex() && locale != null && this.verifySearchEngineConnection()) {
			this.getRegistrar().deleteItemFromAllIndexes(locale);
		}
		MsgObject mo = ContentIndexerMsg.getMsgObject("14", (Object) Integer.toString(otList.size()));
		this.consoleOut(mo);
		for (Object type : otList) {
			ObjectType ot = (ObjectType) type;
			this.consoleOut("");
			this.consoleOut(ContentIndexerMsg.getMsgObject("20",
					(Object) (ot.getData().getName() + "    " + ot.getContentManagementId().toString())));
			this.indexByObjectType(ot, outputFileName, locale);
		}
	}

	public void indexStaticFiles(String path, boolean recurse, String filename)
			throws ApplicationException, ValidationException, ConfigException, ContentIndexException {
		if (filename != null) {
			this.surveyFileName = filename;
			this.surveyMode = true;
		}
		StaticFileDBQuery sfq = new StaticFileDBQuery();
		RequestParameters rp = new RequestParameters();
		if (!this.surveyMode) {
			rp.setSystemDataReturnMode(RequestParameters.DataReturnMode.FULL);
			rp.setUserDataReturnMode(RequestParameters.DataReturnMode.IDENTITY);
		} else {
			rp.setSystemDataReturnMode(RequestParameters.DataReturnMode.IDENTITY);
			rp.setUserDataReturnMode(RequestParameters.DataReturnMode.IDENTITY);
		}
		StaticFileWhereClause sfClause = new StaticFileWhereClause();
		if (ConfigUtil.inMgmtCDS() || ConfigUtil.isMgmtDA()) {
			sfClause.checkParentProjectPath(recurse ? StringQueryOp.STARTS_WITH : StringQueryOp.EQUAL, path);
		} else {
			sfClause.checkPlacementPath(StringQueryOp.STARTS_WITH, path);
		}
		sfq.setWhereClause((WhereClause) sfClause);
		IPagingList results = QueryManager.execute((Query) sfq, (AsObjectRequestParameters) rp);
		this.registerPagingList(results, this.getStaticFileBundleFlags());
		if (this.surveyMode) {
			this.writeEOF();
		}
	}

	public void indexByObjectType(ObjectType ot, boolean isSurveyMode)
			throws ApplicationException, ContentIndexException, ConfigException, MalformedURLException {
		this.surveyMode = isSurveyMode;
		this.retrieveAndRegisterInstances(ot, null);
	}

	private void indexByObjectType(String objectTypeName) throws Exception {
		this.retrieveAndRegisterInstances(objectTypeName, null);
	}

	public void indexByObjectType(ObjectType ot, String guidFileName, String locale) throws ValidationException,
			ApplicationException, ContentIndexException, ConfigException, MalformedURLException {
		if (guidFileName != null) {
			this.surveyMode = true;
			this.surveyFileName = guidFileName;
		}
		this.retrieveAndRegisterInstances(ot, locale);
	}

	public void indexByGUID(String guid) throws ValidationException, AuthorizationException, ApplicationException,
			ContentIndexException, ConfigException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(guid);
		ObjectId oid = new ObjectId(guid);
		if (oid.isStaticFileId()) {
			this.indexByGUIDs(list, this.getStaticFileBundleFlags());
		} else if (this.isContentInstance(oid)) {
			this.indexByGUIDs(list, this.getRecordBundleFlags());
		}
	}

	private boolean isContentInstance(ObjectId oid)
			throws ApplicationException, AuthorizationException, ValidationException {
		ManagedObjectRef mor = ManagedObject.asManagedObjectRef((ManagedObjectVCMRef) new ManagedObjectVCMRef(oid));
		ObjectTypeRef otRef = mor == null ? null : mor.getObjectTypeRef();
		return otRef != null && AsObjectType.getInstance((String) otRef.getId()).isContentInstance();
	}

	protected void indexByGUIDs(List ids, CMSObjectBundleFlags flags)
			throws ValidationException, ApplicationException, ContentIndexException, ConfigException {
		this.consoleOut("Processing a total of " + ids.size() + " GUID Strings");
		ManagedObjectVCMRef[] validGUIDs = this.getManagedObjectVCMRefsFromStringIds(ids);
		if (null == validGUIDs || validGUIDs.length == 0) {
			MsgObject mo = ContentIndexerMsg.getMsgObject("16");
			this.consoleOut(mo);
			logger.error((Object) mo);
			return;
		}
		RequestParameters params = new RequestParameters();
		params.setTopRelationOnly(false);
		IPagingList managedObjects = ManagedObject.findByContentManagementIds((ManagedObjectVCMRef[]) validGUIDs,
				(RequestParameters) params);
		List moList = managedObjects.asList();
		HashMap<String, ManagedObject> objectMap = new HashMap<String, ManagedObject>(moList.size());
		for (Object object : moList) {
			ManagedObject mo = (ManagedObject) object;
			objectMap.put(mo.getContentManagementId().getId(), mo);
		}
		this.consoleOut("Processing the registration of " + managedObjects.size() + " assets");
		this.registerObjects(validGUIDs, objectMap, flags);
	}

	protected void indexByManagedObjects(List moList, CMSObjectBundleFlags flags) throws Exception {
		HashSet<ManagedObjectVCMRef> validGuids = new HashSet<ManagedObjectVCMRef>();
		HashMap<String, ManagedObject> objectMap = new HashMap<String, ManagedObject>(moList.size());
		for (Object object : moList) {
			ManagedObject mo = (ManagedObject) object;
			if (mo instanceof ContentItem) {
				ContentItem ci = (ContentItem) mo;
				if (ci.getChannelAssociations() == null || ci.getChannelAssociations().length == 0) {
					continue;
				}
			}
			objectMap.put(mo.getContentManagementId().getId(), mo);
			validGuids.add(mo.getContentManagementId());
		}
		ManagedObjectVCMRef[] guids = null;
		if (validGuids.size() > 0) {
			guids = validGuids.toArray(new ManagedObjectVCMRef[0]);
		}
		this.consoleOut("Processing the registration of " + validGuids.size() + " assets");
		this.registerObjects(guids, objectMap, flags);
	}

	private void indexByExternalResource(List<ExternalResourceObject> list, String typeName) throws Exception {
		String className = TurWEMIndex.getClassValidToIndex(typeName, turingConfig);
		IValidToIndex instance = null;
		if (className != null) {
			Class<?> clazz = Class.forName(className);
			if (clazz == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Valid to Index className is not found in the jar file: " + className);
				}
				return;
			} else {
				instance = (IValidToIndex) clazz.newInstance();
			}
		}
		for (ExternalResourceObject item : list) {
			registerObject(item, typeName);
		}
	}

	public void replaceByGUIDs(List ids, String fieldName, String fieldValue)
			throws ApplicationException, ValidationException, ConfigException {
		this.consoleOut("Processing a total of " + ids.size() + " GUID Strings");
		ISearchIndex registrar = this.getRegistrar();
		ArrayList itemsToBeReplaced = new ArrayList();
		for (Object id : ids) {
			String guid = (String) id;
			HashMap<String, String> attributes = new HashMap<String, String>();
			attributes.put("vcmSys-identifier", guid);
			attributes.put(fieldName, fieldValue);
			itemsToBeReplaced.add(attributes);
		}
		registrar.replaceItems(itemsToBeReplaced);
		this.consoleOut("Processed " + ids.size() + " GUID Strings");
	}

	protected void indexReset() throws ApplicationException, ValidationException, ConfigException {
		if (PluggableInterfaceFactory.getSearchFactory() instanceof OpenTextFactory) {
			SearchQuery query = new SearchQuery();
			query.getData().setQueryText("*");
			query.getData().setSearchType(SearchTypeEnum.CONTENT_PLUS_STATICFILE);
			query.getData().setLoadFromSearchEngine(true);
			IPagingList results = query.execute();
			if (null == results) {
				return;
			}
			Date start = new Date();
			int totalResults = results.size();
			if (totalResults > 0) {
				this.consoleOut("Removing a total of " + totalResults + " indexed items.");
				int pageSize = 50;
				int totalPages = totalResults / pageSize + (totalResults % pageSize > 0 ? 1 : 0);
				ISearchIndex registrar = this.getRegistrar();
				for (int chunkPage = 0; chunkPage < totalPages; ++chunkPage) {
					long pageStartTime = new Date().getTime();
					int chunkStart = chunkPage * pageSize;
					int chunkEnd = chunkStart + pageSize - 1;
					List list = results.subList(chunkStart, chunkEnd);
					this.consoleOut(ContentIndexerMsg.getMsgObject("22", (Object) Integer.toString(chunkPage + 1)));
					this.consoleOut(ContentIndexerMsg.getMsgObject("23", (Object) Integer.toString(chunkStart + 1),
							(Object) Integer.toString(chunkStart + list.size())));
					ArrayList<String> guidList = new ArrayList<String>();
					for (Object aList : list) {
						IManagedObjectRef ref = ((SearchResult) aList).getManagedObjectRef();
						String id = ((ManagedObjectVCMRef) ref).getId();
						guidList.add(id);
					}
					registrar.removeItems(guidList);
					long pageEndTime = new Date().getTime();
					long pageDuration = pageEndTime - pageStartTime;
					this.consoleOut(ContentIndexerMsg.getMsgObject("24", (Object) Long.toString(pageDuration)));
				}
			}
			this.printTimingReport(start, totalResults);
		} else {
			ISearchIndex registrar = this.getRegistrar();
			registrar.removeItems(null);
		}
	}

	public void setPageSize(int pageSize) {
		if (pageSize > 0) {
			this.consoleOut(ContentIndexerMsg.getMsgObject("39", (Object) Integer.toString(pageSize)));
			this.mPageSize = pageSize;
		}
	}

	private int getPageSize() {
		if (this.mPageSize > 5000) {
			return 5000;
		}
		return this.mPageSize;
	}

	private boolean verifySearchEngineConnection()
			throws ApplicationException, AuthorizationException, ValidationException {
		if (!this.mSearchEngineConnection) {
			for (int i = 0; i < 2; ++i) {
				this.mSearchEngineConnection = PluggableInterfaceFactory.getSearchAdmin().isIndexReady();
				if (this.mSearchEngineConnection) {
					return this.mSearchEngineConnection;
				}
				try {
					this.consoleOut(ContentIndexMsg.getMsgObject("46"));
					Thread.sleep(5000);
					continue;
				} catch (InterruptedException e) {
					MsgObject mo = ContentIndexMsg.getMsgObject("16", (Object) e.getMessage());
					logger.error((Object) mo, (Throwable) e);
				}
			}
		}
		return this.mSearchEngineConnection;
	}

	private ManagedObjectVCMRef[] getManagedObjectVCMRefsFromStringIds(List ids) {
		ArrayList<ManagedObjectVCMRef> validGuids = new ArrayList<ManagedObjectVCMRef>();
		for (Object guid : ids) {
			String id = (String) guid;
			if (null != id && id.length() > 0) {
				try {
					ManagedObjectVCMRef ref = new ManagedObjectVCMRef(id);
					validGuids.add(ref);
				} catch (VgnIllegalArgumentException e) {
					MsgObject mo = ContentIndexerMsg.getMsgObject("15", (Object) id);
					this.consoleOut(mo);
					logger.error((Object) mo, (Throwable) e);
				}
				continue;
			}
			MsgObject mo = ContentIndexerMsg.getMsgObject("15", (Object) id);
			this.consoleOut(mo);
			logger.error((Object) mo);
		}
		ManagedObjectVCMRef[] guids = null;
		if (validGuids.size() > 0) {
			guids = validGuids.toArray(new ManagedObjectVCMRef[0]);
		}
		return guids;
	}

	private void retrieveAndRegisterInstances(ObjectType ot, String locale, String[] vcmids)
			throws ApplicationException, ContentIndexException, ConfigException, ValidationException,
			AuthorizationException, MalformedURLException {
		if (vcmids == null) {
			this.retrieveAndRegisterInstances(ot, locale);
		} else {
			this.indexDeleteByIds(ot.getData().getName(), vcmids);
			for (String vcmid : vcmids) {
				this.indexByGUID(vcmid);
			}
		}
	}

	private void retrieveAndRegisterInstances(ObjectType ot, String locale)
			throws ApplicationException, ContentIndexException, ConfigException, MalformedURLException {
		int totalPages;
		Iterator it;
		int totalEntries;
		// TODO: Create register Version for Viglet Turing
		// if (!this.surveyMode) {
		// this.registerObjectType(ot);
		// }
		try {
			indexResetByType(ot.getData().getName());
			IPagingList results = this.retrieveInstances(ot, locale);
			totalEntries = results.size();
			MsgObject mo = ContentIndexerMsg.getMsgObject("21",
					(Object) (ot.getData().getName() + "    " + ot.getContentManagementId().toString()),
					(Object) Integer.toString(totalEntries));
			this.consoleOut(mo);
			if (totalEntries == 0) {
				logger.info((Object) mo);
			}
			int pageSize = this.getPageSize();
			totalPages = totalEntries > 0 ? (totalEntries + pageSize - 1) / pageSize : totalEntries / pageSize;
			it = results.pageIterator(pageSize);
		} catch (Exception e) {
			throw new ContentIndexException(ContentIndexerMsg.getMsgObject("29", (Object) ot.getData().getName(),
					(Object) e.getLocalizedMessage()));
		}
		int currentPage = 1;
		while (it.hasNext()) {
			List moList = (List) it.next();
			if (this.surveyMode) {
				this.writeSurveyEntries(moList);
				continue;
			}
			this.consoleOut(ContentIndexerMsg.getMsgObject("38", (Object) Integer.toString(currentPage++),
					(Object) Integer.toString(totalPages)));
			long start = System.currentTimeMillis();
			try {
				this.indexByManagedObjects(moList, this.getBundleFlags(ot));
			} catch (Exception e) {
			}
			long elapsed = System.currentTimeMillis() - start;
			this.consoleOut(ContentIndexerMsg.getMsgObject("37", (Object) Integer.toString(moList.size()),
					(Object) Long.toString(elapsed)));
		}
		if (this.surveyMode) {
			this.writeEOF();
			if (totalEntries > 0) {
				this.consoleOut(ContentIndexerMsg.getMsgObject("36", (Object) Integer.toString(totalEntries),
						(Object) this.getSurveyFileName()));
			} else {
				this.consoleOut(ContentIndexerMsg.getMsgObject("35"));
			}
		}
	}

	private void retrieveAndRegisterInstances(String objectTypeName, String[] vcmids) throws Exception {
		int totalPages;
		int totalEntries;
		List<ExternalResourceObject> results = null;
		String className = TurWEM.getCustomClassName(objectTypeName, turingConfig);
		if (className == null) {
			return;
		}
		Class<?> clazz = Class.forName(className);
		if (clazz == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Custom className is not found in the jar file: " + className);
			}
			return;
		}
		IExternalResource instance = (IExternalResource) clazz.newInstance();
		try {
			if (vcmids == null) {
				indexResetByType(objectTypeName);
				results = instance.listExternalResource(turingConfig);
			} else {
				this.indexDeleteByIds(objectTypeName, vcmids);
				results = new ArrayList<ExternalResourceObject>();
				for (String guid : vcmids) {
					ExternalResourceObject item = instance.getExternalResource(guid, turingConfig);
					if (item != null) {
						results.add(item);
					}
				}
			}
			totalEntries = results.size();
			MsgObject mo = ContentIndexerMsg.getMsgObject("21", (Object) (objectTypeName),
					(Object) Integer.toString(totalEntries));
			this.consoleOut(mo);
			if (totalEntries == 0) {
				logger.info((Object) mo);
			}
			int pageSize = this.getPageSize();
			totalPages = totalEntries > 0 ? (totalEntries + pageSize - 1) / pageSize : totalEntries / pageSize;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ContentIndexException(
					ContentIndexerMsg.getMsgObject("29", (Object) objectTypeName, (Object) e.getLocalizedMessage()));
		}
		int currentPage = 1;
		if (results.size() > 0) {
			int toIndex = results.size() > this.getPageSize() ? this.getPageSize() : results.size();
			List<ExternalResourceObject> pageList = results.subList(0, toIndex);
			while (currentPage <= totalPages) {
				this.consoleOut(ContentIndexerMsg.getMsgObject("38", (Object) Integer.toString(currentPage++),
						(Object) Integer.toString(totalPages)));
				long start = System.currentTimeMillis();
				this.indexByExternalResource(pageList, objectTypeName);
				long elapsed = System.currentTimeMillis() - start;
				this.consoleOut(ContentIndexerMsg.getMsgObject("37", (Object) Integer.toString(pageList.size()),
						(Object) Long.toString(elapsed)));
				toIndex += this.getPageSize();
				toIndex = results.size() > toIndex ? toIndex : results.size();
				pageList = results.subList(0, toIndex);
			}
		}
	}

	private IPagingList retrieveInstances(ObjectType ot, String locale) throws Exception {
		RequestParameters rp = new RequestParameters();
		rp.setTopRelationOnly(false);
		IPagingList results = null;
		AsObjectType aot = AsObjectType.getInstance((ObjectTypeRef) new ObjectTypeRef((ManagedObject) ot));
		String className = TurWEMIndex.getClassValidToIndex(ot.getData().getName(), turingConfig);
		IValidToIndex instance = null;
		if (className != null) {
			Class<?> clazz = Class.forName(className);
			if (clazz == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Valid to Index className is not found in the jar file: " + className);
				}
			} else {
				instance = (IValidToIndex) clazz.newInstance();
			}
		}
		if (aot.isStaticFile()) {
			StaticFileWhereClause clause = new StaticFileWhereClause();
			if (locale != null) {
				AsLocale asLocale = AsLocale.findByLocaleCode((String) locale, (RequestParameters) null);
				clause.checkLocale(ObjectQueryOp.EQUAL, asLocale.getManagedObjectRef());
			}
			StaticFileDBQuery query = new StaticFileDBQuery();
			if (instance != null) {
				instance.whereToValid(clause, turingConfig);
			}
			query.setWhereClause((WhereClause) clause);
			results = QueryManager.execute((Query) query, (AsObjectRequestParameters) rp);
		} else {
			ContentInstanceWhereClause clause = new ContentInstanceWhereClause();
			if (locale != null) {
				AsLocale asLocale = AsLocale.findByLocaleCode((String) locale, (RequestParameters) null);
				clause.checkLocale(ObjectQueryOp.EQUAL, asLocale.getManagedObjectRef());
			}
			ContentInstanceDBQuery query = new ContentInstanceDBQuery(new ContentTypeRef(ot.getId()));
			if (instance != null) {
				instance.whereToValid(clause, turingConfig);
			}
			query.setWhereClause((WhereClause) clause);
			results = QueryManager.execute((Query) query, (AsObjectRequestParameters) rp);
		}
		return results;
	}

	private void registerPagingList(IPagingList results, CMSObjectBundleFlags flags)
			throws ApplicationException, ConfigException, ContentIndexException {
		int pageSize = 0;
		int totalResults = 0;
		if (null == results) {
			return;
		}
		Date start = new Date();
		int totalPages = totalResults / pageSize
				+ ((totalResults = results.size()) % (pageSize = this.getPageSize()) > 0 ? 1 : 0);
		for (int chunkPage = 0; chunkPage < totalPages; ++chunkPage) {
			long pageStartTime = new Date().getTime();
			int chunkStart = chunkPage * pageSize;
			int chunkEnd = chunkStart + pageSize - 1;
			List list = results.subList(chunkStart, chunkEnd);
			this.consoleOut(ContentIndexerMsg.getMsgObject("22", (Object) Integer.toString(chunkPage + 1)));
			this.consoleOut(ContentIndexerMsg.getMsgObject("23", (Object) Integer.toString(chunkStart + 1),
					(Object) Integer.toString(chunkStart + list.size())));
			if (this.surveyMode) {
				this.writeSurveyEntries(list);
			} else {
				this.registerObjects(list, flags);
			}
			long pageEndTime = new Date().getTime();
			long pageDuration = pageEndTime - pageStartTime;
			this.consoleOut(ContentIndexerMsg.getMsgObject("24", (Object) Long.toString(pageDuration)));
		}
		this.printTimingReport(start, totalResults);
	}

	protected void printTimingReport(Date start, int totalResults) {
		long startTime = start.getTime();
		Date end = new Date();
		long endTime = end.getTime();
		long duration = endTime - startTime;
		this.consoleOut(ContentIndexerMsg.getMsgObject("25", (Object) this.formatter.format(start)));
		this.consoleOut(ContentIndexerMsg.getMsgObject("26", (Object) this.formatter.format(end)));
		this.consoleOut(ContentIndexerMsg.getMsgObject("24", (Object) Long.toString(duration)));
		this.consoleOut(ContentIndexerMsg.getMsgObject("19", (Object) Integer.toString(totalResults)));
	}

	private CMSObjectBundleFlags getBundleFlags(ObjectType ot) throws ApplicationException, ValidationException {
		AsObjectType aot = AsObjectType.getInstance((ObjectTypeRef) new ObjectTypeRef((ManagedObject) ot));
		if (aot == null) {
			return null;
		}
		if (aot.isStaticFile()) {
			return this.getStaticFileBundleFlags();
		}
		return this.getRecordBundleFlags();
	}

	private CMSObjectBundleFlags getRecordBundleFlags() {
		if (null == this.recordBundleFlags) {
			this.recordBundleFlags = SearchBundleUtils.getRecordBundleFlags();
		}
		return this.recordBundleFlags;
	}

	private CMSObjectBundleFlags getStaticFileBundleFlags() {
		if (null == this.staticFileBundleFlags) {
			this.staticFileBundleFlags = SearchBundleUtils.getStaticFileBundleFlags();
		}
		return this.staticFileBundleFlags;
	}

	private void registerObjects(ManagedObjectVCMRef[] refs, HashMap objects, CMSObjectBundleFlags flags)
			throws ApplicationException, ConfigException, ContentIndexException {
		HashMap bundleMap = this.getBundleMap(objects.values(), flags);
		for (ManagedObjectVCMRef ref : refs) {
			ManagedObject mo = (ManagedObject) objects.get(ref.getId());
			if (mo != null) {
				CMSObjectBundle bundle = (CMSObjectBundle) bundleMap.get(ref.getId());
				this.registerObject(mo, bundle);
				continue;
			}
			MsgObject msg = ContentIndexerMsg.getMsgObject("5", (Object) ref.getId());
			logger.info((Object) msg);
		}
	}

	private void registerObjects(List list, CMSObjectBundleFlags flags)
			throws ApplicationException, ContentIndexException, ConfigException {
		HashMap bundleMap = this.getBundleMap(list, flags);
		for (Object aList : list) {
			ManagedObject mo = (ManagedObject) aList;
			String moId = mo.getContentManagementId().getObjectId().getId();
			CMSObjectBundle bundle = (CMSObjectBundle) bundleMap.get(moId);
			this.registerObject(mo, bundle);
		}
	}

	private HashMap getBundleMap(Collection managedObjs, CMSObjectBundleFlags flags)
			throws ApplicationException, ConfigException {
		SearchBundleUtils utils = new SearchBundleUtils();
		return utils.getBundles(managedObjs, flags);
	}

	private void registerObject(ManagedObject mo, CMSObjectBundle bundle)
			throws ApplicationException, ContentIndexException {
		String guid = "";
		int counter = 0;
		boolean indexed = false;
		while (!indexed) {
			MsgObject msg;
			++counter;
			try {
				guid = mo.getContentManagementId().getId();
				this.logDebug("Attempting to register object: " + guid);
				// TODO: Verify Viglet Turing Connection
				// if (this.verifySearchEngineConnection()) {
				MsgObject msg2;
				if (mo instanceof ContentInstance) {
					ContentInstance ci = (ContentInstance) mo;
					RecordBundle recordBundle = (RecordBundle) bundle;
					///
					indexed = TurWEMIndexer.IndexCreate(mo, turingConfig, null, null);
					////

					// this.getRegistrar().registerContentInstance(ci,
					// (RecordItemBundle)recordBundle);
					msg2 = ContentIndexerMsg.getMsgObject("10", (Object) guid);
					this.logDebug(msg2.localize());
					// indexed = true;
					continue;
				}
				if (!(mo instanceof StaticFile))
					continue;
				StaticFile sf = (StaticFile) mo;
				StaticFileBundle sfBundle = (StaticFileBundle) bundle;
				this.getRegistrar().registerFile(sf, null, sfBundle, null);
				msg2 = ContentIndexerMsg.getMsgObject("10", (Object) guid);
				this.logDebug(msg2.localize());
				indexed = true;
				continue;
				// }
				/*
				 if (counter < 2)
				 continue;
				MsgObject msg3 = ContentIndexerMsg.getMsgObject("3");
				logger.error((Object) msg3);
				this.consoleOut(msg3);
				this.consoleOut(ContentIndexerMsg.getMsgObject("27"));				
				throw new ContentIndexException(msg3);
				*/
			} catch (ValidationException e) {
				msg = ContentIndexerMsg.getMsgObject("9", (Object) guid);
				this.consoleOut(msg);
				logger.error((Object) msg, (Throwable) e);
				continue;
			} catch (AuthorizationException e) {
				msg = ContentIndexerMsg.getMsgObject("9", (Object) guid);
				this.consoleOut(msg);
				logger.error((Object) msg, (Throwable) e);
				continue;
			} catch (RemoteException e) {
				msg = ContentIndexerMsg.getMsgObject("13", (Object) e.getMessage());
				this.consoleOut(msg);
				logger.error((Object) msg, (Throwable) e);
				throw ApplicationException.getOne((IVgnErrorCode) ASErrorCode.REMOTE_ERROR, (Object) this,
						(MsgObject) msg);
			} catch (ConfigException e) {
				msg = ContentIndexerMsg.getMsgObject("13", (Object) e.getMessage());
				this.consoleOut(msg);
				logger.error((Object) msg, (Throwable) e);
				throw ApplicationException.getOne((IVgnErrorCode) ASErrorCode.CONFIG_ERROR, (Object) this,
						(MsgObject) msg);
			}
		}
	}

	private void registerObject(ExternalResourceObject mo, String typeName)
			throws ApplicationException, ContentIndexException {
		String guid = "";
		int counter = 0;
		boolean indexed = false;
		while (!indexed) {
			MsgObject msg;
			++counter;
			try {
				guid = String.valueOf(mo.getId());
				this.logDebug("Attempting to register object: " + guid);
				if (this.verifySearchEngineConnection()) {
					MsgObject msg2;
					indexed = TurWEMIndexer.IndexCreate(mo, typeName, turingConfig);
					msg2 = ContentIndexerMsg.getMsgObject("10", (Object) guid);
					this.logDebug(msg2.localize());
					continue;
				}
				if (counter < 2)
					continue;
				MsgObject msg3 = ContentIndexerMsg.getMsgObject("3");
				logger.error((Object) msg3);
				this.consoleOut(msg3);
				this.consoleOut(ContentIndexerMsg.getMsgObject("27"));
				throw new ContentIndexException(msg3);
			} catch (ValidationException e) {
				msg = ContentIndexerMsg.getMsgObject("9", (Object) guid);
				this.consoleOut(msg);
				logger.error((Object) msg, (Throwable) e);
				continue;
			} catch (AuthorizationException e) {
				msg = ContentIndexerMsg.getMsgObject("9", (Object) guid);
				this.consoleOut(msg);
				logger.error((Object) msg, (Throwable) e);
				continue;
			}
		}
	}

	public void registerObjectType(ObjectType ot) throws ApplicationException {
		String objectTypeName = "";
		try {
			objectTypeName = ot.getData().getName();
			this.consoleOut("Attempting to register Object Type: " + objectTypeName);
			this.getRegistrar().registerObjectType(ot, null);
			MsgObject mo = ContentIndexerMsg.getMsgObject("8", (Object) objectTypeName);
			logger.info((Object) mo);
		} catch (ApplicationException e) {
			MsgObject mo = ContentIndexerMsg.getMsgObject("7", (Object) objectTypeName);
			this.consoleOut(mo);
			logger.error((Object) mo);
		} catch (ValidationException e) {
			MsgObject mo = ContentIndexerMsg.getMsgObject("7", (Object) objectTypeName);
			this.consoleOut(mo);
			logger.error((Object) mo);
		} catch (AuthorizationException e) {
			MsgObject mo = ContentIndexerMsg.getMsgObject("7", (Object) objectTypeName);
			this.consoleOut(mo);
			logger.error((Object) mo);
		} catch (RemoteException e) {
			MsgObject mo = ContentIndexerMsg.getMsgObject("13", (Object) e.getMessage());
			this.consoleOut(mo);
			logger.error((Object) mo, (Throwable) e);
			throw ApplicationException.getOne((IVgnErrorCode) ASErrorCode.REMOTE_ERROR, (Object) this, (MsgObject) mo);
		} catch (ConfigException e) {
			MsgObject mo = ContentIndexerMsg.getMsgObject("13", (Object) e.getMessage());
			this.consoleOut(mo);
			logger.error((Object) mo, (Throwable) e);
			throw ApplicationException.getOne((IVgnErrorCode) ASErrorCode.CONFIG_ERROR, (Object) this, (MsgObject) mo);
		}
	}

	private ISearchIndex getRegistrar() throws ApplicationException {
		if (this.registrar == null) {
			this.registrar = PluggableInterfaceFactory.getSearchIndex();
		}
		return this.registrar;
	}

	private String getSurveyFileName() {
		return this.surveyFileName;
	}

	protected void writeSurveyEntries(List moList) {
		StringBuffer buff = new StringBuffer(moList.size() * 41);
		for (Object aMoList : moList) {
			ManagedObject mo = (ManagedObject) aMoList;
			buff.append(mo.getContentManagementId().getId());
			buff.append(System.getProperty("line.separator"));
		}
		try {
			FileIOUtil.writeToFile((CharSequence) buff, (String) this.getSurveyFileName(), (boolean) false);
		} catch (IOException e) {
			MsgObject msg = ContentIndexerMsg.getMsgObject("13", (Object) e.getMessage());
			logger.error((Object) msg, (Throwable) e);
		}
	}

	public void writeEOF() {
		try {
			StringBuffer buff = new StringBuffer("EOF");
			FileIOUtil.writeToFile((CharSequence) buff, (String) this.getSurveyFileName(), (boolean) false);
		} catch (IOException e) {
			MsgObject msg = ContentIndexerMsg.getMsgObject("13", (Object) e.getMessage());
			logger.error((Object) msg, (Throwable) e);
		}
	}

	public boolean indexGUIDsFromFile(String filePath)
			throws ApplicationException, ValidationException, ConfigException, ContentIndexException {
		MsgObject mo;
		File vgnGUIDsFile = new File(filePath);
		RandomAccessFile raFile = null;
		boolean validGUIDsRead = false;
		ArrayList<String> recordGuids = new ArrayList<String>();
		ArrayList<String> staticfileGuids = new ArrayList<String>();
		long offset = this.getOffset(vgnGUIDsFile);
		logger.info((Object) ContentIndexerMsg.getMsgObject("45", offset));
		int currentPage = 1;
		int totalEntries = this.getTotalEntries(filePath, offset);
		this.consoleOut(
				ContentIndexerMsg.getMsgObject("34", (Object) Integer.toString(totalEntries), (Object) filePath));
		int pageSize = this.getPageSize();
		int totalPages = totalEntries > 0 ? (totalEntries + pageSize - 1) / pageSize : totalEntries / pageSize;
		try {
			long elapsed;
			long start;
			String temp = "";
			raFile = new RandomAccessFile(vgnGUIDsFile, "r");
			raFile.seek(offset);
			while ((temp = raFile.readLine()) != null) {
				if (temp.length() <= 0 || "EOF".equals(temp))
					continue;
				if (temp.endsWith("STFL")) {
					staticfileGuids.add(temp);
				} else if (temp.endsWith("RCRD")) {
					recordGuids.add(temp);
				} else {
					this.consoleOut(ContentIndexerMsg.getMsgObject("33", (Object) temp));
				}
				if (recordGuids.size() + staticfileGuids.size() != pageSize)
					continue;
				this.consoleOut(ContentIndexerMsg.getMsgObject("30", (Object) Integer.toString(currentPage),
						(Object) Integer.toString(totalPages)));
				if (recordGuids.size() > 0) {
					long recordStart = System.currentTimeMillis();
					this.indexByGUIDs(recordGuids, this.getRecordBundleFlags());
					elapsed = System.currentTimeMillis() - recordStart;
					this.consoleOut(ContentIndexerMsg.getMsgObject("31", (Object) Integer.toString(recordGuids.size()),
							(Object) Long.toString(elapsed)));
					recordGuids = new ArrayList();
				}
				if (staticfileGuids.size() > 0) {
					long staticfileStart = System.currentTimeMillis();
					this.indexByGUIDs(staticfileGuids, this.getStaticFileBundleFlags());
					elapsed = System.currentTimeMillis() - staticfileStart;
					this.consoleOut(ContentIndexerMsg.getMsgObject("32",
							(Object) Integer.toString(staticfileGuids.size()), (Object) Long.toString(elapsed)));
					staticfileGuids = new ArrayList();
				}
				++currentPage;
				this.writeOutPageOffset(vgnGUIDsFile, raFile.getFilePointer());
			}
			if (recordGuids.size() > 0) {
				this.consoleOut(ContentIndexerMsg.getMsgObject("30", (Object) Integer.toString(currentPage++),
						(Object) Integer.toString(totalPages)));
				start = System.currentTimeMillis();
				this.indexByGUIDs(recordGuids, this.getRecordBundleFlags());
				elapsed = System.currentTimeMillis() - start;
				this.consoleOut(ContentIndexerMsg.getMsgObject("31", (Object) Integer.toString(recordGuids.size()),
						(Object) Long.toString(elapsed)));
			}
			if (staticfileGuids.size() > 0) {
				this.consoleOut(ContentIndexerMsg.getMsgObject("30", (Object) Integer.toString(currentPage++),
						(Object) Integer.toString(totalPages)));
				start = System.currentTimeMillis();
				this.indexByGUIDs(staticfileGuids, this.getStaticFileBundleFlags());
				elapsed = System.currentTimeMillis() - start;
				this.consoleOut(ContentIndexerMsg.getMsgObject("32", (Object) Integer.toString(staticfileGuids.size()),
						(Object) Long.toString(elapsed)));
			}
			this.deleteOffsetFile(vgnGUIDsFile);
		} catch (IOException e) {
			mo = ContentIndexerMsg.getMsgObject("17", (Object) e.getMessage());
			logger.error((Object) mo);
			this.consoleOut(mo);
			validGUIDsRead = false;
		} finally {
			try {
				if (null != raFile) {
					raFile.close();
				}
			} catch (IOException e) {
				mo = ContentIndexerMsg.getMsgObject("18", (Object) e.getMessage());
				logger.error((Object) mo);
				this.consoleOut(mo);
				validGUIDsRead = false;
			}
		}
		return validGUIDsRead;
	}

	private void deleteOffsetFile(File vgnGUIDsFile) {
		File offsetFile = new File(vgnGUIDsFile.getParentFile(), vgnGUIDsFile.getName() + ".offset");
		boolean deleted = offsetFile.delete();
		if (!deleted && offsetFile.isFile()) {
			MsgObject mo = ContentIndexerMsg.getMsgObject("46", (Object) offsetFile.getAbsolutePath());
			logger.info((Object) mo);
			this.consoleOut(mo);
		}
	}

	private long getOffset(File vgnGUIDsFile) {
		long offset = 0;
		File offsetFile = new File(vgnGUIDsFile.getParentFile(), vgnGUIDsFile.getName() + ".offset");
		if (offsetFile.isFile()) {
			MsgObject mo;
			FileInputStream fis = null;
			ObjectInputStream ois = null;
			try {
				fis = new FileInputStream(offsetFile);
				ois = new ObjectInputStream(fis);
				offset = ois.readLong();
				MsgObject mo2 = ContentIndexerMsg.getMsgObject("47");
				logger.info((Object) mo2);
				this.consoleOut(mo2);
			} catch (IOException e) {
				mo = ContentIndexerMsg.getMsgObject("44", (Object) e.getMessage());
				logger.warn((Object) mo);
				this.consoleOut(mo);
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
					if (ois != null) {
						ois.close();
					}
				} catch (IOException e) {
					mo = ContentIndexerMsg.getMsgObject("48", (Object) offsetFile.getAbsolutePath());
					logger.warn((Object) mo);
					this.consoleOut(mo);
				}
			}
		}
		MsgObject mo = ContentIndexerMsg.getMsgObject("42");
		logger.info((Object) mo);
		this.consoleOut(mo);
		return offset;
	}

	private void writeOutPageOffset(File vgnGUIDFile, long filePointer) {
		block14: {
			MsgObject mo;
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
				MsgObject mo2 = ContentIndexerMsg.getMsgObject("41", (Object) fileDirectory.getAbsolutePath());
				logger.warn((Object) mo2);
				this.consoleOut(mo2);
			} catch (IOException e) {
				mo = ContentIndexerMsg.getMsgObject("43", (Object) fileDirectory.getAbsolutePath());
				logger.warn((Object) mo);
				this.consoleOut(mo);
			} finally {
				try {
					if (oos != null) {
						oos.close();
					}
					if (fos != null) {
						fos.close();
					}
				} catch (IOException e) {
					mo = ContentIndexerMsg.getMsgObject("48", (Object) offsetFile.getAbsolutePath());
					logger.warn((Object) mo);
					this.consoleOut(mo);
				}
			}
		}
	}

	public void replaceFieldsOfGUIDsFromFile(String filePath, String fieldName, String fieldValue)
			throws ApplicationException, ValidationException, ConfigException {
		MsgObject mo;
		File vgnGUIDsFile = new File(filePath);
		FileReader fileReader = null;
		BufferedReader buffReader = null;
		ArrayList<String> moGuids = new ArrayList<String>();
		int currentPage = 1;
		int totalEntries = this.getTotalEntries(filePath, 0);
		this.logDebug((Object) ContentIndexerMsg.getMsgObject("34", (Object) Integer.toString(totalEntries),
				(Object) filePath));
		int pageSize = this.getPageSize();
		int totalPages = totalEntries > 0 ? (totalEntries + pageSize - 1) / pageSize : totalEntries / pageSize;
		try {
			String temp = "";
			fileReader = new FileReader(vgnGUIDsFile);
			buffReader = new BufferedReader(fileReader);
			while ((temp = buffReader.readLine()) != null) {
				if (temp.length() <= 0 || "EOF".equals(temp))
					continue;
				moGuids.add(temp);
				if (moGuids.size() != pageSize)
					continue;
				this.logDebug((Object) ContentIndexerMsg.getMsgObject("30", (Object) Integer.toString(currentPage++),
						(Object) Integer.toString(totalPages)));
				this.replaceByGUIDs(moGuids, fieldName, fieldValue);
				moGuids = new ArrayList();
			}
			if (moGuids.size() > 0) {
				this.logDebug((Object) ContentIndexerMsg.getMsgObject("30", (Object) Integer.toString(currentPage++),
						(Object) Integer.toString(totalPages)));
				this.replaceByGUIDs(moGuids, fieldName, fieldValue);
			}
		} catch (IOException e) {
			mo = ContentIndexerMsg.getMsgObject("17", (Object) e.getMessage());
			logger.error((Object) mo);
		} finally {
			try {
				if (null != buffReader) {
					buffReader.close();
				}
				if (null != fileReader) {
					fileReader.close();
				}
			} catch (IOException e) {
				mo = ContentIndexerMsg.getMsgObject("18", (Object) e.getMessage());
				logger.error((Object) mo);
			}
		}
	}

	protected int getTotalEntries(String fileName, long offset) {
		int total;
		MsgObject mo;
		total = 0;
		RandomAccessFile raFile = null;
		try {
			String temp;
			raFile = new RandomAccessFile(fileName, "r");
			raFile.seek(offset);
			while ((temp = raFile.readLine()) != null && !"EOF".equals(temp)) {
				++total;
			}
		} catch (IOException e) {
			mo = ContentIndexerMsg.getMsgObject("17", (Object) e.getMessage());
			logger.error((Object) mo);
			this.consoleOut(mo);
		} finally {
			try {
				if (null != raFile) {
					raFile.close();
				}
			} catch (IOException e) {
				mo = ContentIndexerMsg.getMsgObject("18", (Object) e.getMessage());
				logger.warn((Object) mo);
				this.consoleOut(mo);
			}
		}
		return total;
	}

	protected void logDebug(Object debugMessage) {
		if (logger.isDebugEnabled()) {
			logger.debug(debugMessage);
		}
	}

	protected void consoleOut(String message) {
		this.output.println(message);
		this.logDebug(message);
	}

	protected void consoleOut(MsgObject message) {
		this.output.println(message.localize());
		this.logDebug(message.localize());
	}

	private static int getBulkActionChunkSize() {
		int chunkSize;
		block3: {
			chunkSize = 500;
			try {
				chunkSize = ConfigUtil.getASComponent().getBulkActionChunkSize();
			} catch (ConfigException ce) {
				if (!logger.isDebugEnabled())
					break block3;
				logger.debug(
						(Object) ("ContentIndexer.getBulkActionChunkSize() - unable to get BULK_ACTION_CHUNK_SIZE - setting chunkSize to "
								+ chunkSize));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug((Object) ("ContentIndexer.getBulkActionChunkSize() - Chunk Size : " + chunkSize));
		}
		return chunkSize;
	}

	public void setSearchEngineConnection(boolean connection) {
		this.mSearchEngineConnection = connection;
	}

	public void purgeAndReIndex(List otList, String outputFileName) throws ValidationException, ApplicationException,
			ContentIndexException, ConfigException, AuthorizationException, MalformedURLException {
		MsgObject mo = ContentIndexerMsg.getMsgObject("49");
		this.consoleOut(mo);
		this.consoleOut("");
		PluggableInterfaceFactory.getSearchAdmin().reset();
		this.indexByObjectTypes(otList, outputFileName, null);
	}

	protected void indexResetByType(String typeName) throws MalformedURLException {
		TurWEMIndexer.IndexDeleteByType(typeName, turingConfig);
	}

	protected void indexDeleteById(String typeName, String id) throws MalformedURLException {
		this.consoleOut("DELETE ID " + id + " in Viglet Turing index");
		TurWEMIndexer.IndexDelete(id, turingConfig, null, null);
		this.consoleOut("ID " + id + " DELETED in Viglet Turing index");
	}

	public void indexDeleteByIds(String mObjectTypeName, String[] vcmids) throws MalformedURLException {
		this.consoleOut("Removing a total of " + vcmids.length + " indexed items.");
		for (String id : vcmids) {
			this.indexDeleteById(mObjectTypeName, id);
		}
	}
}
