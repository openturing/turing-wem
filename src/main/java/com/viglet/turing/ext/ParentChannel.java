package com.viglet.turing.ext;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.ref.ChannelRef;
import com.vignette.as.client.common.ref.SiteRef;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;


public class ParentChannel implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(ParentChannel.class);

	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config) throws Exception {

		String cdaContextName = "/" + config.getCDAContextName() + "/";
		String cdaServer = config.getCDAServer() + ":";
		String cdaPort = config.getCDAPort();

		Channel firstChannel;
		ChannelRef[] fcref = ci.getChannelAssociations();
		String chFurlName = "";
		String siteNameAssociated = "";
		String moFurlName = "";
		StringBuffer channelPath = new StringBuffer();

		if (fcref.length > 0) {
			SiteRef[] sref = fcref[0].getChannel().getSiteRefs();
			siteNameAssociated = sref[0].getSite().getName();
			firstChannel = fcref[0].getChannel();

			Channel[] breadcrumb = firstChannel.getBreadcrumbPath(true);
			for (int j = 0; j < breadcrumb.length; j++) {
				if (j > 0) {
					channelPath.append("/" + breadcrumb[j].getFurlName());
				}
			}
			channelPath.append("/");
			chFurlName = channelPath.toString();

		}
		moFurlName = chFurlName.replaceAll("-", "–").replaceAll(" ", "-");
		return "http://" + cdaServer + cdaPort + cdaContextName
				+ siteNameAssociated.replaceAll("-", "–").replaceAll(" ", "-") + moFurlName;
	}

	@Override
	public String consume(TuringTag tag, ExternalResourceObject ci,
			String attributeData, IHandlerConfiguration config)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
