package com.viglet.turing.listener;

import com.viglet.turing.broker.indexer.TurWEMIndexer;
import com.viglet.turing.config.IHandlerConfiguration;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.server.event.AsDeploymentEvent;
import com.vignette.logging.context.ContextLogger;


public class DeploymentHandler {

    /**
     * Category used to log messages for this class.
     */
	private static final ContextLogger log = ContextLogger.getLogger(DeploymentHandler.class);

    IHandlerConfiguration config;

    public DeploymentHandler(IHandlerConfiguration config) {
        this.config = config;
    }

    public void onManagedObjectCreate(ManagedObject mo, AsDeploymentEvent deploymentEvent) {
        boolean result = TurWEMIndexer.IndexCreate(mo, config, deploymentEvent.getSiteId().getId(), deploymentEvent.getSiteName());
        log.debug("Viglet Turing Indexing Create: " + result);
     }

    public void onManagedObjectUpdate(ManagedObject mo, AsDeploymentEvent deploymentEvent) {
        boolean result = TurWEMIndexer.IndexUpdate(mo, config, deploymentEvent.getSiteId().getId(), deploymentEvent.getSiteName());
        log.debug("Viglet Turing Indexing Update: " + result);
    }

    public void onManagedObjectDelete(ManagedObject mo, AsDeploymentEvent deploymentEvent) {
        String GUID = deploymentEvent.getManagedObjectVCMRef().getId();
        boolean result = TurWEMIndexer.IndexDelete(GUID, config, deploymentEvent.getSiteId().getId(), deploymentEvent.getSiteName());
        log.debug("Viglet Turing Indexing Delete: " + result);
    }

}