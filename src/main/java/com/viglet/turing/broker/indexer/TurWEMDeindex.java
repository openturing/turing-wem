package com.viglet.turing.broker.indexer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.viglet.turing.config.IHandlerConfiguration;
import com.vignette.logging.context.ContextLogger;

public class TurWEMDeindex {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMDeindex.class);

	// This method deletes the content to the Viglet Turing broker
	public static boolean indexDelete(String guid, IHandlerConfiguration config) {

		boolean success = false;
		try {
			GetMethod get = new GetMethod(config.getTuringURL() + "/?action=delete&index=" + config.getIndex()
					+ "&config=" + config.getConfig() + "&id=" + guid);
			HttpClient httpclient = new HttpClient();
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing Delete Request URI:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP result is: " + result);
				log.debug("Viglet Turing indexer response HTTP result is: " + get.getResponseBodyAsString());
			}
			get.releaseConnection();
			success = true;

		} catch (Exception e) {

			log.error("Can't DELETE in Viglet Turing index: " + e.getMessage());
		}

		return success;

	}

	public static boolean indexDeleteByType(String typeName, IHandlerConfiguration config) {
		boolean success = false;
		try {
			GetMethod get = new GetMethod(config.getTuringURL() + "/?action=delete&index=" + config.getIndex()
					+ "&config=" + config.getConfig() + "&type=" + typeName);
			HttpClient httpclient = new HttpClient();
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing Delete Request URI:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP result is: " + result);
				log.debug("Viglet Turing indexer response HTTP result is: " + get.getResponseBodyAsString());
			}
			get.releaseConnection();
			success = true;

		} catch (Exception e) {

			log.error("Can't DELETE in Viglet Turing index: " + e.getMessage());
		}

		return success;
	}
}
