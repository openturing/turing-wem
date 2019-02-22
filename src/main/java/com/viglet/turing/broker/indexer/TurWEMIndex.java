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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import com.viglet.turing.beans.TurCTDMappingMap;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.attribute.TurWEMAttrXML;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.exceptions.MappingNotFoundException;
import com.viglet.turing.index.IValidToIndex;
import com.viglet.turing.mappers.CTDMappings;
import com.viglet.turing.mappers.MappingDefinitions;
import com.viglet.turing.mappers.MappingDefinitionsProcess;
import com.viglet.turing.util.TuringUtils;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class TurWEMIndex {

	private static final ContextLogger log = ContextLogger.getLogger(TurWEMIndex.class);

	public static String generateXMLToIndex(ContentInstance ci, IHandlerConfiguration config) throws Exception {
		MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.getMappingDefinitions(config);
		if (log.isDebugEnabled()) {
			log.debug("Generating Viglet Turing XML for a content instance");
		}
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><document>");
		xml.append("<id>" + ci.getContentManagementId().getId() + "</id>");

		// we force the type on the Viglet Turing side
		TurCTDMappingMap mappings = mappingDefinitions.getMappingDefinitions();

		CTDMappings ctdMappings = mappings.get(ci.getObjectType().getData().getName());

		if (ctdMappings == null) {

			if (log.isDebugEnabled()) {
				log.debug("Mapping definition is not found in the mappingXML for the CTD: "
						+ ci.getObjectType().getData().getName());
			}
			throw new MappingNotFoundException("Mapping definition is not found in the mappingXML for the CTD: "
					+ ci.getObjectType().getData().getName());
		}

		HashMap<String, List<String>> attributesDefs = new HashMap<String, List<String>>();

		for (String key : ctdMappings.getIndexAttrs()) {
			for (TuringTag tag : ctdMappings.getIndexAttrTag(key)) {
				if (key != null && tag != null && tag.getTagName() != null) {

					if (log.isDebugEnabled()) {
						String debugRelation = tag.getSrcAttributeRelation() != null
								? TuringUtils.listToString(tag.getSrcAttributeRelation())
								: null;
						log.debug("Key: " + key + " Tag: " + tag.getTagName() + " relation: " + debugRelation
								+ " content Type: " + tag.getSrcAttributeType());
					}
					attributesDefs = TurWEMAttrXML.attributeXML(ci, attributesDefs, tag, key, config,
							mappingDefinitions);
				}
			}
		}

		// Create xml of attributesDefs
		for (Entry<String, List<String>> entry : attributesDefs.entrySet()) {
			String key = entry.getKey();
			List<String> listValue = entry.getValue();
			for (String value : listValue) {
				if ((value != null) && (value.toString().trim().length() > 0)) {
					xml.append("<" + key + "><![CDATA[" + value.toString() + "]]></" + key + ">");
				}
			}
		}

		String classifications[] = ci.getTaxonomyClassifications();
		if (classifications != null && classifications.length > 0) {
			for (int i = 0; i < classifications.length; i++) {
				String wemClassification = classifications[i].substring(classifications[i].lastIndexOf("/") + 1);
				xml.append("<categories>").append(wemClassification).append("</categories>");
			}
		}

		xml.append("</document>");

		if (log.isDebugEnabled()) {
			log.debug("Viglet Turing XML content: " + xml.toString());
		}

		return xml.toString();

	}

	// This method post the content to the Viglet Turing broker
	public static boolean indexCreate(ManagedObject mo, IHandlerConfiguration config) {

		MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.getMappingDefinitions(config);
		boolean success = false;
		if (mappingDefinitions == null || !mappingDefinitions.getMappingsXML().equals(config.getMappingsXML())) {
			mappingDefinitions = MappingDefinitionsProcess.loadMappings(config.getMappingsXML());
			if (mappingDefinitions == null) {

				if (log.isDebugEnabled()) {
					log.error(
							"Mapping definitions are not loaded properly from mappingsXML: " + config.getMappingsXML());
				}

				return false;
			}
		}

		if ((mo != null) && (mo instanceof ContentInstance)) {
			try {

				if (mappingDefinitions.getMappingDefinitions().get(mo.getObjectType().getData().getName()) != null) {

					log.info(
							"Viglet Turing indexer Processing Content Type: " + mo.getObjectType().getData().getName());
					if (log.isDebugEnabled()) {
						log.debug("Viglet Turing indexer Processing Content Type: "
								+ mo.getObjectType().getData().getName());
					}

					String className = getClassValidToIndex(mo.getObjectType().getData().getName(), config);
					IValidToIndex instance = null;
					if (className != null) {
						Class<?> clazz = Class.forName(className);
						if (clazz == null) {
							if (log.isDebugEnabled()) {
								log.debug("Valid to Index className is not found in the jar file: " + className);
							}
						} else {
							instance = (IValidToIndex) clazz.newInstance();
						}
					}
					if (instance != null && !instance.isValid((ContentInstance) mo, config)) {					
						return false;
					}
					postIndex(generateXMLToIndex((ContentInstance) mo, config), config);
					success = true;

				} else {
					if (log.isDebugEnabled()) {
						log.debug("Mapping definition is not found in the mappingXML for the CTD: "
								+ mo.getObjectType().getData().getName());
						log.debug("Viglet Turing indexer Ingnoring Content Type: "
								+ mo.getObjectType().getData().getName());
					}
				}

			} catch (Exception e) {
				log.error("Can't CREATE to Viglet Turing indexer: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return success;
	}

	public static String getClassValidToIndex(String objectTypeName, IHandlerConfiguration config) {
		HashMap<String, CTDMappings> mappings = MappingDefinitionsProcess.getMappingDefinitions(config)
				.getMappingDefinitions();
		CTDMappings ctdMappings = mappings.get(objectTypeName);
		if (ctdMappings.getClassValidToIndex() == null) {
			if (log.isDebugEnabled()) {
				log.debug("Valid to Index className is not found in the mappingXML for the CTD: " + objectTypeName);
			}
			return null;
		}
		return ctdMappings.getClassValidToIndex();
	}

	public static void postIndex(String xml, IHandlerConfiguration config) throws HttpException, IOException {

		PostMethod post = new PostMethod(
				config.getTuringURL() + "/?index=" + config.getIndex() + "&config=" + config.getConfig());

		post.setParameter("data", xml);
		post.setParameter("index", config.getIndex());
		post.setParameter("config", config.getConfig());
		post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		post.setRequestHeader("Accept-Encoding", "UTF-8");
		HttpClient httpclient = new HttpClient();
		int result = httpclient.executeMethod(post);

		if (log.isDebugEnabled()) {
			log.debug("Viglet Turing Index Request URI:" + post.getURI());
			log.debug("Using the index:" + config.getIndex() + ", config:" + config.getConfig());
			log.debug("XML:" + xml);
			log.debug(
					"Viglet Turing indexer response HTTP result is: " + result + ", for request uri:" + post.getURI());
			log.debug("Viglet Turing indexer response HTTP result is: " + post.getResponseBodyAsString());
		}
		post.releaseConnection();
	}
}
