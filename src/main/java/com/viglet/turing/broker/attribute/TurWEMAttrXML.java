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
package com.viglet.turing.broker.attribute;

import java.util.ArrayList;
import java.util.List;

import com.viglet.turing.beans.TurAttrDefContext;
import com.viglet.turing.beans.TurAttrDefMap;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.relator.TurWEMRelator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurWEMAttrXML {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMAttrXML.class);

	public static TurAttrDefMap attributeXML(TurAttrDefContext turAttrDefContext) throws Exception {
		TuringTag turingTag = turAttrDefContext.getTuringTag();
		if (turingTag.getSrcAttributeRelation() != null && turingTag.getSrcAttributeRelation().size() > 0)
			return addAttributeWithRelator(turAttrDefContext);
		else
			return addAttributeWithoutRelator(turAttrDefContext);
	}

	public static TurAttrDefMap attributeXMLUpdate(TurAttrDefContext turAttrDefContext, AttributeData attributeData)
			throws Exception {
		TuringTag turingTag = turAttrDefContext.getTuringTag();
		if (log.isDebugEnabled() && attributeData != null)
			log.debug(String.format("%s = %s", turingTag.getTagName(), attributeData.getValue().toString()));

		if (attributeData != null && attributeData.getValue().toString() != null
				&& attributeData.getValue().toString().trim().length() > 0)
			return TurWEMAttrWidget.attributeByWidget(turAttrDefContext, attributeData);

		return new TurAttrDefMap();
	}

	private static TurAttrDefMap addAttributeWithRelator(TurAttrDefContext turAttrDefContext) throws Exception {
		TuringTag turingTag = turAttrDefContext.getTuringTag();
		ContentInstance ci = turAttrDefContext.getContentInstance();
		String key = turAttrDefContext.getKey();
		AttributedObject[] relation = ci.getRelations(turingTag.getSrcAttributeRelation().get(0));
		TurAttrDefMap attributesDefs = new TurAttrDefMap();
		if (turingTag.getSrcAttributeRelation().size() > 1) {
			log.debug("Attribute has nested relator");
			List<AttributedObject[]> nestedRelation = new ArrayList<AttributedObject[]>();
			nestedRelation.add(relation);
			relation = TurWEMRelator.nestedRelators(turingTag.getSrcAttributeRelation(), nestedRelation, 0);
		}

		if (relation != null) {
			List<String> listAttributeValues = new ArrayList<String>();
			for (int i = 0; i < relation.length; i++) {
				if (relation[i].getAttributeValue(key) != null) {
					String attributeValue = relation[i].getAttributeValue(key).toString();
					AttributeData attributeData = relation[i].getAttribute(key);
					if (log.isDebugEnabled()) {
						log.debug(String.format("Key: %s,  Value: %s", key, attributeValue));
					}
					if (attributeValue != null && !attributeValue.trim().equals("")) {
						attributesDefs.putAll(attributeXMLUpdate(turAttrDefContext, attributeData));
					}
				}

			}
			if (turingTag.isSrcUniqueValues()) {
				if (attributesDefs.get(turingTag.getTagName()) != null) {
					for (String item : attributesDefs.get(turingTag.getTagName())) {
						if (!listAttributeValues.contains(item)) {
							listAttributeValues.add(item);
						}
					}
					attributesDefs.put(turingTag.getTagName(), listAttributeValues);
				}
			}
		}
		return attributesDefs;
	}

	private static TurAttrDefMap addAttributeWithoutRelator(TurAttrDefContext turAttrDefContext) throws Exception {
		TuringTag turingTag = turAttrDefContext.getTuringTag();
		ContentInstance ci = turAttrDefContext.getContentInstance();
		String key = turAttrDefContext.getKey();
		if (ci.getAttributeValue(key) != null && ci.getAttributeValue(key).toString().trim().length() > 0) {
			AttributeData attributeData = ci.getAttribute(key);
			return attributeXMLUpdate(turAttrDefContext, attributeData);
		} else if (turingTag.getSrcClassName() != null
				&& (turingTag.getSrcAttribute().startsWith("CLASSNAME_") || ci.getAttributeValue(key) != null)) {
			AttributeData attributeData = ci.getAttribute(key);
			return TurWEMAttrClass.attributeByClass(turAttrDefContext, attributeData);
		} else {
			return new TurAttrDefMap();
		}
	}

}
