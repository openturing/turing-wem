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
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import com.viglet.turing.beans.TurAttrDefContext;
import com.viglet.turing.beans.TurAttrDefMap;
import com.viglet.turing.beans.TurCTDMappingMap;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.attribute.TurWEMAttrXML;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.mappers.CTDMappings;
import com.viglet.turing.mappers.MappingDefinitions;
import com.viglet.turing.mappers.MappingDefinitionsProcess;
import com.viglet.turing.util.TuringUtils;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class TurWEMIndex {

	private static final ContextLogger log = ContextLogger.getLogger(TurWEMIndex.class);

	public static boolean indexCreate(ManagedObject mo, IHandlerConfiguration config) {
		MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.getMappingDefinitions(config);
		if ((mappingDefinitions != null) && (mo != null) && (mo instanceof ContentInstance)) {
			try {
				ContentInstance contentInstance = (ContentInstance) mo;
				String contentTypeName = contentInstance.getObjectType().getData().getName();

				if (mappingDefinitions.isClassValidToIndex(contentInstance, config)) {
					log.info(String.format("Viglet Turing indexer Processing Content Type: %s", contentTypeName));
					return postIndex(generateXMLToIndex(contentInstance, config), config);

				} else {
					if (log.isDebugEnabled())
						log.debug(String.format(
								"Mapping definition is not found in the mappingXML for the CTD and ignoring: %s",
								contentTypeName));
				}
			} catch (Exception e) {
				log.error(String.format("Can't Create to Viglet Turing indexer: %s", e.getMessage()));
				e.printStackTrace();
			}
		}
		return false;
	}

	// Generate XML To Index By ContentInstance
	public static String generateXMLToIndex(ContentInstance ci, IHandlerConfiguration config) throws Exception {
		MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.getMappingDefinitions(config);
		if (log.isDebugEnabled())
			log.debug("Generating Viglet Turing XML for a content instance");

		String contentTypeName = ci.getObjectType().getData().getName();

		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><document>");
		xml.append(createXMLAttribute("id", ci.getContentManagementId().getId()));

		TurCTDMappingMap mappings = mappingDefinitions.getMappingDefinitions();

		CTDMappings ctdMappings = mappings.get(contentTypeName);

		if (ctdMappings == null && log.isErrorEnabled())
			log.error(String.format("Mapping definition is not found in the mappingXML for the CTD: %s",
					contentTypeName));

		TurAttrDefMap attributesDefs = new TurAttrDefMap();

		for (String tag : ctdMappings.getTagList()) {
			for (TuringTag turingTag : ctdMappings.getTuringTagMap().get(tag)) {
				if (tag != null && turingTag != null && turingTag.getTagName() != null) {

					if (log.isDebugEnabled()) {
						String debugRelation = turingTag.getSrcAttributeRelation() != null
								? TuringUtils.listToString(turingTag.getSrcAttributeRelation())
								: null;
						log.debug(String.format("Tag:  %s, relation: %s, content Type: %s", turingTag.getTagName(),
								debugRelation, turingTag.getSrcAttributeType()));
					}

					TurAttrDefContext turAttrDefContext = new TurAttrDefContext(ci, turingTag, config,
							mappingDefinitions);

					attributesDefs = TurWEMAttrXML.attributeXML(turAttrDefContext);
				}
			}
		}

		// Create xml of attributesDefs
		for (Entry<String, List<String>> entry : attributesDefs.entrySet()) {
			String key = entry.getKey();
			List<String> listValue = entry.getValue();
			for (String value : listValue) {
				if ((value != null) && (value.toString().trim().length() > 0))
					xml.append(createXMLAttribute(key, value.toString()));
			}
		}

		String classifications[] = ci.getTaxonomyClassifications();
		if (classifications != null && classifications.length > 0) {
			for (int i = 0; i < classifications.length; i++) {
				String wemClassification = classifications[i].substring(classifications[i].lastIndexOf("/") + 1);
				xml.append(createXMLAttribute("categories", wemClassification));
			}
		}

		xml.append("</document>");

		if (log.isDebugEnabled())
			log.debug(String.format("Viglet Turing XML content: %s", xml.toString()));

		return xml.toString();

	}

	public static boolean postIndex(String xml, IHandlerConfiguration config) throws HttpException, IOException {

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
			log.debug(String.format("Viglet Turing Index Request URI: %s", post.getURI()));
			log.debug(String.format("Using the index: %s, config: %s", config.getIndex(), config.getConfig()));
			log.debug(String.format("XML: %s", xml));
			log.debug(String.format("Viglet Turing indexer response HTTP result is: %s, for request uri: %s", result,
					post.getURI()));
			log.debug(
					String.format("Viglet Turing indexer response HTTP result is: %s", post.getResponseBodyAsString()));
		}
		post.releaseConnection();

		return true;
	}

	private static String createXMLAttribute(String tag, String value) {
		return String.format("<%1$s><![CDATA[%2$s]]></%1$s>", tag, value.toString());
	}

}
