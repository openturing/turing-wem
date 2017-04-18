package com.viglet.turing.listener;

import com.viglet.turing.broker.Indexer;
import com.viglet.turing.config.IHandlerConfiguration;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.server.event.AsDeploymentEvent;
import com.vignette.logging.context.ContextLogger;

/**
 * User: Bertrand de Coatpont
 * Date: 13/05/2010
 * Time: 21:26:23
 */
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
        boolean result = Indexer.IndexCreate(mo, config, deploymentEvent.getSiteId().getId(), deploymentEvent.getSiteName());
        log.debug("OTSN Indexing Create: " + result);
     }

    public void onManagedObjectUpdate(ManagedObject mo, AsDeploymentEvent deploymentEvent) {
        boolean result = Indexer.IndexUpdate(mo, config, deploymentEvent.getSiteId().getId(), deploymentEvent.getSiteName());
        log.debug("OTSN Indexing Update: " + result);
    }

    public void onManagedObjectDelete(ManagedObject mo, AsDeploymentEvent deploymentEvent) {
        String GUID = deploymentEvent.getManagedObjectVCMRef().getId();
        boolean result = Indexer.IndexDelete(GUID, config, deploymentEvent.getSiteId().getId(), deploymentEvent.getSiteName());
        log.debug("OTSN Indexing Delete: " + result);
    }

}