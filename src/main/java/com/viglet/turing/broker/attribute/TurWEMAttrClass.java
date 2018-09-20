package com.viglet.turing.broker.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.ext.ExtAttributeInterface;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.util.HtmlManipulator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurWEMAttrClass {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMAttrXML.class);
	
	public static HashMap<String, List<String>> attributeByClass(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config) throws Exception {
		if (attributesDefs.get(tag.getTagName()) == null) {
			attributesDefs.put(tag.getTagName(), new ArrayList<String>());
		}
		if (tag.getSrcClassName() != null) {
			String className = tag.getSrcClassName();
			if (log.isDebugEnabled()) {
				log.debug("ClassName : " + className);
			}

			if (className != null) {
				Object extAttribute = Class.forName(className).newInstance();
				attributesDefs.get(tag.getTagName())
						.add(((ExtAttributeInterface) extAttribute).consume(tag, ci, attributeData, config));
			}
		} else {
			if (tag.getSrcAttributeType() != null && tag.getSrcAttributeType().equals("html")) {
				attributesDefs.get(tag.getTagName()).add(HtmlManipulator.Html2Text(attributeData.getValue().toString()));
			} else {

				if (attributeData != null && attributeData.getValue() != null) {
					attributesDefs.get(tag.getTagName()).add(attributeData.getValue().toString());
				}
			}
		}
		return attributesDefs;
	}
	
	public static HashMap<String, List<String>> attributeByClass(ExternalResourceObject ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, String attributeData,
			IHandlerConfiguration config) throws Exception {
		if (attributesDefs.get(tag.getTagName()) == null) {
			attributesDefs.put(tag.getTagName(), new ArrayList<String>());
		}
		if (tag.getSrcClassName() != null) {
			String className = tag.getSrcClassName();
			if (log.isDebugEnabled()) {
				log.debug("ClassName : " + className);
			}

			if (className != null) {
				Object extAttribute = Class.forName(className).newInstance();
				attributesDefs.get(tag.getTagName())
						.add(((ExtAttributeInterface) extAttribute).consume(tag, ci, attributeData, config));
			}
		} else {
			if (tag.getSrcAttributeType() != null && tag.getSrcAttributeType().equals("html")) {
				attributesDefs.get(tag.getTagName()).add(HtmlManipulator.Html2Text(attributeData));
			} else {

				if (attributeData != null) {
					attributesDefs.get(tag.getTagName()).add(attributeData);
				}
			}
		}
		return attributesDefs;
	}
}
