package com.viglet.turing.ext;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public interface ExtAttributeInterface {
	static final ContextLogger log = ContextLogger.getLogger(ExtAttributeInterface.class);
	public String consume (TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config) throws Exception;
	public String consume (TuringTag tag, ExternalResourceObject ci, String attributeData, IHandlerConfiguration config) throws Exception;
}
