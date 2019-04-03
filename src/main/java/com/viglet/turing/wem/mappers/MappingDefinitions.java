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
package com.viglet.turing.wem.mappers;

import com.viglet.turing.wem.beans.TurCTDMappingMap;
import com.viglet.turing.wem.config.IHandlerConfiguration;
import com.viglet.turing.wem.index.IValidToIndex;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ObjectType;
import com.vignette.logging.context.ContextLogger;

public class MappingDefinitions {
	private TurCTDMappingMap mappingDefinitions;
	private String mappingsXML;
	private static final ContextLogger log = ContextLogger.getLogger(MappingDefinitions.class);

	public String getMappingsXML() {
		return mappingsXML;
	}

	public void setMappingsXML(String mappingsXML) {
		this.mappingsXML = mappingsXML;
	}

	public MappingDefinitions() {
	}

	public MappingDefinitions(String mappingsXML, TurCTDMappingMap mappingDefinitions) {
		if (log.isDebugEnabled()) {
			log.debug("initializing mapping definitions");
		}
		setMappingsXML(mappingsXML);
		setMappingDefinitions(mappingDefinitions);
	}

	public TurCTDMappingMap getMappingDefinitions() {
		return mappingDefinitions;
	}


	public void setMappingDefinitions(TurCTDMappingMap mappingDefinitions) {
		this.mappingDefinitions = mappingDefinitions;
	}

	public boolean hasContentType(String contentTypeName) {
		return mappingDefinitions.get(contentTypeName) == null ? false : true;
	}

	public boolean hasClassValidToIndex(String contentTypeName) {
		CTDMappings ctdMappings = mappingDefinitions.get(contentTypeName);
		boolean status = ctdMappings != null && ctdMappings.getClassValidToIndex() != null ? true : false;
		if (!status && log.isDebugEnabled())
			log.debug(String.format("Valid to Index className is not found in the mappingXML for the CTD: %s",
					contentTypeName));
		return status;
	}

	public IValidToIndex validToIndex(ObjectType ot, IHandlerConfiguration config) {
		
		try {
			String contentTypeName;
			contentTypeName = ot.getData().getName();

			if (this.hasClassValidToIndex(contentTypeName)) {
				CTDMappings ctdMappings = mappingDefinitions.get(contentTypeName);
				IValidToIndex instance = null;
				String className = ctdMappings.getClassValidToIndex();
				if (className != null) {
					Class<?> clazz = Class.forName(className);

					if (clazz == null) {
						if (log.isDebugEnabled())
							log.debug(String.format("Valid to Index className is not found in the jar file: %s",
									className));

					} else
						instance = (IValidToIndex) clazz.newInstance();
				}
				return instance;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public boolean isClassValidToIndex(ContentInstance ci, IHandlerConfiguration config) {		
		try {
			IValidToIndex iValidToIndex = validToIndex(ci.getObjectType(), config);
			return (iValidToIndex != null && !iValidToIndex.isValid(ci, config)) ? false : true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
