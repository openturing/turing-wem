package com.viglet.turing.broker.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.relator.TurWEMRelator;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.mappers.MappingDefinitions;
import com.viglet.turing.util.TuringUtils;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurWEMAttrXML {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMAttrXML.class);

	public static HashMap<String, List<String>> attributeXML(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, IHandlerConfiguration config,  MappingDefinitions mappingDefinitions)
			throws Exception {
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
							attributesDefs = attributeXMLUpdate(ci, attributesDefs, tag, key, attributeData, config, mappingDefinitions);
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
				attributesDefs = attributeXMLUpdate(ci, attributesDefs, tag, key, attributeData, config, mappingDefinitions);
			} else if (tag.getSrcClassName() != null) {
				attributesDefs = TurWEMAttrClass.attributeByClass(ci, attributesDefs, tag, key, null, config);
			}
		}
		return attributesDefs;
	}
	@SuppressWarnings("unchecked")
	public static HashMap<String, List<String>> attributeXML(ExternalResourceObject ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, IHandlerConfiguration config)
			throws Exception {

		// Relator
		if (tag.getSrcAttributeRelation() != null && tag.getSrcAttributeRelation().size() > 0) {
			List<ExternalResourceObject> relation = (List<ExternalResourceObject>) ci
					.get(tag.getSrcAttributeRelation().get(0));

			if (relation != null) {
				List<String> listAttributeValues = new ArrayList<String>();
				for (int i = 0; i < relation.size(); i++) {
					if (relation.get(i).get(key) != null) {
						String attributeValue = String.valueOf(relation.get(i).get(key));
						if (log.isDebugEnabled()) {
							log.debug("Key : " + key + " Value: " + attributeValue);
						}
						if (attributeValue != null && !attributeValue.trim().equals("")) {
							attributesDefs = attributeXMLUpdate(ci, attributesDefs, tag, key, attributeValue, config);
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

			if (ci.get(key) != null && !ci.get(key).toString().trim().equals("")) {
				String attributeValue = ci.get(key).toString();
				attributesDefs = attributeXMLUpdate(ci, attributesDefs, tag, key, attributeValue, config);
			} else if (tag.getSrcClassName() != null) {
				attributesDefs = TurWEMAttrClass.attributeByClass(ci, attributesDefs, tag, key, null, config);
			}
		}
		return attributesDefs;
	}
	
	public static HashMap<String, List<String>> attributeXMLUpdate(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config,  MappingDefinitions mappingDefinitions) throws Exception {

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
				attributesDefs = TurWEMAttrWidget.attributeByWidget(ci, attributesDefs, tag, key, attributeData, config, mappingDefinitions);
			}
		}

		return attributesDefs;
	}
	
	public static HashMap<String, List<String>> attributeXMLUpdate(ExternalResourceObject ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, String attributeData,
			IHandlerConfiguration config) throws Exception {

		if (log.isDebugEnabled()) {
			if (attributeData != null) {
				log.debug(tag.getTagName() + " = " + attributeData);
			}
		}
		// Semantic Attributes
		if (attributeData != null && !attributeData.trim().equals("")) {
			if (TuringUtils.isTuringTag(tag.getTagName())) {
				StringTokenizer tokenizer = new StringTokenizer(attributeData, ",");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					attributesDefs.get(tag.getTagName()).add(token);
					if (TuringUtils.isSinlgeValueTMETag(tag.getTagName())) {
						// consider only the first value
						break;
					}
				}
			} else {
				attributesDefs = TurWEMAttrClass.attributeByClass(ci, attributesDefs, tag, key, attributeData, config);
			}
		}

		return attributesDefs;
	}

}
