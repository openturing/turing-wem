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

public class TurWEMModificationDate implements ExtAttributeInterface {

	private static final ContextLogger log = ContextLogger.getLogger(TurWEMModificationDate.class);

	@Override
	public String consume(TuringTag tag, ContentInstance ci, AttributeData attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Start TurWEMOriginalDate");
		}
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
	 
		return ci.getLastModTime() != null ? df.format(ci.getLastModTime()) : df.format(ci.getCreationTime());
	}

	@Override
	public String consume(TuringTag tag, ExternalResourceObject ci, String attributeData, IHandlerConfiguration config)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Start TurWEMOriginalDate");
		}
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		
		return  ci.getLastModTime() != null ? df.format(ci.getLastModTime()) : df.format(ci.getCreationTime());
	}

}
