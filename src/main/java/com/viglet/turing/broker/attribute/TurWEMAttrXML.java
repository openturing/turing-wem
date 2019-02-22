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
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.relator.TurWEMRelator;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.mappers.MappingDefinitions;
import com.viglet.turing.util.TuringUtils;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurWEMAttrXML {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMAttrXML.class);

	public static HashMap<String, List<String>> attributeXML(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, IHandlerConfiguration config,
			MappingDefinitions mappingDefinitions) throws Exception {
		// Relator
		if (tag.getSrcAttributeRelation() != null && tag.getSrcAttributeRelation().size() > 0) {
			AttributedObject[] relation = ci.getRelations(tag.getSrcAttributeRelation().get(0));

			if (tag.getSrcAttributeRelation().size() > 1) {
				log.debug("Attribute has nested relator");
				List<AttributedObject[]> nestedRelation = new ArrayList<AttributedObject[]>();
				nestedRelation.add(relation);
				relation = TurWEMRelator.nestedRelators(tag.getSrcAttributeRelation(), nestedRelation, 0);
			}

			if (relation != null) {
				List<String> listAttributeValues = new ArrayList<String>();
				for (int i = 0; i < relation.length; i++) {
					if (relation[i].getAttributeValue(key) != null) {
						String attributeValue = relation[i].getAttributeValue(key).toString();
						AttributeData attributeData = relation[i].getAttribute(key);
						if (log.isDebugEnabled()) {
							log.debug("Key : " + key + " Value: " + attributeValue);
						}
						if (attributeValue != null && !attributeValue.trim().equals("")) {
							attributesDefs = attributeXMLUpdate(ci, attributesDefs, tag, key, attributeData, config,
									mappingDefinitions);
						}
					}

				}
				if (tag.isSrcUniqueValues()) {
					if (attributesDefs.get(tag.getTagName()) != null) {
						for (String item : attributesDefs.get(tag.getTagName())) {
							if (!listAttributeValues.contains(item)) {
								listAttributeValues.add(item);
							}
						}
						attributesDefs.put(tag.getTagName(), listAttributeValues);
					}
				}
			}
		} else { // Normal attribute without relation

			if (ci.getAttributeValue(key) != null && !ci.getAttributeValue(key).toString().trim().equals("")) {
				AttributeData attributeData = ci.getAttribute(key);
				attributesDefs = attributeXMLUpdate(ci, attributesDefs, tag, key, attributeData, config,
						mappingDefinitions);
			} else if (tag.getSrcClassName() != null
					&& (tag.getSrcAttribute().startsWith("CLASSNAME_") || ci.getAttributeValue(key) != null)) {			
				attributesDefs = TurWEMAttrClass.attributeByClass(ci, attributesDefs, tag, key, null, config);
			}
		}
		return attributesDefs;
	}

	public static HashMap<String, List<String>> attributeXMLUpdate(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config, MappingDefinitions mappingDefinitions) throws Exception {

		if (log.isDebugEnabled()) {
			if (attributeData != null) {
				log.debug(tag.getTagName() + " = " + attributeData.getValue().toString());
			}
		}
		// Semantic Attributes
		if (attributeData != null && attributeData.getValue().toString() != null
				&& !attributeData.getValue().toString().trim().equals("")) {
			if (TuringUtils.isTuringTag(tag.getTagName())) {
				List<String> listAttributeValues = new ArrayList<String>();
				StringTokenizer tokenizer = new StringTokenizer(attributeData.getValue().toString(), ",");
				while (tokenizer.hasMoreTokens()) {
					if (attributesDefs.get(tag.getTagName()) == null) {
						attributesDefs.put(tag.getTagName(), new ArrayList<String>());
					}
					String token = tokenizer.nextToken();
					if (!listAttributeValues.contains(token)) {
						listAttributeValues.add(token);
					}
					if (TuringUtils.isSinlgeValueTMETag(tag.getTagName())) {
						// consider only the first value
						break;
					}
				}
				attributesDefs.put(tag.getTagName(), listAttributeValues);
			} else {
				attributesDefs = TurWEMAttrWidget.attributeByWidget(ci, attributesDefs, tag, key, attributeData, config,
						mappingDefinitions);
			}
		}

		return attributesDefs;
	}

}
