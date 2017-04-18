package com.viglet.turing.ext;

import com.viglet.turing.beans.NsteinTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.util.ETLOTSNTranslator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class DPSUrl implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(DPSUrl.class);

	public String consume(NsteinTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing DPSUrl");
		}
		ETLOTSNTranslator etlTranslator = new ETLOTSNTranslator(config);
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
	public String consume(NsteinTag tag, ExternalResourceObject ci,
			String attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing DPSUrl");
		}
		return ci.getLink();
	}
}
