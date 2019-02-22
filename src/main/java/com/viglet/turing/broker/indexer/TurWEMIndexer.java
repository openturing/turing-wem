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
package com.viglet.turing.broker.indexer;

import com.viglet.turing.config.IHandlerConfiguration;
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
}
