package com.viglet.turing.mappers;

import java.util.HashMap;

import com.viglet.turing.broker.OTSN;
import com.vignette.logging.context.ContextLogger;

/**
 * Created by IntelliJ IDEA.
 * User: spinnama
 * Date: Feb 3, 2011
 * Time: 11:38:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class MappingDefinitions {
    private HashMap<String,CTDMappings> mappingDefinitions;
    private HashMap<String,String> mscConfig;
    private String mappingsXML;
 	private static final ContextLogger log = ContextLogger.getLogger(OTSN.class);

    public String getMappingsXML() {
        return mappingsXML;
    }

    public void setMappingsXML(String mappingsXML) {
        this.mappingsXML = mappingsXML;
    }

    public MappingDefinitions() {
    }

    public MappingDefinitions(String mappingsXML,HashMap<String, CTDMappings> mappingDefinitions, HashMap<String, String> mscConfig) {
		if (log.isDebugEnabled()) {
			log.debug("initializing mapping definitions");
		}
    	setMappingsXML(mappingsXML);
        setMappingDefinitions(mappingDefinitions);
        setMscConfig(mscConfig);
    }

    public HashMap<String, CTDMappings> getMappingDefinitions() {
        return mappingDefinitions;
    }

    public HashMap<String, String> getMscConfig() {
        return mscConfig;
    }

    public void setMappingDefinitions(HashMap<String, CTDMappings> mappingDefinitions) {
        this.mappingDefinitions = mappingDefinitions;
    }

    public void setMscConfig(HashMap<String, String> mscConfig) {
        this.mscConfig = mscConfig;
    }
}
