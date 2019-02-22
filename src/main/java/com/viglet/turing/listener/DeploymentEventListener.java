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
package com.viglet.turing.listener;

import com.viglet.turing.config.GenericResourceHandlerConfiguration;
import com.viglet.turing.config.IHandlerConfiguration;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.AuthorizationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.server.event.AsDeploymentEvent;
import com.vignette.as.server.event.AsEvent;
import com.vignette.as.server.event.IAsEventListener;
import com.vignette.logging.context.ContextLogger;

public class DeploymentEventListener implements IAsEventListener {

    /**
     * Category used to log messages for this class.
     */
	private static final ContextLogger log = ContextLogger.getLogger(DeploymentEventListener.class);

    /**
     * Processes, or consumes, the deployment events.  Two deployment events are
     * of particular interest, update and delete of a managed object upon deployment
     *
     * @param event the event.
     * @throws ApplicationException   thrown if application errors occur when an <code>AsEvent</code> is processed.
     * @throws AuthorizationException thrown if security errors occur when an <code>AsEvent</code> is processed.
     * @throws ValidationException    thrown if validation errors occur when an <code>AsEvent</code> is processed.
     */
    public void consume(AsEvent event)
            throws ApplicationException, AuthorizationException,
            ValidationException {
    	try {
	    	// If this is an actual item being deployed, process the item
	        if (event instanceof AsDeploymentEvent) {
	            AsDeploymentEvent deploymentEvent = (AsDeploymentEvent) event;
	            String type = deploymentEvent.getType();
	            ManagedObject mo = deploymentEvent.getManagedObject();
	
	            IHandlerConfiguration config = new GenericResourceHandlerConfiguration();
	
	            DeploymentHandler handler = new DeploymentHandler(config);
	
	            // Call out to the appropriate method depending on event type
	            if (type.equals(AsDeploymentEvent.MANAGED_OBJECT_CREATE)) {
	                if (log.isDebugEnabled()) {
	                    log.debug("Viglet Turing DeploymentEvent() - Create object");
	                }
	                handler.onManagedObjectCreate(mo, deploymentEvent);
	            } else if (type.equals(AsDeploymentEvent.MANAGED_OBJECT_UPDATE)) {
	                if (log.isDebugEnabled()) {
	                    log.debug("Viglet Turing DeploymentEvent() - Update object");
	                }
	                handler.onManagedObjectUpdate(mo, deploymentEvent);
	            } else if (type.equals(AsDeploymentEvent.MANAGED_OBJECT_DELETE)) {
	                if (log.isDebugEnabled()) {
	                    log.debug("Viglet Turing DeploymentEvent() - Delete object");
	                }
	                handler.onManagedObjectDelete(mo, deploymentEvent);
	            }
	        }
    	} catch (Exception e) {
    		if (log.isDebugEnabled()) {
    			log.error(e, e);
    		}
    	}
    }

    public int getPriority() {
        return HIGH_PRIORITY;
    }


}
