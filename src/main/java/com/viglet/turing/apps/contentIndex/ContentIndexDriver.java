// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
// Copyright (C) 2002-2010 Vignette Corporation  All rights reserved.
// THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE SECRET
// OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
// EXCEPT AS EXPLICITLY STATED IN A WRITTEN AGREEMENT BETWEEN THE PARTIES,
// THE SOFTWARE IS PROVIDED AS-IS, WITHOUT WARRANTIES OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
// NONINFRINGEMENT, PERFORMANCE, AND QUALITY.
// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
package com.viglet.turing.apps.contentIndex;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import com.viglet.turing.config.GenericResourceHandlerConfiguration;
import com.viglet.turing.config.IHandlerConfiguration;
import com.vignette.as.apps.contentIndex.ContentIndexerMsg;
import com.vignette.as.client.exception.ASException;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.ContentType;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.StaticFile;
import com.vignette.as.common.pluggable.PluggableInterfaceFactory;
import com.vignette.as.config.ConfigUtil;
import com.vignette.as.server.pluggable.search.opentext.OpenTextMsg;
import com.vignette.authn.AuthnBundle;
import com.vignette.authn.AuthnConsts;
import com.vignette.authn.LoginMgr;
import com.vignette.config.client.common.ConfigException;
import com.vignette.config.util.ConfigLog;
import com.vignette.logging.LoggingManager;
import com.vignette.logging.context.ContextLogger;
import com.vignette.util.FileIOUtil;
import com.vignette.util.GetOpt;
import com.vignette.util.MsgObject;
import com.vignette.util.VgnException;

/**
 * This class acts as Driver for the <tt>vgncontentreindex</tt> command-line
 * utility. The purpose of the utility is to perform re-indexing of existing,
 * searchable Content Instance(s) and Static Files by command line specified
 * Object Type.
 */
public class ContentIndexDriver {

	// Command line switch for displaying help
	public static final char OPT_HELP = '?';

	// Command line switch for VCM Host
	// [REQUIRED]
	public static final char OPT_VCM_HOST = 'h';

	// Command line switch for VCM Username
	// [REQUIRED]
	public static final char OPT_USER_NAME = 'u';

	// Command line switch for VCM Password
	// [REQUIRED]
	public static final char OPT_PASSWORD = 'p';

	// Command line switch for indexing by ObjectType
	// [OPTIONAL IF indexing by GUIDs, otherwise REQUIRED]
	// Valid value: ObjectType xml name
	public static final char OPT_OBJECT_TYPE = 'c';

	// Command line switch for indexing all ObjectTypes
	// [OPTIONAL IF indexing by GUIDs, otherwise REQUIRED]
	public static final char OPT_ALL_OBJECT_TYPES = 'a';

	// Command line switch for indexing by GUIDs
	// [OPTIONAL IF indexing by OBJECTTYPE, otherwise REQUIRED]
	// Valid values: Path to valid file (including filename) containing GUIDS
	// [One GUID on each line of the file]
	public static final char OPT_INDEX_BY_GUIDS = 'g';

	// Command line switch for StaticFile project path. Index all static files
	// in this project
	public static final char OPT_STATIC_FILE_PROJECT_PATH = 's';

	// Command line switch for indicating whether or not to recurse the project
	// [OPTIONAL]
	public static final char OPT_RECURSE_PROJECT_PATH = 'r';

	// Command line switch for Working Dir
	// Typical user would not need to specify this.
	// [OPTIONAL]
	public static final char OPT_WORKING_DIR = 'w';

	// Command line switch for specifying page/chunk size.
	// Typical user would not need to specify this. Is not displayed in USAGE.
	// However, would be useful for adjusting chunk size based on Environment.
	// [OPTIONAL]
	public static final char OPT_PAGE_SIZE = 'z';

	// Command line switch for listing the guids of all managed objects that
	// will be
	// indexed for the given options. Particularly useful for being able to
	// restart the process.
	// fully qualified filename to write the guids to expcected
	// [OPTIONAL]
	public static final char OPT_LIST_GUIDS_FILE = 'f';

	// Command line switch for pausing the driver to allow a user to connect a
	// debugger
	// [OPTIONAL & UNDOCUMENTED]
	public static final char OPT_DEBUG_MODE = 'd';

	// Command line switch for removing content items from search engine
	// [UNDOCUMENTED]
	public static final char OPT_INDEX_RESET = 'x';

	// Command line switch for purging the indexes in search engine
	public static final char OPT_PURGE_REINDEX = 'q';

	// Command line switch for specifying the locale, whose Content Items have
	// to be indexed
	public static final char OPT_INDEX_BY_LOCALE = 'l';

	// Command line switch for forcing the reindex
	// [OPTIONAL] Internal purposes until next release
	public static final char OPT_FORCE_REINDEX = 'y';

	// Command line switch for logging in DEBUG mode rather than INFO
	// [OPTIONAL]
	public static final String OPT_LOGGING_DEBUG = "-v";
	
	// Command line switch for indexing by VCMIDs
	// [OPTIONAL]
	// Valid values: VCMIDs of Contents
	// [List of VCMIDs comma separeted]
	public static final char OPT_INDEX_LIST_VCMIDS = 'i';
	
	// Command line switch for expurg indexed Contents by IDs
	// [OPTIONAL]
	public static final char OPT_EXPURG_BY_IDS = 'e';

	// logger
	private static ContextLogger logger = LoggingManager.getContextLogger(ContentIndexDriver.class);

	// Constant for the VCM Working Directory (vgncfg.properties is located
	// here)
	private static final String WORKING_DIR = "com.vignette.workingDir";

	// Connection attributes
	private static final String WL_JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
	private static final String WL_PROTOCOL = "t3";
	private static final String DEFAULT_VCM_PORT = "27110";

	// VCM Host, Username and Password
	private String mVCMHost = null;
	private String mVCMPort = DEFAULT_VCM_PORT;
	private String mUserName = null;
	private String mPassword = null;

	// ObjectType specified via Command line
	private String mObjectTypeName = null;

	// boolean flag to index All ObjectTypes
	private boolean mIndexAllObjectTypes = false;

	// Path to the file containing GUIDs
	private String mGUIDFilePath = null;

	// VCM Working Directory (vgncfg.properties is located here)
	private String mWorkingDir = null;

	// Index all staticFiles within the project specified by this projectPath
	private String mProjectPath = null;
	private boolean mRecurseProject = false;

	// The output file for listing GUIDs of indexed items if requested
	private boolean mListGuids = false;
	private String mGUIDOutputFilePath = null;

	// boolean flag to show help
	private boolean mShowHelp = false;

	// clean search engine
	private boolean indexReset = false;

	// purge search index
	private boolean purgeReIndex = false;

	// The page size for the indexer to use
	private int mPageSize = 0;

	// The SearchEngine Connection
	private boolean mSearchEngineConnection = false;

	// The logging mode
	private static String mLoggingLevel = "info";

	// locale specified via Command line
	private String mLocale = null;

	// force re index
	private boolean mForceReIndex = false;
	
	// list of VCMIDs
	private String[] vcmids;
	
	// force expurg of indexed contents
	private boolean expurg;

	/**
	 * Default Constructor
	 */
	public ContentIndexDriver() {
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		for (String arg : args) {
			if (arg.equals(OPT_LOGGING_DEBUG)) {
				mLoggingLevel = "debug";
				break;
			}
		}
		try {
		//	ConfigLog.initializeLogging("contentindex.log", mLoggingLevel);
			ConfigLog.initializeLogging("contentindexOTSN.log", "DEBUG");
		} catch (VgnException e) {
			System.err.println("Warning: logging not initialized");
		}

		ContentIndexDriver driver = new ContentIndexDriver();
		if ((args.length > 0) && (args[0].equalsIgnoreCase("-d"))) {
			try {
				System.out.print("Press enter to continue...");
				System.in.read();
				System.out.println("    continuing...");
			} catch (IOException e) {
				// ignore
			}

			String[] newArgs = new String[args.length - 1];
			System.arraycopy(args, 1, newArgs, 0, (args.length - 1));
			driver.performProcessing(newArgs);
		} else {
			driver.performProcessing(args);
		}
	}

	/**
	 * Encapsulates the core 'driver' functionality. 1. Processes/Validates
	 * inputs. 2. Tests VCM and search engine Connections. 3. Retrieves Content
	 * Instances by Content Type. 4. Pushes Content Instances to the search
	 * engine for (re)indexing.
	 *
	 * @param args
	 *            String[] Command line params
	 */
	public void performProcessing(String[] args) {

		// STEP 1: Process command line args
		if (args.length == 0) {
			showHelp(false);
			return;
		}

		processArgs(args);

		if (helpShown()) {
			return; // Help displayed; No more processing
		}

		// STEP 2: Verify (and make) connections to VCM and the search engine
		if (!verifyConnections()) {
			return; // If either VCM or the search engine cannot be contacted,
					// bail...
		}
		try {
			// STEP 3: add the system regions to search engine
			PluggableInterfaceFactory.getSearchAdmin().initialize();
		} catch (ASException e) {
			e.printStackTrace();
			MsgObject mo = OpenTextMsg.getMsgObject(OpenTextMsg.ERROR_ADDING_SYS_REGIONS, e.getLocalizedMessage());
			error(mo, e);
		}
		// Delegate the user request information to the ContentIndexer
		doIndexAction();
	}

	/**
	 * Performs the index action requested by the user.
	 */
	private void doIndexAction() {
		try {
			ContentIndexer indexer = getIndexer();
			if (mIndexAllObjectTypes) {
				indexer.purgeAndReIndex(getAllIndexableObjectTypes(), mGUIDOutputFilePath);
			} else if (null != mObjectTypeName) {
				if (vcmids != null) {
			    	if (expurg) {
			    		indexer.indexDeleteByIds(mObjectTypeName, vcmids);
			    		return;
			    	}
					indexer.indexByObjectType(mObjectTypeName, vcmids);
				} else {
					indexer.indexByObjectType(mObjectTypeName, mGUIDOutputFilePath);
				}
			} else if (null != mGUIDFilePath) {
				indexer.indexGUIDsFromFile(mGUIDFilePath);
			} else if (null != mProjectPath) {
				indexer.indexStaticFiles(mProjectPath, mRecurseProject, mGUIDOutputFilePath);
			} else if (indexReset) {
				indexer.indexReset();
			} else if (purgeReIndex) {
				indexer.purgeAndReIndex(getAllIndexableObjectTypes(), mGUIDOutputFilePath);
			} else if (null != mLocale) {
				indexer.indexByObjectTypes(getAllIndexableObjectTypes(), mGUIDOutputFilePath, mLocale);
			}

		} catch (Exception e) {
			e.printStackTrace();
			MsgObject mo = ContentIndexerMsg.getMsgObject(ContentIndexerMsg.EXCEPTION_OCCURRED,
					e.getLocalizedMessage());
			error(mo, e);
		}
	}

	/**
	 * Returns a list of all indexable ObjectTypes in the system. Currently this
	 * includes all ContentTypes and the StaticFile ObjectType. No other system
	 * defined ObjectTypes are supported yet.
	 *
	 * @return List containing all indexable ObjectTypes
	 */
	private List getAllIndexableObjectTypes() throws ApplicationException, ValidationException {
		IPagingList contentTypes = ContentType.findAll();
		List otList = contentTypes.asList();
		otList.add(StaticFile.getTypeObjectTypeRef().getObjectType());
		return otList;
	}

	/**
	 * Constructs and configures the indexer to be used.
	 *
	 * @return the configured ContentIndexer
	 */
	private ContentIndexer getIndexer() {
		IHandlerConfiguration otsnConfig = new GenericResourceHandlerConfiguration();
		ContentIndexer indexer = new ContentIndexer();
		indexer.setPageSize(mPageSize);
		indexer.setSearchEngineConnection(mSearchEngineConnection);
		indexer.setForceReIndex(mForceReIndex);
		indexer.setOTSNConfig(otsnConfig);
		return indexer;
	}

	/**
	 * Tests (and makes) the connections to the VCM application and the search
	 * engine.
	 *
	 * @return boolean connection status
	 */
	private boolean verifyConnections() {

		if (!connectToVCM()) {
			return false;
		}

		try {
			if (!PluggableInterfaceFactory.getSearchAdmin().isIndexReady()) {
				MsgObject mo = ContentIndexerMsg.getMsgObject(ContentIndexerMsg.SEARCH_ENGINE_UNAVAILABLE);
				error(mo);
				return false;
			}
			mSearchEngineConnection = true;

		} catch (ASException e) {
			e.printStackTrace();
			MsgObject mo = ContentIndexerMsg.getMsgObject(ContentIndexerMsg.SEARCH_ENGINE_UNAVAILABLE);
			error(mo, e);
			return false;
		}

		return true;
	}

	/**
	 * Returns boolean flag indicating if Usage/Help was displayed
	 *
	 * @return boolean
	 */
	public boolean helpShown() {
		return mShowHelp;
	}

	/**
	 * Set the boolean flag indicating if Usage/Help was displayed
	 */
	public void helpShown(boolean value) {
		mShowHelp = value;
	}

	/**
	 * Helper method to process command line arguments
	 *
	 * @param args
	 *            the command line arguments
	 */
	private void processArgs(String[] args) {

		try {
			// parse the args
			GetOpt opt = new GetOpt(args, getExpectedArgStr());

			// if an invalid option was found, print help
			if (opt.getInvalidArgFound()) {
				error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.INVALID_COMMANDLINE));
				showHelp(false);
				return;
			}

			// Extract all options
			for (char c = opt.nextArg(); c != 0; c = opt.nextArg()) {
				switch (c) {
				case OPT_VCM_HOST:
					mVCMHost = opt.optArg();
					if (null != mVCMHost) {
						StringTokenizer tokenizer = new StringTokenizer(mVCMHost, ":");
						mVCMHost = (String) tokenizer.nextElement();

						if (tokenizer.hasMoreElements()) {
							mVCMPort = (String) tokenizer.nextElement();
						}
					}
					break;
				case OPT_USER_NAME:
					mUserName = opt.optArg();
					break;
				case OPT_PASSWORD:
					mPassword = opt.optArg();
					break;
				case OPT_OBJECT_TYPE:
					mObjectTypeName = opt.optArg();
					break;
				case OPT_ALL_OBJECT_TYPES:
					mIndexAllObjectTypes = true;
					break;
				case OPT_PAGE_SIZE:
					mPageSize = Integer.parseInt(opt.optArg());
					break;
				case OPT_INDEX_BY_GUIDS:
					mGUIDFilePath = opt.optArg();
					break;
				case OPT_WORKING_DIR:
					mWorkingDir = opt.optArg();
					break;
				case OPT_STATIC_FILE_PROJECT_PATH:
					mProjectPath = opt.optArg();
					break;
				case OPT_RECURSE_PROJECT_PATH:
					mRecurseProject = true;
					break;
				case OPT_HELP:
					mShowHelp = true;
					showHelp(false);
					break;
				case OPT_LIST_GUIDS_FILE:
					mListGuids = true;
					mGUIDOutputFilePath = opt.optArg();
					truncateGUIDFile();
					break;
				case OPT_INDEX_RESET:
					indexReset = true;
					break;
				case OPT_PURGE_REINDEX:
					purgeReIndex = true;
					break;
				case OPT_INDEX_BY_LOCALE:
					mLocale = opt.optArg();
					mForceReIndex = true;
					break;
				case OPT_FORCE_REINDEX:
					mForceReIndex = true;
					break;
				case OPT_INDEX_LIST_VCMIDS:
					for (int i = 0;i < args.length;i++) {
						if ("-i".equals(args[i])) {
							String vcmids = args[i + 1];
							if (vcmids != null &&
								!"".equals(vcmids)) {
								this.vcmids = vcmids.split(",");
							}
							break;
						}
					}
					break;
				case OPT_EXPURG_BY_IDS:
					for (int i = 0;i < args.length;i++) {
						if ("-e".equals(args[i])) {
							expurg = true;
							break;
						}
					}
					break;
				default:
					mShowHelp = true;
					showHelp(false);
					break;
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.NO_OPTION));
			showHelp(true);
			return;
		}

		logParameters();

		// returns after displaying help message and logged parameters
		if (mShowHelp) {
			return;
		}

		// Validate host, username & pwd
		if ((null == mVCMHost) || (null == mUserName) || (null == mPassword) || (null == mWorkingDir)) {
			if (!helpShown()) {
				error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.INVALID_COMMANDLINE));
				showHelp(false);
				return;
			}
		}

		// Validate there is no specific object type named if request is for all
		// object types
		if (mIndexAllObjectTypes && (null != mObjectTypeName)) {
			if (!helpShown()) {
				error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.INVALID_COMMANDLINE));
				showHelp(false);
				return;
			}
		}

		// Validate that we've been given something to do
		if ((null == mObjectTypeName) && (null == mGUIDFilePath) && (null == mProjectPath) && !mIndexAllObjectTypes
				&& (null == mLocale)) {
			if (!helpShown() && !indexReset && !purgeReIndex) {
				error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.INVALID_COMMANDLINE));
				showHelp(false);
				return;
			}
		}

		// Validate that only one parameter option is selected
		int nbrOfArgs = 0;
		if (null != mObjectTypeName)
			++nbrOfArgs;
		if (null != mGUIDFilePath)
			++nbrOfArgs;
		if (null != mProjectPath)
			++nbrOfArgs;
		if (mIndexAllObjectTypes)
			++nbrOfArgs;
		if (null != mLocale)
			++nbrOfArgs;

		if (nbrOfArgs > 1) {
			if (!helpShown()) {
				error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.INVALID_COMMANDLINE));
				showHelp(false);
				return;
			}
		}

		// If indexing by GUID input file verify the file exists
		if (null != mGUIDFilePath) {
			File guidFile = new File(mGUIDFilePath);
			if (!guidFile.exists()) {
				error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.CANNOT_READ_GUID_FILE, mGUIDFilePath));
				helpShown(true);
				return;
			}
		}

		init();
	}

	/**
	 * initialize the config space
	 */
	private void init() {
		try {
			// Initialize the config space, vgncfg.properties should be in this
			// directory
			System.setProperty(WORKING_DIR, mWorkingDir);
			ConfigUtil.setHasDataSource(false);
			ConfigUtil.setContainerType(ConfigUtil.CONTAINER_TYPE_SERVLET);
		} catch (ConfigException ce) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to initialize config space local");
			}
		}
	}

	/**
	 * Helper method to display usage and options
	 *
	 * @param optionsOnly
	 *            true if only options should be displayed and not the usage
	 *            instructions, false for both
	 */
	private void showHelp(boolean optionsOnly) {
		if (!optionsOnly) {
			error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.USAGE));
		}
		error(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.OPTIONS));
		logger.error(" -i  Indexing by VCMIDs.\n     Valid values: VCMIDs of Contents.\n     List of VCMIDs comma separeted.");
		System.out.println(" -i  Indexing by VCMIDs.\n     Valid values: VCMIDs of Contents.\n     List of VCMIDs comma separeted.");
		logger.error(" -e  Expurg indexed Contents by IDs");
		System.out.println(" -e  Expurg indexed Contents by IDs");
		mShowHelp = true;
	}

	/**
	 * Helper method for GetOpt.
	 *
	 * @return a String of options to pass to GetOpt
	 */
	private String getExpectedArgStr() {
		final char COLON = ':';
		StringBuffer argBuf = new StringBuffer();
		argBuf.append(OPT_VCM_HOST);
		argBuf.append(COLON);
		argBuf.append(OPT_USER_NAME);
		argBuf.append(COLON);
		argBuf.append(OPT_PASSWORD);
		argBuf.append(COLON);
		argBuf.append(OPT_STATIC_FILE_PROJECT_PATH);
		argBuf.append(COLON);
		argBuf.append(OPT_RECURSE_PROJECT_PATH);
		argBuf.append(OPT_OBJECT_TYPE);
		argBuf.append(COLON);
		argBuf.append(OPT_PAGE_SIZE);
		argBuf.append(COLON);
		argBuf.append(OPT_INDEX_BY_GUIDS);
		argBuf.append(COLON);
		argBuf.append(OPT_WORKING_DIR);
		argBuf.append(COLON);
		argBuf.append(OPT_LIST_GUIDS_FILE);
		argBuf.append(COLON);
		argBuf.append(OPT_HELP);
		argBuf.append(OPT_ALL_OBJECT_TYPES);
		argBuf.append(OPT_INDEX_RESET);
		argBuf.append(OPT_PURGE_REINDEX);
		argBuf.append(OPT_INDEX_BY_LOCALE);
		argBuf.append(COLON);
		argBuf.append(OPT_FORCE_REINDEX);
		argBuf.append(COLON);
		argBuf.append(OPT_INDEX_LIST_VCMIDS);
		argBuf.append(COLON);
		argBuf.append(OPT_EXPURG_BY_IDS);
		return argBuf.toString();
	}

	/**
	 * Connect to VCM using user params
	 * 
	 * @return true if successfully connected, otherwsie false
	 */
	private boolean connectToVCM() {

		if (logger.isDebugEnabled()) {
			logger.debug("Connecting to VCMS...");
		}

		AuthnBundle authnBundle = new AuthnBundle();

		try {
			authnBundle.setHost(mVCMHost);
			authnBundle.setUsername(mUserName);
			authnBundle.setPassword(mPassword);
			authnBundle.setFactory(WL_JNDI_FACTORY);
			authnBundle.setProtocol(WL_PROTOCOL);
			authnBundle.setPort(mVCMPort);

			// We deal with weblogic exclusively since 7.2
			String authType = AuthnConsts.WEBLOGIC_CONTEXT;
			authnBundle.setAuthType(authType);

			// Login
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting VCM Login at " + mVCMHost + ":" + mVCMPort);
			}

			LoginMgr loginMgr = new LoginMgr();
			loginMgr.login(authnBundle);

			System.out.println(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.VCM_CONNECTED).localize());
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully connected to VCM.");
			}

		} catch (VgnException ex) {
			MsgObject mo = ex.getMsgObject();
			System.out.print(ContentIndexerMsg.getMsgObject(ContentIndexerMsg.VCM_CONNECTION_FAILED).localize());
			error(mo);
			return false;
		}

		return true;
	}

	/**
	 * Log the parameters specified by the user.
	 */
	private void logParameters() {
		if (logger.isDebugEnabled()) {
			logger.debug("User Parameters:");
			logger.debug("Hostname: " + mVCMHost);
			logger.debug("Username: " + mUserName);
			if (null != mObjectTypeName) {
				logger.debug("ObjectType: " + mObjectTypeName);
			}
			if (null != mGUIDFilePath) {
				logger.debug("GUID file path: " + mGUIDFilePath);
			}
			if (null != mWorkingDir) {
				logger.debug("Working Directory: " + mWorkingDir);
			}
			if (null != mProjectPath) {
				logger.debug("Project Path: " + mProjectPath);
			}
			logger.debug("Recurse project: " + mRecurseProject);
			logger.debug("List guids: " + mListGuids);
			if (null != mLocale) {
				logger.debug("Language: " + mLocale);
			}
		}
	}

	protected void error(MsgObject mo) {
		logger.error(mo);
		System.out.println(mo.localize());
	}

	protected void error(MsgObject mo, Throwable e) {
		logger.error(mo, e);
		System.out.println(mo.localize());
	}

	// If the file exists, truncate it so that the list of guids in the file is
	// fresh
	protected void truncateGUIDFile() {
		FileIOUtil.truncateFile(mGUIDOutputFilePath);
	}
}
