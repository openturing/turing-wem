package com.viglet.turing.ext;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.util.HtmlManipulator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class HTML2Text implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(HTML2Text.class);
	private static final String EMPTY_STRING = "";
	
	@Override
	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing HTML2Text");
		}
		
		if (attributeData != null && attributeData.getValue() != null) {
			return HtmlManipulator.Html2Text(attributeData.getValue().toString());
		}
		else {
			return EMPTY_STRING;
		}
	}

	@Override
	public String consume(TuringTag tag, ExternalResourceObject ci,
			String attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing HTML2Text");
		}
		return EMPTY_STRING;
	}
}
