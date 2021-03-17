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

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.viglet.turing.wem.broker.indexer.ssl.TSLSocketConnectionFactory;
import com.viglet.turing.wem.config.IHandlerConfiguration;
import com.viglet.turing.wem.util.TuringUtils;
import com.vignette.logging.context.ContextLogger;

public class TurWEMDeindex {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMDeindex.class);

	// This method deletes the content to the Viglet Turing broker
	public static boolean indexDelete(String guid, IHandlerConfiguration config) {
		String parameter = "&id=".concat(guid);
		
		return indexDeleteGeneric(parameter, config);
	}

	public static boolean indexDeleteByType(String typeName, IHandlerConfiguration config) {

		String parameter =  "&type=".concat(typeName);
		
		return indexDeleteGeneric(parameter, config);
	}

	public static boolean indexDeleteGeneric(String parameter, IHandlerConfiguration config) {

		boolean success = false;
		try {
			URL url = new URL( config.getTuringURL() + "/?action=delete&index=" + config.getIndex() + "&config="
					+ config.getConfig() + parameter );
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
			httpsURLConnection.setSSLSocketFactory(new TSLSocketConnectionFactory());
			TuringUtils.basicAuth(config, httpsURLConnection);
			try {
				httpsURLConnection.setRequestMethod("GET");
				httpsURLConnection.setDoOutput(true);
				int result = httpsURLConnection.getResponseCode();

				TuringUtils.getResponseBody(httpsURLConnection, result);
				if (log.isDebugEnabled()) {
					log.debug("Viglet Turing Delete Request URI:" + httpsURLConnection.getURL());
					log.debug("Viglet Turing indexer response HTTP result is: " + result);
					log.debug("Viglet Turing indexer response HTTP result is: "
							+ TuringUtils.getResponseBody(httpsURLConnection, result));
				}
			} finally {
				httpsURLConnection.disconnect();
			}
			success = true;

		} catch (Exception e) {

			log.error("Can't DELETE in Viglet Turing index: " + e.getMessage());
		}

		return success;

	}

}
