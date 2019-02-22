package com.viglet.turing.beans;

import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.mappers.MappingDefinitions;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.javabean.ContentInstance;

public class TurAttrDefContext {

	private ContentInstance contentInstance;
	private TuringTag turingTag;
	private String key;
	private IHandlerConfiguration iHandlerConfiguration;
	private MappingDefinitions mappingDefinitions;
	private AttributeData attributeData;

	public TurAttrDefContext(TurAttrDefContext turAttrDefContext) {
		this.setAttributeData(turAttrDefContext.getAttributeData());
		this.setContentInstance(turAttrDefContext.getContentInstance());
		this.setiHandlerConfiguration(turAttrDefContext.getiHandlerConfiguration());
		this.setKey(turAttrDefContext.getKey());
		this.setMappingDefinitions(turAttrDefContext.getMappingDefinitions());
		this.setTuringTag(turAttrDefContext.getTuringTag());
	}

	public TurAttrDefContext(ContentInstance contentInstance, TuringTag turingTag, String key,
			IHandlerConfiguration iHandlerConfiguration, MappingDefinitions mappingDefinitions) {
		this.setContentInstance(contentInstance);
		this.setiHandlerConfiguration(iHandlerConfiguration);
		this.setKey(key);
		this.setMappingDefinitions(mappingDefinitions);
		this.setTuringTag(turingTag);
	}

	public ContentInstance getContentInstance() {
		return contentInstance;
	}

	public void setContentInstance(ContentInstance contentInstance) {
		this.contentInstance = contentInstance;
	}

	public TuringTag getTuringTag() {
		return turingTag;
	}

	public void setTuringTag(TuringTag turingTag) {
		this.turingTag = turingTag;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public IHandlerConfiguration getiHandlerConfiguration() {
		return iHandlerConfiguration;
	}

	public void setiHandlerConfiguration(IHandlerConfiguration iHandlerConfiguration) {
		this.iHandlerConfiguration = iHandlerConfiguration;
	}

	public MappingDefinitions getMappingDefinitions() {
		return mappingDefinitions;
	}

	public void setMappingDefinitions(MappingDefinitions mappingDefinitions) {
		this.mappingDefinitions = mappingDefinitions;
	}

	public AttributeData getAttributeData() {
		if (attributeData != null) {
			return attributeData;
		}

		try {
			return contentInstance.getAttribute(key);
		} catch (ApplicationException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void setAttributeData(AttributeData attributeData) {
		this.attributeData = attributeData;
	}
}
