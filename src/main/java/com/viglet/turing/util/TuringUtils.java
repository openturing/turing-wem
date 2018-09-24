package com.viglet.turing.util;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TuringUtils {
	private static final ContextLogger log = ContextLogger.getLogger(TuringUtils.class);

	public static String listToString(List<String> stringList) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String s : stringList) {
			if (i++ != stringList.size() - 1) {
				sb.append(s);
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public static boolean isTuringTag(String tagName) {
		return (tagName.equals("turingSentimentTone") || tagName.equals("turingGL") || tagName.equals("turingON")
				|| tagName.equals("turingPN") || tagName.equals("turingSentimentSubj")
				|| tagName.equals("turingSimpleConcept"));
	}

	public static boolean isSinlgeValueTMETag(String tagName) {
		return (tagName.equals("turingSentimentTone") || tagName.equals("turingSentimentSubj"));
	}

	public static boolean isIndexed(ContentInstance mo, IHandlerConfiguration config) {
		try {
			HttpClient httpclient = new HttpClient();
			GetMethod get = new GetMethod(config.getTuringURL() + "/solr/" + config.getIndex() + "/select/?q=id%3A"
					+ mo.getContentManagementId().getId());
			get.setRequestHeader("Accept", "*/*");
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("executing query:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP result is: " + result);
				log.debug("Viglet Turing indexer response HTTP response body is: " + get.getResponseBodyAsString());
			}
			if (result == 200) {
				if (!"numFound=\"0\"".equals(get.getResponseBodyAsString())) {
					return true;
				}
			}
			get.releaseConnection();
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
		return false;
	}

	public static boolean isIndexed(ExternalResourceObject mo, IHandlerConfiguration config) {
		try {
			HttpClient httpclient = new HttpClient();
			GetMethod get = new GetMethod(
					config.getTuringURL() + "/solr/" + config.getIndex() + "/select/?q=id%3A" + mo.getId());
			get.setRequestHeader("Accept", "*/*");
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("executing query:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP result is: " + result);
				log.debug("Viglet Turing indexer response HTTP response body is: " + get.getResponseBodyAsString());
			}
			if (result == 200) {
				if (!"numFound=\"0\"".equals(get.getResponseBodyAsString())) {
					return true;
				}
			}
			get.releaseConnection();
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
		return false;
	}
}
