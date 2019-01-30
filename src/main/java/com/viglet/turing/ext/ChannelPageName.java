package com.viglet.turing.ext;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class ChannelPageName implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(ChannelPageName.class);

	@Override
	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config)
			throws Exception {
		String name = "";
		if (log.isDebugEnabled()) {
			log.debug("Executing ChannelPageName");
		}
		for (ManagedObjectVCMRef mo : ManagedObject.getReferringManagedObjects(ci.getContentManagementId())) {
			if (mo.getObjectTypeRef().getObjectType().getName().equals("Channel")) {
				Channel channel = (Channel) mo.asManagedObjectRef().retrieveManagedObject();
				name = channel.getName();
			}

		}
		return name;
	}

	@Override
	public String consume(TuringTag tag, ExternalResourceObject ci, String attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing ChannelPageUrl");
		}
		return ci.getTypeName();
	}
}
