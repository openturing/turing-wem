package com.viglet.turing.ext;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.util.ETLTuringTranslator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class DPSUrl implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(DPSUrl.class);
	
	@Override
	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config)
			throws Exception {
		ETLTuringTranslator etlTranslator = new ETLTuringTranslator(config);
		
		if (log.isDebugEnabled()) {
			log.debug("Executing DPSUrl");
		}
		
		String attribContent = null;
		if (attributeData != null) {
			attribContent = attributeData.getValue().toString();
		}
		if (attribContent == null) {
			return etlTranslator.translateByGUID(ci.getContentManagementId().getId());
		} else {
			if (attribContent.toLowerCase().startsWith("http")) {
				return attribContent;
			} else {
				return etlTranslator.translate(attribContent);

			}
		}

	}

	@Override
	public String consume(TuringTag tag, ExternalResourceObject ci,
			String attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing DPSUrl");
		}
		return ci.getLink();
	}
}
