package com.viglet.turing.util;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.mappers.CTDMappings;
import com.viglet.turing.mappers.MappingDefinitions;
import com.vignette.logging.context.ContextLogger;

/**
 * Created by IntelliJ IDEA. User: spinnama Date: Jan 28, 2011 Time: 11:02:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class XmlParserUtilities {
	public static final String VALUE_TYPE_ATT = "valueType";
	public static final String XML_NAME_ATT = "xmlName";
	public static final String CLASS_NAME_ATT = "className";
	public static final String RELATION_ATT = "relation";
	public static final String MANDATORY_ATT = "mandatory";
	public static final String TAG_COMMON_INDEX_DATA = "common-index-attrs";
	public static final String TAG_INDEX_DATA = "index-attrs";
	public static final String TAG_MAPPING_DEF = "mappingDefinition";
	public static final String TAG_ATT_MAPPING_DEF = "contentType";
	public static final String TAG_MSC_CONFIG = "misc-attrs";
	public static final String TAG_MSC_ATTR = "attr";
	public static final String TAG_MSC_KEY = "key";
	public static final String TAG_MSC_VALUE = "value";
	public static final String TAG_ATT_CUSTOM_CLASS = "customClassName";
	public static final String TAG_ATT_CLASS_VALID_TOINDEX = "validToIndex";
	public static final String UNIQUE_VALUES_ATT = "uniqueValues";

	private static final ContextLogger log = ContextLogger.getLogger(XmlParserUtilities.class);

	public static void main(String[] args) throws Exception {

		MappingDefinitions mappingDefs = loadMappings("/CTD-Nstein-Mappings.xml");
		HashMap<String, CTDMappings> mappings = mappingDefs.getMappingDefinitions();
		HashMap<String, String> config = mappingDefs.getMscConfig();

		for (Map.Entry<String, String> entry : config.entrySet()) {
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
		for (Map.Entry<String, CTDMappings> entry : mappings.entrySet()) {
			System.out.println("\n\nKey: " + entry.getKey());
			System.out.println("\nCommon Data: ");
			CTDMappings ctdMappings = mappings.get(entry.getKey());
			for (String key : ctdMappings.getIndexAttrs()) {
				for (TuringTag tag : ctdMappings.getIndexAttrTag(key)) {
					System.out.println("Key: " + key + " Tag: " + tag.getTagName() + " content Type: "
							+ tag.getSrcAttributeType());
				}
			}
		}
	}

	public static MappingDefinitions loadMappings(String resourceXml) {
		HashMap<String, CTDMappings> mappings = null;
		HashMap<String, String> mscConfig = null;

		try {
			DocumentBuilderFactory dlf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dlf.newDocumentBuilder();
			// InputStream resourceInputStream =
			// XmlParserUtilities.class.getResourceAsStream(resourceXml);

			File f = new File(resourceXml);
			if (f.isFile() && f.canRead()) {
				InputStream resourceInputStream = new FileInputStream(resourceXml);

				Document document = db.parse(resourceInputStream);
				Element rootElement = document.getDocumentElement();

				// Loading mapping definitions
				mappings = readCTDMappings(rootElement);

				// Loading Miscellaneous config
				mscConfig = readMscConfig(rootElement, TAG_MSC_CONFIG);
			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error when loading mappings");
			return null;
		}
		return new MappingDefinitions(resourceXml, mappings, mscConfig);
	}

	// Loading mapping definitions
	public static HashMap<String, CTDMappings> readCTDMappings(Element rootElement) {
		HashMap<String, CTDMappings> mappings = new HashMap<String, CTDMappings>();
		HashMap<String, ArrayList<TuringTag>> commonMappings = readIndexAttributeMappings(rootElement,
				TAG_COMMON_INDEX_DATA);
		NodeList ctdMappingDefList = rootElement.getElementsByTagName(TAG_MAPPING_DEF);
		for (int i = 0; i < ctdMappingDefList.getLength(); i++) {
			Element mappingDefinition = (Element) ctdMappingDefList.item(i);
			if (mappingDefinition.hasAttribute(TAG_ATT_MAPPING_DEF)) {
				String ctdXmlName = mappingDefinition.getAttribute(TAG_ATT_MAPPING_DEF);
				HashMap<String, ArrayList<TuringTag>> ctdSpecificMappings = readIndexAttributeMappings(
						(Element) ctdMappingDefList.item(i), TAG_INDEX_DATA);
				CTDMappings ctdMapping = new CTDMappings(commonMappings, ctdSpecificMappings);
				if (mappingDefinition.hasAttribute(TAG_ATT_CUSTOM_CLASS)) {
					ctdMapping.setCustomClassName(mappingDefinition.getAttribute(TAG_ATT_CUSTOM_CLASS));
				}
				if (mappingDefinition.hasAttribute(TAG_ATT_CLASS_VALID_TOINDEX)) {
					ctdMapping.setClassValidToIndex(mappingDefinition.getAttribute(TAG_ATT_CLASS_VALID_TOINDEX));
				}
				mappings.put(ctdXmlName, ctdMapping);
			}
		}
		return mappings;
	}

	// Loading Miscellaneous config
	public static HashMap<String, String> readMscConfig(Element rootElement, String mscTagName) {
		HashMap<String, String> mscConfig = new HashMap<String, String>();
		NodeList configNodeList = rootElement.getElementsByTagName(mscTagName);
		for (int i = 0; i < configNodeList.getLength(); i++) {
			Element mscConfigElement = (Element) configNodeList.item(i);
			NodeList attributeLilst = mscConfigElement.getElementsByTagName(TAG_MSC_ATTR);
			for (int j = 0; j < attributeLilst.getLength(); j++) {
				Element attrElement = (Element) attributeLilst.item(j);
				String key = attrElement.getAttribute(TAG_MSC_KEY);
				String value = attrElement.getAttribute(TAG_MSC_VALUE);
				if (key != null && value != null) {
					mscConfig.put(key, value);
				}
			}
		}
		return mscConfig;
	}

	public static HashMap<String, ArrayList<TuringTag>> readIndexAttributeMappings(Element rootElement,
			String tagName) {
		HashMap<String, ArrayList<TuringTag>> commonMappings = new HashMap<String, ArrayList<TuringTag>>();
		NodeList commonMappingList = rootElement.getElementsByTagName(tagName);
		for (int i = 0; i < commonMappingList.getLength(); i++) {
			loadAtributesFromAttrsElement((Element) commonMappingList.item(i), commonMappings);
		}
		return commonMappings;
	}

	public static void loadAtributesFromAttrsElement(Element AttrsElement,
			HashMap<String, ArrayList<TuringTag>> indexAttrMap) {
		NodeList srcNodeList = AttrsElement.getElementsByTagName("srcAttr");
		for (int i = 0; i < srcNodeList.getLength(); i++) {
			Element srcAttrNode = (Element) srcNodeList.item(i);
			if (srcAttrNode.hasAttributes()
					&& (srcAttrNode.hasAttribute(XML_NAME_ATT) || srcAttrNode.hasAttribute(CLASS_NAME_ATT))) {
				String srcAttXmlName = null, srcAttClassName = null, srcAttValueType = null;
				List<String> srcAttRelation = null;
				boolean srcMandatory = false;
				boolean srcUniqueValues = false;
				if (srcAttrNode.hasAttribute(XML_NAME_ATT)) {
					srcAttXmlName = srcAttrNode.getAttribute(XML_NAME_ATT);
				}

				if (srcAttrNode.hasAttribute(CLASS_NAME_ATT)) {
					srcAttClassName = srcAttrNode.getAttribute(CLASS_NAME_ATT);
				}

				if (srcAttrNode.hasAttribute(VALUE_TYPE_ATT)) {
					srcAttValueType = srcAttrNode.getAttribute(VALUE_TYPE_ATT);
				}

				if (srcAttrNode.hasAttribute(RELATION_ATT)) {				
					srcAttRelation = Arrays.asList(srcAttrNode.getAttribute(RELATION_ATT).split("\\."));
				}
				if (srcAttrNode.hasAttribute(MANDATORY_ATT)) {
					if (log.isDebugEnabled()) {
						log.debug("MANDATORY: " + srcAttrNode.getAttribute(MANDATORY_ATT));

					}
					srcMandatory = Boolean.valueOf(srcAttrNode.getAttribute(MANDATORY_ATT)).booleanValue();

					if (log.isDebugEnabled()) {
						if (srcMandatory) {
							log.debug("MANDATORY: verdadeiro");
						} else {
							log.debug("MANDATORY: falso");
						}
					}
				}
				if (srcAttrNode.hasAttribute(UNIQUE_VALUES_ATT)) {
					if (log.isDebugEnabled()) {
						log.debug("UNIQUE VALUES: " + srcAttrNode.getAttribute(UNIQUE_VALUES_ATT));

					}
					srcUniqueValues = Boolean.valueOf(srcAttrNode.getAttribute(UNIQUE_VALUES_ATT)).booleanValue();

					if (log.isDebugEnabled()) {
						if (srcUniqueValues) {
							log.debug("UNIQUE VALUES: verdadeiro");
						} else {
							log.debug("UNIQUE VALUES: falso");
						}
					}
				}
				if ((srcAttXmlName != null) || (srcAttClassName != null)) {
					NodeList tagList = (srcAttrNode).getElementsByTagName("tag");
					if (log.isDebugEnabled()) {
						log.debug("Node Parent: " + srcAttXmlName);
						log.debug("Node.getLength(): " + tagList.getLength());
					}

					ArrayList<TuringTag> TuringTags = new ArrayList<TuringTag>();

					for (int nodePos = 0; nodePos < tagList.getLength(); nodePos++) {
						if (log.isDebugEnabled()) {
							log.debug("Node: " + nodePos);
						}
						String tagName = null;
						Node tagNode = tagList.item(nodePos);
						tagName = tagNode.getFirstChild().getNodeValue();
						if (log.isDebugEnabled()) {
							log.debug("NodeName:" + tagName);
						}
						if (tagName != null) {
							if ((srcAttXmlName == null) && (srcAttClassName != null)) {
								srcAttXmlName = "CLASSNAME_" + tagName;
							}
							TuringTags.add(new TuringTag(tagName, srcAttXmlName, srcAttValueType, srcAttRelation,
									srcAttClassName, srcMandatory, srcUniqueValues));

						}
					}
					if (TuringTags.size() > 0) {
						indexAttrMap.put(srcAttXmlName, TuringTags);
					}
				}
			}
		}
	}
}
