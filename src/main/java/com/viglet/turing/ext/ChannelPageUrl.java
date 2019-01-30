package com.viglet.turing.ext;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.util.ETLTuringTranslator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class ChannelPageUrl implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(ChannelPageUrl.class);

	@Override
	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config)
			throws Exception {
		String url = "";
		ETLTuringTranslator etlTranslator = new ETLTuringTranslator(config);

		if (log.isDebugEnabled()) {
			log.debug("Executing ChannelPageUrl");
		}
		for (ManagedObjectVCMRef mo : ManagedObject.getReferringManagedObjects(ci.getContentManagementId())) {
			if (mo.getObjectTypeRef().getObjectType().getName().equals("Channel")) {
				url = etlTranslator.translateByGUID(mo.getId());
			}

		}
		if (log.isDebugEnabled()) {
			log.debug("ChannelPageUrl URL: " + url);
		}
		return url;
	}

	@Override
	public String consume(TuringTag tag, ExternalResourceObject ci, String attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing ChannelPageUrl");
		}
		return ci.getLink();
	}
}
