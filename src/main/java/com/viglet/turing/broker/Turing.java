package com.viglet.turing.broker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.GenericResourceHandlerConfiguration;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.exceptions.MappingNotFoundException;
import com.viglet.turing.ext.ExtAttributeInterface;
import com.viglet.turing.index.ExternalResourceObject;
import com.viglet.turing.index.IValidToIndex;
import com.viglet.turing.mappers.CTDMappings;
import com.viglet.turing.mappers.MappingDefinitions;
import com.viglet.turing.util.HtmlManipulator;
import com.viglet.turing.util.TuringUtils;
import com.viglet.turing.util.XmlParserUtilities;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.ref.ChannelRef;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.client.javabean.Site;
import com.vignette.ext.templating.link.LinkBuilder;
import com.vignette.ext.templating.link.LinkSpec;
import com.vignette.ext.templating.util.ContentUtil;
import com.vignette.ext.templating.util.RequestContext;
import com.vignette.logging.context.ContextLogger;

public class Turing {

	private static MappingDefinitions mappingDefinitions = null;
	private static final ContextLogger log = ContextLogger.getLogger(Turing.class);

	private static boolean isTuringTag(String tagName) {
		return (tagName.equals("turingSentimentTone") || tagName.equals("turingGL") || tagName.equals("turingON")
				|| tagName.equals("turingPN") || tagName.equals("turingSentimentSubj")
				|| tagName.equals("turingSimpleConcept"));
	}

	private static boolean isSinlgeValueTMETag(String tagName) {
		return (tagName.equals("turingSentimentTone") || tagName.equals("turingSentimentSubj"));
	}

	public static String getXML(ContentInstance ci, IHandlerConfiguration config) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Generating Viglet Turing XML for a content instance");
		}
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><document>");
		xml.append("<id>" + ci.getContentManagementId().getId() + "</id>");

		// we force the type on the Viglet Turing side
		HashMap<String, CTDMappings> mappings = mappingDefinitions.getMappingDefinitions();

		CTDMappings ctdMappings = mappings.get(ci.getObjectType().getData().getName());

		if (ctdMappings == null) {

			if (log.isDebugEnabled()) {
				log.debug("Mapping definition is not found in the mappingXML for the CTD: "
						+ ci.getObjectType().getData().getName());
			}
			throw new MappingNotFoundException("Mapping definition is not found in the mappingXML for the CTD: "
					+ ci.getObjectType().getData().getName());
		}

		TuringTag typeTag = ctdMappings.findIndexTagInMappings("type");
		if (typeTag == null || ci.getAttributeValue(typeTag.getSrcAttribute()) == null
				|| ci.getAttributeValue(typeTag.getSrcAttribute()).toString().trim().equals("")) {
			xml.append("<type>" + ci.getObjectType().getData().getName() + "</type>");
		}

		HashMap<String, List<String>> attributesDefs = new HashMap<String, List<String>>();

		attributesDefs.put("headline", new ArrayList<String>());
		attributesDefs.put("text", new ArrayList<String>());
		attributesDefs.put("title", new ArrayList<String>());
		attributesDefs.put("url", new ArrayList<String>());

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
	
		String modDate = ci.getLastModTime() != null ? df.format(ci.getLastModTime())
				: df.format(ci.getCreationTime());
		String publishDate = ci.getLastPublishDate() != null ? df.format(ci.getLastPublishDate()) : modDate;
		xml.append(
				"<original_date>" + modDate + "</original_date><last_published>" + publishDate + "</last_published>");

		for (String key : ctdMappings.getIndexAttrs()) {
			for (TuringTag tag : ctdMappings.getIndexAttrTag(key)) {
				if (key != null && tag != null && tag.getTagName() != null) {

					if (log.isDebugEnabled()) {
						log.debug("Key: " + key + " Tag: " + tag.getTagName() + " relation: "
								+ TuringUtils.listToString(tag.getSrcAttributeRelation()) + " content Type: "
								+ tag.getSrcAttributeType());
					}
					attributesDefs = attributeXML(ci, attributesDefs, tag, key, config);
				}
			}
		}

		// Create xml of attributesDefs
		for (Entry<String, List<String>> entry : attributesDefs.entrySet()) {
			String key = entry.getKey();
			List<String> listValue = entry.getValue();
			for (String value : listValue) {
				if ((value != null) && (value.toString().trim().length() > 0)) {
					xml.append("<" + key + "><![CDATA[" + value.toString() + "]]></" + key + ">");
				}
			}
		}

		// Let's append the sections (channel path)
		ChannelRef[] cref = ci.getChannelAssociations();
		for (int i = 0; i < cref.length; i++) {
			xml.append("<section><![CDATA[");
			Channel currentChannel = cref[i].getChannel();
			String[] breadcrumb = currentChannel.getBreadcrumbNamePath(true);
			for (int j = 0; j < breadcrumb.length; j++) {
				xml.append("/" + breadcrumb[j]);
			}
			xml.append("]]></section>");

		}

		String classifications[] = ci.getTaxonomyClassifications();
		if (classifications != null && classifications.length > 0) {
			for (int i = 0; i < classifications.length; i++) {
				String wemClassification = classifications[i].substring(classifications[i].lastIndexOf("/") + 1);
				xml.append("<tmeCategories>").append(wemClassification).append("</tmeCategories>");
			}
		}

		xml.append("</document>");

		if (log.isDebugEnabled()) {
			log.debug("Viglet Turing XML content: " + xml.toString());
		}

		return xml.toString();

	}

	public static String getXML(ExternalResourceObject ci, IHandlerConfiguration config) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Generating Viglet Turing XML for a content instance");
		}
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><document>");
		xml.append("<id>" + ci.getId() + "</id>");

		// we force the type on the Viglet Turing side
		HashMap<String, CTDMappings> mappings = mappingDefinitions.getMappingDefinitions();

		CTDMappings ctdMappings = mappings.get(ci.getTypeName());

		if (ctdMappings == null) {

			if (log.isDebugEnabled()) {
				log.debug("Mapping definition is not found in the mappingXML for the CTD: " + ci.getTypeName());
			}
			throw new MappingNotFoundException(
					"Mapping definition is not found in the mappingXML for the CTD: " + ci.getTypeName());
		}

		TuringTag typeTag = ctdMappings.findIndexTagInMappings("type");
		if (typeTag == null || ci.get(typeTag.getSrcAttribute()) == null
				|| ci.get(typeTag.getSrcAttribute()).toString().trim().equals("")) {
			xml.append("<type>" + ci.getTypeName() + "</type>");
		}

		HashMap<String, List<String>> attributesDefs = new HashMap<String, List<String>>();

		attributesDefs.put("headline", new ArrayList<String>());
		attributesDefs.put("text", new ArrayList<String>());
		attributesDefs.put("title", new ArrayList<String>());
		attributesDefs.put("url", new ArrayList<String>());

		// for now considering the current date as published and original data
		// SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		// String currentDate = sdf.format(new Date());
		String modDate = ci.getLastModTime() != null ? sdf.format(ci.getLastModTime())
				: sdf.format(ci.getCreationTime());
		String publishDate = ci.getLastPublishDate() != null ? sdf.format(ci.getLastPublishDate()) : modDate;
		xml.append(
				"<original_date>" + modDate + "</original_date><last_published>" + publishDate + "</last_published>");

		for (String key : ctdMappings.getIndexAttrs()) {
			for (TuringTag tag : ctdMappings.getIndexAttrTag(key)) {
				if (key != null && tag != null && tag.getTagName() != null) {

					if (log.isDebugEnabled()) {
						log.debug("Key: " + key + " Tag: " + tag.getTagName() + " relation: "
								+ TuringUtils.listToString(tag.getSrcAttributeRelation()) + " content Type: "
								+ tag.getSrcAttributeType());
					}
					attributesDefs = attributeXML(ci, attributesDefs, tag, key, config);
				}
			}
		}

		// Create xml of attributesDefs
		for (Entry<String, List<String>> entry : attributesDefs.entrySet()) {
			String key = entry.getKey();
			List<String> listValue = entry.getValue();
			for (String value : listValue) {
				if ((value != null) && (value.toString().trim().length() > 0)) {
					xml.append("<" + key + "><![CDATA[" + value.toString() + "]]></" + key + ">");
				}
			}
		}

		// Let's append the sections (link)
		xml.append("<section><![CDATA[");
		xml.append("/");
		xml.append("]]></section>");

		xml.append("</document>");

		if (log.isDebugEnabled()) {
			log.debug("Viglet Turing XML content: " + xml.toString());
		}

		return xml.toString();

	}

	public static AttributedObject[] nestedRelators(List<String> relationTag, List<AttributedObject[]> currentRelation,
			int currentPosition) {
		List<AttributedObject> relators = new ArrayList<AttributedObject>();
		
		int nextPosition = currentPosition + 1;

		if (nextPosition < relationTag.size()) {
			List<AttributedObject[]> nestedRelationChild = new ArrayList<AttributedObject[]>();
			for (AttributedObject[] attributesFromRelation : currentRelation) {

				for (AttributedObject attributeFromRelation : Arrays.asList(attributesFromRelation)) {
					try {
						AttributedObject[] childRelation = attributeFromRelation
								.getRelations(relationTag.get(nextPosition));

						nestedRelationChild.add(childRelation);

					} catch (ApplicationException e) {
						log.error(String.format("Error getting relations: %s of relation: %s",
								relationTag.get(currentPosition), relationTag.get(currentPosition - 1)), e);
					}
				}
			}
			return nestedRelators(relationTag, nestedRelationChild, nextPosition);

		} else {
			for (AttributedObject[] attributesFromRelation : currentRelation) {
				relators.addAll(Arrays.asList(attributesFromRelation));
			}

			AttributedObject[] relatorsArr = new AttributedObject[relators.size()];
			relatorsArr = relators.toArray(relatorsArr);
			return relatorsArr;
		}

	}

	public static HashMap<String, List<String>> attributeXML(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, IHandlerConfiguration config)
			throws Exception {
		// Relator
		if (tag.getSrcAttributeRelation() != null && tag.getSrcAttributeRelation().size() > 0) {
			AttributedObject[] relation = ci.getRelations(tag.getSrcAttributeRelation().get(0));

			if (tag.getSrcAttributeRelation().size() > 1) {
				log.debug("Attribute has nested relator");
				List<AttributedObject[]> nestedRelation = new ArrayList<AttributedObject[]>();
				nestedRelation.add(relation);
				relation = nestedRelators(tag.getSrcAttributeRelation(), nestedRelation, 0);
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
							attributesDefs = attributeXMLUpdate(ci, attributesDefs, tag, key, attributeData, config);
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
				attributesDefs = attributeXMLUpdate(ci, attributesDefs, tag, key, attributeData, config);
			} else if (tag.getSrcClassName() != null) {
				attributesDefs = attributeByClass(ci, attributesDefs, tag, key, null, config);
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
				attributesDefs = attributeByClass(ci, attributesDefs, tag, key, null, config);
			}
		}
		return attributesDefs;
	}

	public static HashMap<String, List<String>> attributeByWidget(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config) throws Exception {

		String widgetName = null;

		if (tag.getSrcAttributeRelation() != null && tag.getSrcAttributeRelation().size() > 0) {
			AttributedObject[] relation = ci.getRelations(tag.getSrcAttributeRelation().get(0));

			if (tag.getSrcAttributeRelation().size() > 1) {
				List<AttributedObject[]> nestedRelation = new ArrayList<AttributedObject[]>();
				nestedRelation.add(relation);
				relation = nestedRelators(tag.getSrcAttributeRelation(), nestedRelation, 0);
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
			attributesDefs = attributeContentSelectUpdate(ci, attributesDefs, tag, key, attributeData, config);

		} else {
			attributesDefs = attributeByClass(ci, attributesDefs, tag, key, attributeData, config);
		}
		return attributesDefs;
	}

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
				attributesDefs.get(tag.getTagName()).add(Html2Text(attributeData.getValue().toString()));
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
				attributesDefs.get(tag.getTagName()).add(Html2Text(attributeData));
			} else {

				if (attributeData != null) {
					attributesDefs.get(tag.getTagName()).add(attributeData);
				}
			}
		}
		return attributesDefs;
	}

	public static HashMap<String, List<String>> attributeContentSelectUpdate(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config) throws Exception {

		ContentInstance ciRelated = (ContentInstance) ManagedObject
				.findByContentManagementId(new ManagedObjectVCMRef(attributeData.getValue().toString()));
		if (ciRelated != null) {
			if (log.isDebugEnabled()) {
				log.debug("CTD Related: " + ciRelated.getObjectType().getData().getName());
			}
			// we force the type on the Viglet Turing side
			HashMap<String, CTDMappings> relatedMappings = mappingDefinitions.getMappingDefinitions();

			CTDMappings ctdRelatedMappings = relatedMappings.get(ciRelated.getObjectType().getData().getName());

			if (ctdRelatedMappings == null) {

				if (log.isDebugEnabled()) {
					log.debug("Mapping definition is not found in the mappingXML for the CTD: "
							+ ciRelated.getObjectType().getData().getName());
				}
				throw new MappingNotFoundException("Mapping definition is not found in the mappingXML for the CTD: "
						+ ciRelated.getObjectType().getData().getName());
			}

			for (String keyRelated : ctdRelatedMappings.getIndexAttrs()) {
				for (TuringTag tagRelated : ctdRelatedMappings.getIndexAttrTag(keyRelated)) {
					if (keyRelated != null && tagRelated != null && tagRelated.getTagName() != null
							&& tagRelated.getTagName().equals("url")) {

						if (log.isDebugEnabled()) {
							log.debug("Key Related: " + keyRelated + " Tag Related: " + tagRelated.getTagName()
									+ " relation: " + TuringUtils.listToString(tagRelated.getSrcAttributeRelation())
									+ " content Type: " + tagRelated.getSrcAttributeType());
						}

						attributesDefs = attributeXML(ciRelated, attributesDefs, tagRelated, keyRelated, config);

					} else {
						// Teste123
						attributesDefs = attributeXML(ciRelated, attributesDefs, tagRelated, keyRelated, config);

					}
				}
			}
		}
		return attributesDefs;
	}

	public static HashMap<String, List<String>> attributeXMLUpdate(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config) throws Exception {

		if (log.isDebugEnabled()) {
			if (attributeData != null) {
				log.debug(tag.getTagName() + " = " + attributeData.getValue().toString());
			}
		}
		// Semantic Attributes
		if (attributeData != null && attributeData.getValue().toString() != null
				&& !attributeData.getValue().toString().trim().equals("")) {
			if (isTuringTag(tag.getTagName())) {
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
					if (isSinlgeValueTMETag(tag.getTagName())) {
						// consider only the first value
						break;
					}
				}
				attributesDefs.put(tag.getTagName(), listAttributeValues);
			} else {
				attributesDefs = attributeByWidget(ci, attributesDefs, tag, key, attributeData, config);
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
			if (isTuringTag(tag.getTagName())) {
				StringTokenizer tokenizer = new StringTokenizer(attributeData, ",");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					attributesDefs.get(tag.getTagName()).add(token);
					if (isSinlgeValueTMETag(tag.getTagName())) {
						// consider only the first value
						break;
					}
				}
			} else {
				attributesDefs = attributeByClass(ci, attributesDefs, tag, key, attributeData, config);
			}
		}

		return attributesDefs;
	}

	// This method post the content to the Viglet Turing broker
	public static boolean indexCreate(ManagedObject mo, IHandlerConfiguration config) {

		boolean success = false;
		if (mappingDefinitions == null || !mappingDefinitions.getMappingsXML().equals(config.getMappingsXML())) {
			mappingDefinitions = XmlParserUtilities.loadMappings(config.getMappingsXML());
			if (mappingDefinitions == null) {

				if (log.isDebugEnabled()) {
					log.error(
							"Mapping definitions are not loaded properly from mappingsXML: " + config.getMappingsXML());
				}

				return false;
			}
		}
		// Let's sure we process only content instances...
		if ((mo != null) && (mo instanceof ContentInstance)) {
			try {
				// Let's make sure we process only content instances...
				if (mappingDefinitions.getMappingDefinitions().get(mo.getObjectType().getData().getName()) != null) {
					// Let's make sure we only process content instances with a
					// Locale attribute matching the configured locale
					// String moLocale = (String)
					// mo.getAttributeValue("OT_WEM_VA_LOCALE");
					// String moLocale =
					// mo.getLocale().getJavaLocale().toString();
					/*
					 * String configLocale = config.getLocale();
					 * 
					 * if (log.isDebugEnabled()) { log.debug("moLocale:" + moLocale +
					 * " ---- configLocale: " + configLocale); }
					 * 
					 * if (moLocale!=null && configLocale!=null &&
					 * moLocale.startsWith(configLocale)) {
					 * 
					 */

					log.info(
							"Viglet Turing indexer Processing Content Type: " + mo.getObjectType().getData().getName());
					if (log.isDebugEnabled()) {
						log.debug("Viglet Turing indexer Processing Content Type: "
								+ mo.getObjectType().getData().getName());
					}

					// class to indicate if the content will be indexed or not
					String className = getClassValidToIndex(mo.getObjectType().getData().getName(), config);
					IValidToIndex instance = null;
					if (className != null) {
						Class<?> clazz = Class.forName(className);
						if (clazz == null) {
							if (log.isDebugEnabled()) {
								log.debug("Valid to Index className is not found in the jar file: " + className);
							}
						} else {
							instance = (IValidToIndex) clazz.newInstance();
						}
					}
					if (instance != null && !instance.isValid((ContentInstance) mo, config)) {
						if (isIndexed((ContentInstance) mo, config)) {
							indexDelete(mo.getContentManagementId().getId(), config);
						}
						return false;
					}
					postIndex(getXML((ContentInstance) mo, config), config);
					success = true;
					/*
					 * } else { if (log.isDebugEnabled()) { log.debug(
					 * "Viglet Turing indexer ignoring a CI with the wrong locale"); } }
					 */
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Mapping definition is not found in the mappingXML for the CTD: "
								+ mo.getObjectType().getData().getName());
						log.debug("Viglet Turing indexer Ingnoring Content Type: "
								+ mo.getObjectType().getData().getName());
					}
				}

			} catch (Exception e) {
				log.error("Can't CREATE to Viglet Turing indexer: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return success;
	}

	// This method deletes the content to the Viglet Turing broker
	public static boolean indexDelete(String guid, IHandlerConfiguration config) {

		boolean success = false;
		try {
			GetMethod get = new GetMethod(config.getTuringURL() + "/?action=delete&index=" + config.getIndex()
					+ "&config=" + config.getConfig() + "&id=" + guid);
			HttpClient httpclient = new HttpClient();
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing Delete Request URI:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP result is: " + result);
				log.debug("Viglet Turing indexer response HTTP result is: " + get.getResponseBodyAsString());
			}
			get.releaseConnection();
			success = true;

		} catch (Exception e) {

			log.error("Can't DELETE in Viglet Turing index: " + e.getMessage());
		}

		return success;

	}

	public static boolean indexDeleteByType(String typeName, IHandlerConfiguration config) {
		boolean success = false;
		try {
			GetMethod get = new GetMethod(config.getTuringURL() + "/solr/" + config.getIndex()
					+ "update/?stream.body=<delete><query>type:" + typeName + "</query></delete>");
			HttpClient httpclient = new HttpClient();
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing Delete Request URI:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP result is: " + result);
				log.debug("Viglet Turing indexer response HTTP result is: " + get.getResponseBodyAsString());
			}
			get.releaseConnection();
			success = true;

		} catch (Exception e) {

			log.error("Can't DELETE in Viglet Turing index: " + e.getMessage());
		}

		return success;
	}

	// This method strips the HTML tags out of some content
	private static String Html2Text(String text) {

		return HtmlManipulator
				.replaceHtmlEntities(HtmlManipulator.removeScriptContent(text).replaceAll("\\<.*?>", " "));
	}

	// This method returns the link to the primary Channel for Semantic
	// Navigation
	@SuppressWarnings("unchecked")
	public static String getSemanticLink(GenericResourceHandlerConfiguration turingConfig, RequestContext rc) {

		// Looking up the Semantic Navigation channel, or pick the current
		// channel if not available
		if (log.isDebugEnabled()) {
			log.debug("Building a link to the Semantic Navigation channel");
		}

		LinkSpec spec = new LinkSpec();
		String guid = rc.getRequestOIDString();
		Site currentSite = null;
		try {
			currentSite = rc.getCurrentSite();
		} catch (ApplicationException e) {
			log.error("Unable to get the current site using requestContext.getCurrentSite api");
		}
		String link = "";
		try {
			String channelName = turingConfig.getChannel();
			if (currentSite != null) {
				if (log.isDebugEnabled()) {
					log.debug("Loading the channel [" + channelName + "] using the site:"
							+ currentSite.getData().getName());
				}
				Channel semanticNavigation = ContentUtil.getChannelByPath(currentSite, channelName);
				if (semanticNavigation != null) {
					guid = semanticNavigation.getContentManagementId().getId();
					if (log.isDebugEnabled()) {
						log.debug("Found the channel with guid [" + guid + "]");
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Channel was not found, defaulting to current guid [" + guid + "]");
					}
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug(
							"Unable to get the current site using requestContext.getCurrentSite api loading the channel ["
									+ channelName + "] without site");
				}
				IPagingList list = Channel.findByName(channelName);
				List<Channel> channelList = list.asList();
				if (!channelList.isEmpty()) {
					Channel semanticNavigation = (Channel) channelList.get(0);
					if (semanticNavigation != null) {
						guid = semanticNavigation.getContentManagementId().getId();
					}
				}
			}
			spec.setOid(guid);
			link = LinkBuilder.buildLink(spec, rc, true, false);
		} catch (Exception e) {
			log.error("Error when building link to Semantic Navigation channel");
		}
		return link;

	}

	// Gets a widget content...
	public static Document turingRequest(IHandlerConfiguration turingConfig, String query) {

		// A formalized XML document will be returned
		Document doc = null;

		if (query == null || turingConfig == null) {
			return doc;
		}

		try {

			// Primary query
			String request = turingConfig.getTuringURL() + "/" + turingConfig.getIndex() + query;

			// Depending on the form , we need either &format=xml or /format/xml
			if (query.indexOf('?') > 0) {
				request = request + "&format=xml";
			} else {
				request = request + "/format/xml";
			}
			if (log.isDebugEnabled()) {
				log.debug("OSTN request is " + request);
			}
			// Performs a simple GET request to the Viglet Turing server
			GetMethod get = new GetMethod(request);
			// setting the header as below is also not helping in converting the
			// response body to utf-8 string.
			// get.setRequestHeader("Content-Type",
			// "application/xml;charset=utf-8");
			HttpClient httpclient = new HttpClient();

			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("OSTN request HTTP return code is " + result);
			}
			InputStream bodyStream = get.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(bodyStream, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			// String xml = get.getResponseBodyAsString();
			String xml = sb.toString();
			if (log.isDebugEnabled()) {
				log.debug("OSTN request HTTP response string is " + xml);
			}
			get.releaseConnection();

			// Let's treat the results as XML
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			doc = db.parse(is);
			doc.getDocumentElement().normalize();

		} catch (Exception e) {

			log.error("Error is " + e.getMessage());

		}

		return doc;

	}

	public static boolean createAndRegisterTuringIndex(IHandlerConfiguration config, String indexName,
			String templateName) {
		boolean success = false;
		if (templateName == null || templateName.trim().equals("")) {
			templateName = "default-en";
		}
		try {
			if (log.isDebugEnabled()) {
				log.debug("Creating the index with the indexname:" + indexName);
			}
			PostMethod post = new PostMethod(config.getTuringURL() + "/sse/index/" + indexName);
			post.setParameter("template", templateName);
			post.setRequestHeader("Accept", "*/*");
			HttpClient httpclient = new HttpClient();
			int result = httpclient.executeMethod(post);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing create indexer response HTTP result is: " + result);
				log.debug("Viglet Turing create indexer response HTTP response body is: "
						+ post.getResponseBodyAsString());
			}
			post.releaseConnection();
			if (result == 201) {
				if (log.isDebugEnabled()) {
					log.debug("Created the index, now registering the index with the solr indexname:" + indexName);
				}
				GetMethod get = new GetMethod(config.getTuringURL() + "/solr/admin/cores?action=CREATE&name="
						+ indexName + "&instanceDir=" + indexName);
				get.setRequestHeader("Accept", "*/*");
				result = httpclient.executeMethod(get);
				if (log.isDebugEnabled()) {
					log.debug("Viglet Turing register indexer response HTTP result is: " + result);
					log.debug("Viglet Turing register indexer response HTTP response body is: "
							+ get.getResponseBodyAsString());
				}
				get.releaseConnection();
				success = true;
			}
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
		return success;
	}

	public static boolean deleteTuringIndex(IHandlerConfiguration config, String indexName) {
		boolean success = false;
		try {
			HttpClient httpclient = new HttpClient();
			DeleteMethod del = new DeleteMethod(config.getTuringURL() + "/sse/index/" + indexName);
			del.setRequestHeader("Accept", "*/*");
			int result = httpclient.executeMethod(del);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing Delete indexer response HTTP result is: " + result);
				log.debug("Viglet Turing Delete indexer response HTTP response body is: "
						+ del.getResponseBodyAsString());
			}
			del.releaseConnection();
			success = true;
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
		return success;
	}

	public static void printTuringIndexes(IHandlerConfiguration config) {
		try {
			HttpClient httpclient = new HttpClient();
			GetMethod get = new GetMethod(config.getTuringURL() + "/sse/index");
			get.setRequestHeader("Accept", "*/*");
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("executing query:" + get.getURI());
				log.debug("Viglet Turing list indexer response HTTP result is: " + result);
				log.debug(
						"Viglet Turing list indexer response HTTP response body is: " + get.getResponseBodyAsString());
			}
			get.releaseConnection();
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
	}

	public static MappingDefinitions getMappingDefinitions(IHandlerConfiguration config) {
		if (mappingDefinitions == null || !mappingDefinitions.getMappingsXML().equals(config.getMappingsXML())) {
			mappingDefinitions = XmlParserUtilities.loadMappings(config.getMappingsXML());
		}
		if (mappingDefinitions == null) {
			if (log.isDebugEnabled()) {
				log.error("Mapping definitions are not loaded properly from mappingsXML: " + config.getMappingsXML());
			}
		}
		return mappingDefinitions;
	}

	public static boolean isExternalResource(String objectTypeName, IHandlerConfiguration config) {
		return getCustomClassName(objectTypeName, config) != null;
	}

	public static String getCustomClassName(String objectTypeName, IHandlerConfiguration config) {
		HashMap<String, CTDMappings> mappings = getMappingDefinitions(config).getMappingDefinitions();
		CTDMappings ctdMappings = mappings.get(objectTypeName);
		if (ctdMappings.getCustomClassName() == null) {
			if (log.isDebugEnabled()) {
				log.debug("Custom className is not found in the mappingXML for the CTD: " + objectTypeName);
			}
			return null;
		}
		return ctdMappings.getCustomClassName();
	}

	public static String getClassValidToIndex(String objectTypeName, IHandlerConfiguration config) {
		HashMap<String, CTDMappings> mappings = getMappingDefinitions(config).getMappingDefinitions();
		CTDMappings ctdMappings = mappings.get(objectTypeName);
		if (ctdMappings.getClassValidToIndex() == null) {
			if (log.isDebugEnabled()) {
				log.debug("Valid to Index className is not found in the mappingXML for the CTD: " + objectTypeName);
			}
			return null;
		}
		return ctdMappings.getClassValidToIndex();
	}

	public static boolean indexCreate(ExternalResourceObject mo, String typeName, IHandlerConfiguration config) {
		boolean success = false;
		try {
			log.info("Viglet Turing indexer Processing Content Type: " + typeName);
			if (log.isDebugEnabled()) {
				log.debug("Viglet Turing indexer Processing Content Type: " + typeName);
			}
			// class to indicate if the content will be indexed or not
			String className = getClassValidToIndex(typeName, config);
			IValidToIndex instance = null;
			if (className != null) {
				Class<?> clazz = Class.forName(className);
				if (clazz == null) {
					if (log.isDebugEnabled()) {
						log.debug("Valid to Index className is not found in the jar file: " + className);
					}
				} else {
					instance = (IValidToIndex) clazz.newInstance();
				}
			}
			if (instance != null && !instance.isValid(mo, config)) {
				if (isIndexed(mo, config)) {
					indexDelete(mo.getId(), config);
				}
				return false;
			}
			postIndex(getXML(mo, config), config);
			success = true;
		} catch (Exception e) {
			log.error("Can't CREATE to Viglet Turing indexer: " + e.getMessage());
			e.printStackTrace();
		}
		return success;
	}

	public static void postIndex(String xml, IHandlerConfiguration config) throws HttpException, IOException {
		PostMethod post = new PostMethod(
				config.getTuringURL() + "/?index=" + config.getIndex() + "&config=" + config.getConfig());
		post.setParameter("data", xml);
		post.setParameter("index", config.getIndex());
		post.setParameter("config", config.getConfig());
		post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		HttpClient httpclient = new HttpClient();
		int result = httpclient.executeMethod(post);
	
		if (log.isDebugEnabled()) {
			log.debug("Viglet Turing Index Request URI:" + post.getURI());
			log.debug("Using the index:" + config.getIndex() + ", config:" + config.getConfig());
			log.debug("XML:" + xml);
			log.debug(
					"Viglet Turing indexer response HTTP result is: " + result + ", for request uri:" + post.getURI());
			log.debug("Viglet Turing indexer response HTTP result is: " + post.getResponseBodyAsString());
		}
		post.releaseConnection();
	}

	private static boolean isIndexed(ContentInstance mo, IHandlerConfiguration config) {
		try {
			HttpClient httpclient = new HttpClient();
			GetMethod get = new GetMethod(config.getTuringURL() + "/solr/" + config.getIndex() + "/select/?q=id%3A"
					+ mo.getContentManagementId().getId());
			get.setRequestHeader("Accept", "*/*");
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("executing query:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP result is: " + result);
				log.debug("Viglet Turing indexer response HTTP response body is: " + get.getResponseBodyAsString());
			}
			if (result == 200) {
				if (!"numFound=\"0\"".equals(get.getResponseBodyAsString())) {
					return true;
				}
			}
			get.releaseConnection();
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
		return false;
	}

	private static boolean isIndexed(ExternalResourceObject mo, IHandlerConfiguration config) {
		try {
			HttpClient httpclient = new HttpClient();
			GetMethod get = new GetMethod(
					config.getTuringURL() + "/solr/" + config.getIndex() + "/select/?q=id%3A" + mo.getId());
			get.setRequestHeader("Accept", "*/*");
			int result = httpclient.executeMethod(get);
			if (log.isDebugEnabled()) {
				log.debug("executing query:" + get.getURI());
				log.debug("Viglet Turing indexer response HTTP result is: " + result);
				log.debug("Viglet Turing indexer response HTTP response body is: " + get.getResponseBodyAsString());
			}
			if (result == 200) {
				if (!"numFound=\"0\"".equals(get.getResponseBodyAsString())) {
					return true;
				}
			}
			get.releaseConnection();
		} catch (Exception e) {
			System.out.println("Error is " + e.getMessage());
		}
		return false;
	}

}
