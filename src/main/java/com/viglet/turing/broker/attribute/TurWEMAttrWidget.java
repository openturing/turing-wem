package com.viglet.turing.broker.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.relator.TurWEMRelator;
import com.viglet.turing.broker.update.TurWEMUpdateContentSelectWidget;
import com.viglet.turing.broker.update.TurWEMUpdateFileWidget;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.mappers.MappingDefinitions;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurWEMAttrWidget {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMAttrWidget.class);

	public static HashMap<String, List<String>> attributeByWidget(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config, MappingDefinitions mappingDefinitions) throws Exception {

		String widgetName = null;

		if (tag.getSrcAttributeRelation() != null && tag.getSrcAttributeRelation().size() > 0) {
			AttributedObject[] relation = ci.getRelations(tag.getSrcAttributeRelation().get(0));

			if (tag.getSrcAttributeRelation().size() > 1) {
				List<AttributedObject[]> nestedRelation = new ArrayList<AttributedObject[]>();
				nestedRelation.add(relation);
				relation = TurWEMRelator.nestedRelators(tag.getSrcAttributeRelation(), nestedRelation, 0);
			}

			if (relation.length > 0) {
				widgetName = relation[0].getAttribute(key).getAttributeDefinition().getWidgetName();
			}

		} else {
			widgetName = ci.getAttribute(key).getAttributeDefinition().getWidgetName();
		}

		if (widgetName != null && widgetName.equals("WCMContentSelectWidget")
				&& attributeData.getValue().toString().length() == 40 && tag.getSrcClassName() == null) {
			if (log.isDebugEnabled()) {
				log.debug("WCMContentSelectWidget value: " + attributeData.getValue().toString());
				log.debug("WCMContentSelectWidget length: " + attributeData.getValue().toString().length());
			}
			attributesDefs = TurWEMUpdateContentSelectWidget.attributeContentSelectUpdate(ci, attributesDefs, tag, key,
					attributeData, config, mappingDefinitions);

		} else if (widgetName != null && widgetName.equals("VCMFileWidget") && tag.getSrcClassName() == null) {
			if (log.isDebugEnabled()) {
				log.debug("VCMFileWidget value: " + attributeData.getValue().toString());
				log.debug("VCMFileWidget length: " + attributeData.getValue().toString().length());
			}
			attributesDefs = TurWEMUpdateFileWidget.attributeFileWidgetUpdate(ci, attributesDefs, tag, key,
					attributeData, config, mappingDefinitions);
		} else {
			attributesDefs = TurWEMAttrClass.attributeByClass(ci, attributesDefs, tag, key, attributeData, config);
		}
		return attributesDefs;
	}
}
