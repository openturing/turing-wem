/*
 * Copyright (C) 2016-2019 Alexandre Oliveira <alexandre.oliveira@viglet.com> 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viglet.turing.ext;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.ref.ChannelRef;
import com.vignette.as.client.common.ref.SiteRef;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;


public class TurParentChannel implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(TurParentChannel.class);

	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config) throws Exception {
		if (log.isDebugEnabled())
			log.debug("Executing TurParentChannel");

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
}
