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
