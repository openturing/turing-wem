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
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import com.viglet.turing.broker.TurWEM;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
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

	// This method post the content to the Viglet Turing broker
	public static boolean indexCreate(ManagedObject mo, IHandlerConfiguration config) {
		
		MappingDefinitions mappingDefinitions = TurWEM.getMappingDefinitions(config);
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
						if (TuringUtils.isIndexed((ContentInstance) mo, config)) {
							TurWEMDeindex.indexDelete(mo.getContentManagementId().getId(), config);
						}
						return false;
					}
					postIndex(TurWEM.getXML((ContentInstance) mo, config), config);
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
		HashMap<String, CTDMappings> mappings = TurWEM.getMappingDefinitions(config).getMappingDefinitions();
		CTDMappings ctdMappings = mappings.get(objectTypeName);
		if (ctdMappings.getClassValidToIndex() == null) {
			if (log.isDebugEnabled()) {
				log.debug("Valid to Index className is not found in the mappingXML for the CTD: " + objectTypeName);
			}
			return null;
		}
		return ctdMappings.getClassValidToIndex();
	}

	public static boolean indexCreate(ExternalResourceObject mo, String typeName, IHandlerConfiguration config) {
		boolean success = false;
		try {
			log.info("Viglet Turing indexer Processing Content Type: " + typeName);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing indexer Processing Content Type: " + typeName);
			}

			String className = getClassValidToIndex(typeName, config);
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
			if (instance != null && !instance.isValid(mo, config)) {
				if (TuringUtils.isIndexed(mo, config)) {
					TurWEMDeindex.indexDelete(mo.getId(), config);
				}
				return false;
			}
			postIndex(TurWEM.getXML(mo, config), config);
			success = true;
		} catch (Exception e) {
			log.error("Can't CREATE to Viglet Turing indexer: " + e.getMessage());
			e.printStackTrace();
		}
		return success;
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
