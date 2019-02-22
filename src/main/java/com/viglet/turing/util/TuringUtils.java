package com.viglet.turing.util;

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
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TuringUtils {
	private static final ContextLogger log = ContextLogger.getLogger(TuringUtils.class);

	/**
	 * Returns the index Tag name
	 * 
	 * @param turingTag
	 * @return String
	 */
	public static String getIndexTagName(TuringTag turingTag) {
		return ((turingTag.getSrcAttribute() == null) && (turingTag.getSrcClassName() != null))
				? String.format("CLASSNAME_%s", turingTag.getTagName())
				: turingTag.getTagName();

	}

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
