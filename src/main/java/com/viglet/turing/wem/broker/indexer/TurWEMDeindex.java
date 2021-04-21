/*
 * Copyright (C) 2016-2021 Alexandre Oliveira <alexandre.oliveira@viglet.com> 
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
package com.viglet.turing.wem.broker.indexer;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.viglet.turing.wem.config.IHandlerConfiguration;
import com.viglet.turing.wem.util.TuringUtils;
import com.vignette.logging.context.ContextLogger;

public class TurWEMDeindex {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMDeindex.class);

	private TurWEMDeindex() {
		throw new IllegalStateException("TurWEMDeindex");
	}
	
	// This method deletes the content to the Viglet Turing broker
	public static boolean indexDelete(String guid, IHandlerConfiguration config) {
		return indexDeleteParam("&id=" + guid, config);
	}

	public static boolean indexDeleteByType(String typeName, IHandlerConfiguration config) {
		return indexDeleteParam("&type=" + typeName, config);
	}
	
	public static boolean indexDeleteParam(String param, IHandlerConfiguration config) {
		boolean success = false;
		try {
			GetMethod get = new GetMethod(config.getTuringURL() + "/?action=delete&index=" + config.getIndex()
					+ "&config=" + config.getConfig() + param);
			TuringUtils.basicAuth(config, get);
			HttpClient httpclient = new HttpClient();
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing Delete Request URI:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP Status Code is: " + result);
				log.debug("Viglet Turing indexer response HTTP Result is: " + get.getResponseBodyAsString());
			}
			get.releaseConnection();
			success = true;

		} catch (Exception e) {

			log.error("Can't DELETE in Viglet Turing index: " + e.getMessage());
		}

		return success;
	}
}
