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
package com.viglet.turing.mappers;

import java.util.HashMap;

import com.viglet.turing.beans.TurCTDMappingMap;
import com.viglet.turing.beans.TurMiscConfigMap;
import com.vignette.logging.context.ContextLogger;

public class MappingDefinitions {
	private TurCTDMappingMap mappingDefinitions;
	private TurMiscConfigMap mscConfig;
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

	public MappingDefinitions(String mappingsXML,  TurCTDMappingMap mappingDefinitions,
			TurMiscConfigMap mscConfig) {
		if (log.isDebugEnabled()) {
			log.debug("initializing mapping definitions");
		}
		setMappingsXML(mappingsXML);
		setMappingDefinitions(mappingDefinitions);
		setMscConfig(mscConfig);
	}

	public TurCTDMappingMap getMappingDefinitions() {
		return mappingDefinitions;
	}

	public HashMap<String, String> getMscConfig() {
		return mscConfig;
	}

	public void setMappingDefinitions(TurCTDMappingMap mappingDefinitions) {
		this.mappingDefinitions = mappingDefinitions;
	}

	public void setMscConfig(TurMiscConfigMap mscConfig) {
		this.mscConfig = mscConfig;
	}
}
