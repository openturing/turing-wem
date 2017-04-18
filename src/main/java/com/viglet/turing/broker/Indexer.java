package com.viglet.turing.broker;

import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class Indexer {
		
	private static final ContextLogger log = ContextLogger.getLogger(Indexer.class);

	public static boolean IndexCreate(ManagedObject mo, IHandlerConfiguration config, String siteID, String site) {
        if (log.isDebugEnabled()) {
        	log.debug("Creating Object in OTSN index");
        }
		OTSN.indexCreate( mo, config);
    	return true;
    }

    public static boolean IndexUpdate(ManagedObject mo, IHandlerConfiguration config, String siteID, String site) {
        if (log.isDebugEnabled()) {
        	log.debug("Updating Object in OTSN index");
        }
        OTSN.indexCreate( mo, config);
        return true;
    }

    public static boolean IndexDelete(String GUID, IHandlerConfiguration config, String siteID, String site) {
        if (log.isDebugEnabled()) {
        	log.debug("Deleting Object in OTSN index");
        }
        OTSN.indexDelete( GUID, config);
        return true;
    }

    public static boolean IndexDeleteByType(String typeName, IHandlerConfiguration config) {
        if (log.isDebugEnabled()) {
        	log.debug("Deleting Object in OTSN index");
        }
        OTSN.indexDeleteByType(typeName, config);
        return true;
    }

	public static boolean IndexCreate(ExternalResourceObject mo, String typeName, IHandlerConfiguration config) {
        if (log.isDebugEnabled()) {
        	log.debug("Creating Object in OTSN index");
        }
		OTSN.indexCreate(mo, typeName, config);
		return true;
	}

}
