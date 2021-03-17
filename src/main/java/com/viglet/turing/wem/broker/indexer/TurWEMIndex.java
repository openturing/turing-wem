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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.viglet.turing.wem.broker.indexer.ssl.TSLSocketConnectionFactory;

import com.viglet.turing.wem.beans.TurAttrDef;
import com.viglet.turing.wem.beans.TurAttrDefContext;
import com.viglet.turing.wem.beans.TurCTDMappingMap;
import com.viglet.turing.wem.beans.TurMultiValue;
import com.viglet.turing.wem.beans.TuringTag;
import com.viglet.turing.wem.broker.attribute.TurWEMAttrXML;
import com.viglet.turing.wem.config.IHandlerConfiguration;
import com.viglet.turing.wem.mappers.CTDMappings;
import com.viglet.turing.wem.mappers.MappingDefinitions;
import com.viglet.turing.wem.mappers.MappingDefinitionsProcess;
import com.viglet.turing.wem.util.TuringUtils;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

import javax.net.ssl.HttpsURLConnection;

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
					if (mappingDefinitions.hasClassValidToIndex(mo.getObjectType().getData().getName())
							&& mo.getContentManagementId() != null && mo.getContentManagementId().getId() != null) {
						String GUID = mo.getContentManagementId().getId();
						TurWEMDeindex.indexDelete(GUID, config);
					}
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

		List<TurAttrDef> attributesDefs = new ArrayList<TurAttrDef>();

		for (String tag : ctdMappings.getTagList()) {
			if (log.isDebugEnabled()) {
				log.debug("generateXMLToIndex: TagList");
				for (String tags : ctdMappings.getTagList()) {
					log.debug("generateXMLToIndex: Tags: " + tags);
				}
			}
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
					List<TurAttrDef> attributeDefsXML = TurWEMAttrXML.attributeXML(turAttrDefContext);

					// Unique
					if (turingTag.isSrcUniqueValues()) {

						TurMultiValue multiValue = new TurMultiValue();
						for (TurAttrDef turAttrDef : attributeDefsXML) {
							for (String singleValue : turAttrDef.getMultiValue()) {
								if (!multiValue.contains(singleValue)) {
									multiValue.add(singleValue);
								}
							}
						}
						TurAttrDef turAttrDefUnique = new TurAttrDef(turingTag.getTagName(), multiValue);
						attributesDefs.add(turAttrDefUnique);
					} else {
						attributesDefs.addAll(attributeDefsXML);
					}
				}
			}
		}

		// Create xml of attributesDefs
		for (TurAttrDef turAttrDef : attributesDefs) {
			if (log.isDebugEnabled()) {
				log.debug("AttributeDef - TagName: " + turAttrDef.getTagName());
				for (String string : turAttrDef.getMultiValue()) {
					log.debug("AttributeDef - Value: " + string);
				}

			}

			for (String value : turAttrDef.getMultiValue()) {
				if ((value != null) && (value.toString().trim().length() > 0))
					xml.append(createXMLAttribute(turAttrDef.getTagName(), value.toString()));
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

	public static boolean postIndex(String xml, IHandlerConfiguration config){
		
		try {

			URL url = new URL(config.getTuringURL() + "/?index=" + config.getIndex() + "&config=" + config.getConfig());
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

			httpsURLConnection.setSSLSocketFactory(new TSLSocketConnectionFactory());
			httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			httpsURLConnection.setRequestProperty("Accept-Encoding", "UTF-8");

			TuringUtils.basicAuth(config, httpsURLConnection);

			Map<String, Object> params = new LinkedHashMap<String, Object>();
			params.put("data", xml);
			params.put("index", config.getIndex());
			params.put("config", config.getConfig());

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			try {
				httpsURLConnection.setRequestMethod("POST");
				httpsURLConnection.setDoOutput(true);
				httpsURLConnection.getOutputStream().write(postDataBytes);
				int result = httpsURLConnection.getResponseCode();
				
				if (log.isDebugEnabled()) {
					log.debug(String.format("Viglet Turing Index Request URI: %s", httpsURLConnection.getURL()));
					log.debug(String.format("Using the index: %s, config: %s", config.getIndex(), config.getConfig()));
					log.debug(String.format("XML: %s", xml));
					log.debug(String.format("Viglet Turing indexer response HTTP result is: %s, for request uri: %s",
							result, httpsURLConnection.getURL()));
					log.debug(String.format("Viglet Turing indexer response HTTP result is: %s",
							TuringUtils.getResponseBody(httpsURLConnection, result)));
				}
				log.info(String.format("Viglet Turing indexer Processed Content Type:"));

				return true;
			} finally {
				httpsURLConnection.disconnect();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String createXMLAttribute(String tag, String value) {
		return String.format("<%1$s><![CDATA[%2$s]]></%1$s>", tag, value.toString());
	}

}
