package com.viglet.turing.broker.indexer;

import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class TurWEMIndexer {

	private static final ContextLogger log = ContextLogger.getLogger(TurWEMIndexer.class);

	public static boolean IndexCreate(ManagedObject mo, IHandlerConfiguration config, String siteID, String site) {
		if (log.isDebugEnabled()) {
			log.debug("Creating Object in Viglet Turing index");
		}
		TurWEMIndex.indexCreate(mo, config);
		return true;
	}

	public static boolean IndexUpdate(ManagedObject mo, IHandlerConfiguration config, String siteID, String site) {
		if (log.isDebugEnabled()) {
			log.debug("Updating Object in Viglet Turing index");
		}
		TurWEMIndex.indexCreate(mo, config);
		return true;
	}

	public static boolean IndexDelete(String GUID, IHandlerConfiguration config, String siteID, String site) {
		if (log.isDebugEnabled()) {
			log.debug("Deleting Object in Viglet Turing index");
		}
		TurWEMDeindex.indexDelete(GUID, config);
		return true;
	}

	public static boolean IndexDeleteByType(String typeName, IHandlerConfiguration config) {
		if (log.isDebugEnabled()) {
			log.debug("Deleting Object in Viglet Turing index");
		}
		TurWEMDeindex.indexDeleteByType(typeName, config);
		return true;
	}

	public static boolean IndexCreate(ExternalResourceObject mo, String typeName, IHandlerConfiguration config) {
		if (log.isDebugEnabled()) {
			log.debug("Creating Object in Viglet Turing index");
		}
		TurWEMIndex.indexCreate(mo, typeName, config);
		return true;
	}

}