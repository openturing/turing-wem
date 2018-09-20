package com.viglet.turing.broker.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.mappers.MappingDefinitions;
import com.viglet.turing.util.ETLTuringTranslator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;

import com.vignette.logging.context.ContextLogger;

public class TurWEMUpdateFileWidget {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMUpdateFileWidget.class);

	public static HashMap<String, List<String>> attributeFileWidgetUpdate(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config, MappingDefinitions mappingDefinitions) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("TurWEMUpdateFileWidget started");
		}
		ETLTuringTranslator etlTranslator = new ETLTuringTranslator(config);

		if (tag.getSrcClassName() == null) {
			if (attributesDefs.get(tag.getTagName()) == null) {
				attributesDefs.put(tag.getTagName(), new ArrayList<String>());
			}

			String url = etlTranslator.getSiteDomain(ci) + attributeData.getValue().toString();
			if (log.isDebugEnabled()) {
				log.debug("TurWEMUpdateFileWidget url" + url);
			}
			attributesDefs.get(tag.getTagName()).add(url);

		}

		if (log.isDebugEnabled()) {
			log.debug("TurWEMUpdateFileWidget finished");
		}

		return attributesDefs;

	}
}
