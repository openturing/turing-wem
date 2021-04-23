package com.viglet.turing.wem.mappers;

import com.viglet.turing.wem.config.IHandlerConfiguration;
import com.viglet.turing.wem.index.IValidToIndex;
import com.viglet.turing.wem.mapping.MappingDefinition;
import com.viglet.turing.wem.mapping.MappingDefinitions;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ObjectType;
import com.vignette.logging.context.ContextLogger;

public class MappingDefinitionsUtils {
	private static final ContextLogger logger = ContextLogger.getLogger(MappingDefinitionsUtils.class);
	private MappingDefinitions mappingDefinitions;
	
	public MappingDefinitionsUtils (MappingDefinitions mappingDefinitions) {
		this.mappingDefinitions = mappingDefinitions;
	}
	public MappingDefinition getContentType(String contentTypeName) {
		MappingDefinition contentType = new MappingDefinition();
		
		for (MappingDefinition mappingDefinition : mappingDefinitions.getMappingDefinition()) {
			if (mappingDefinition.getContentType().equals(contentTypeName)) {
				contentType = mappingDefinition;
				break;
			}
		}
		
		return contentType;
	}
 
	public boolean hasContentType(String contentTypeName) {
		boolean hasContentTypeReturn = false;
		
		for (MappingDefinition mappingDefinition : mappingDefinitions.getMappingDefinition()) {
			hasContentTypeReturn = mappingDefinition.getContentType().equals(contentTypeName);
			if (hasContentTypeReturn)
				break;
		}
		
		return hasContentTypeReturn;
	}

	public boolean hasClassValidToIndex(String contentTypeName) {
		MappingDefinition contentType = getContentType(contentTypeName);
		boolean status = (contentType != null && contentType.getValidToIndex() != null);
		if (!status && logger.isDebugEnabled())
			logger.debug(String.format("Valid to Index className is not found in the mappingXML for the CTD: %s",
					contentTypeName));
		return status;
	}
	
	public IValidToIndex validToIndex(ObjectType ot, IHandlerConfiguration config) {

		try {
			String contentTypeName;
			contentTypeName = ot.getData().getName();

			if (this.hasClassValidToIndex(contentTypeName)) {
				MappingDefinition contentType = getContentType(contentTypeName);
				IValidToIndex instance = null;
				String className = contentType.getValidToIndex();
				if (className != null) {
					Class<?> clazz = Class.forName(className);

					if (clazz == null) {
						if (logger.isDebugEnabled())
							logger.debug(String.format("Valid to Index className is not found in the jar file: %s",
									className));

					} else
						instance = (IValidToIndex) clazz.newInstance();
				}
				return instance;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;

	}

	public boolean isClassValidToIndex(ContentInstance ci, IHandlerConfiguration config) {
		try {
			IValidToIndex iValidToIndex = validToIndex(ci.getObjectType(), config);
			return !(iValidToIndex != null && !iValidToIndex.isValid(ci, config));
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}
}
