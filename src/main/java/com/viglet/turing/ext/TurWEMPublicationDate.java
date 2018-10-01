package com.viglet.turing.ext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurWEMPublicationDate implements ExtAttributeInterface {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMPublicationDate.class);

	@Override
	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Start TurWEMPublicationDate");
		}

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		String modDate = ci.getLastModTime() != null ? df.format(ci.getLastModTime()) : df.format(ci.getCreationTime());

		return ci.getLastPublishDate() != null ? df.format(ci.getLastPublishDate()) : modDate;
	}

	@Override
	public String consume(TuringTag tag, ExternalResourceObject ci, String attributeData, IHandlerConfiguration config)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Start TurWEMPublicationDate");
		}

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		String modDate = ci.getLastModTime() != null ? df.format(ci.getLastModTime()) : df.format(ci.getCreationTime());

		return ci.getLastPublishDate() != null ? df.format(ci.getLastPublishDate()) : modDate;
	}

}