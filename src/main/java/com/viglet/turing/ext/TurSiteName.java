package com.viglet.turing.ext;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.util.ETLTuringTranslator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurSiteName implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(TurSiteName.class);

	@Override
	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing SiteName");
		}
		ETLTuringTranslator etlTranslator = new ETLTuringTranslator(config);

		return etlTranslator.getSiteName(ci);

	}

	@Override
	public String consume(TuringTag tag, ExternalResourceObject ci, String attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing SiteName");
		}
		return ci.getTypeName();
	}
}
