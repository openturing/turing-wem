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
package com.viglet.turing.mappers;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.viglet.turing.beans.TurCTDMappingMap;
import com.viglet.turing.beans.TurIndexAttrMap;
import com.viglet.turing.beans.TurMiscConfigMap;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.TurXMLConstant;
import com.viglet.turing.util.TuringUtils;
import com.vignette.logging.context.ContextLogger;

public class MappingDefinitionsProcess {
	private static final ContextLogger log = ContextLogger.getLogger(MappingDefinitionsProcess.class);

	public static MappingDefinitions loadMappings(String resourceXml) {
		TurCTDMappingMap mappings = null;
		TurMiscConfigMap mscConfig = null;

		try {
			DocumentBuilderFactory dlf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dlf.newDocumentBuilder();

			File f = new File(resourceXml);
			if (f.isFile() && f.canRead()) {
				InputStream resourceInputStream = new FileInputStream(resourceXml);

				Document document = db.parse(resourceInputStream);
				Element rootElement = document.getDocumentElement();

				// Loading mapping definitions
				mappings = readCTDMappings(rootElement);

				// Loading Miscellaneous config
				mscConfig = readMscConfig(rootElement, TurXMLConstant.TAG_MSC_CONFIG);
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
	public static TurCTDMappingMap readCTDMappings(Element rootElement) {
		TurCTDMappingMap mappings = new TurCTDMappingMap();
		
		TurIndexAttrMap commonIndexAttrsMap = readIndexAttributeMappings(rootElement, TurXMLConstant.TAG_COMMON_INDEX_DATA);
		
		NodeList ctdMappingDefList = rootElement.getElementsByTagName(TurXMLConstant.TAG_MAPPING_DEF);
		
		for (int i = 0; i < ctdMappingDefList.getLength(); i++) {
			Element mappingDefinition = (Element) ctdMappingDefList.item(i);
			if (mappingDefinition.hasAttribute(TurXMLConstant.TAG_ATT_MAPPING_DEF)) {
				String ctdXmlName = mappingDefinition.getAttribute(TurXMLConstant.TAG_ATT_MAPPING_DEF);
				TurIndexAttrMap indexAttrsMap = readIndexAttributeMappings((Element) ctdMappingDefList.item(i),
						TurXMLConstant.TAG_INDEX_DATA);
				CTDMappings ctdMapping = new CTDMappings(commonIndexAttrsMap, indexAttrsMap);
				if (mappingDefinition.hasAttribute(TurXMLConstant.TAG_ATT_CUSTOM_CLASS)) {
					ctdMapping.setCustomClassName(mappingDefinition.getAttribute(TurXMLConstant.TAG_ATT_CUSTOM_CLASS));
				}
				if (mappingDefinition.hasAttribute(TurXMLConstant.TAG_ATT_CLASS_VALID_TOINDEX)) {
					ctdMapping.setClassValidToIndex(
							mappingDefinition.getAttribute(TurXMLConstant.TAG_ATT_CLASS_VALID_TOINDEX));
				}
				mappings.put(ctdXmlName, ctdMapping);
			}
		}
		return mappings;
	}

	// Loading Miscellaneous config
	public static TurMiscConfigMap readMscConfig(Element rootElement, String mscTagName) {
		TurMiscConfigMap mscConfig = new TurMiscConfigMap();
		NodeList configNodeList = rootElement.getElementsByTagName(mscTagName);
		for (int i = 0; i < configNodeList.getLength(); i++) {
			Element mscConfigElement = (Element) configNodeList.item(i);
			NodeList attributeLilst = mscConfigElement.getElementsByTagName(TurXMLConstant.TAG_MSC_ATTR);
			for (int j = 0; j < attributeLilst.getLength(); j++) {
				Element attrElement = (Element) attributeLilst.item(j);
				String key = attrElement.getAttribute(TurXMLConstant.TAG_MSC_KEY);
				String value = attrElement.getAttribute(TurXMLConstant.TAG_MSC_VALUE);
				if (key != null && value != null) {
					mscConfig.put(key, value);
				}
			}
		}
		return mscConfig;
	}

	public static TurIndexAttrMap readIndexAttributeMappings(Element rootElement, String tagName) {
		TurIndexAttrMap genericIndexAttrMap = new TurIndexAttrMap();
		NodeList mappingList = rootElement.getElementsByTagName(tagName);
		for (int i = 0; i < mappingList.getLength(); i++) {
			loadAtributesFromAttrsElement((Element) mappingList.item(i), genericIndexAttrMap);
		}
		return genericIndexAttrMap;
	}

	public static void loadAtributesFromAttrsElement(Element AttrsElement, TurIndexAttrMap genericIndexAttrMap) {
		NodeList srcNodeList = AttrsElement.getElementsByTagName("srcAttr");

		for (int i = 0; i < srcNodeList.getLength(); i++) {
			Element srcAttrNode = (Element) srcNodeList.item(i);
			if (srcAttrNode.hasAttributes() && (srcAttrNode.hasAttribute(TurXMLConstant.XML_NAME_ATT)
					|| srcAttrNode.hasAttribute(TurXMLConstant.CLASS_NAME_ATT))) {
				loadSrcAttr(srcAttrNode, genericIndexAttrMap);
			}
		}
	}

	// Load srcAttr XML Attribute
	public static void loadSrcAttr(Element srcAttrNode, TurIndexAttrMap genericIndexAttrMap) {
		TuringTag turingTag = new TuringTag();
		if (srcAttrNode.hasAttribute(TurXMLConstant.XML_NAME_ATT))
			turingTag.setSrcAttribute(srcAttrNode.getAttribute(TurXMLConstant.XML_NAME_ATT));

		if (srcAttrNode.hasAttribute(TurXMLConstant.CLASS_NAME_ATT))
			turingTag.setSrcClassName(srcAttrNode.getAttribute(TurXMLConstant.CLASS_NAME_ATT));

		if (srcAttrNode.hasAttribute(TurXMLConstant.VALUE_TYPE_ATT))
			turingTag.setSrcAttributeType(srcAttrNode.getAttribute(TurXMLConstant.VALUE_TYPE_ATT));

		if (srcAttrNode.hasAttribute(TurXMLConstant.RELATION_ATT))
			turingTag.setSrcAttributeRelation(
					Arrays.asList(srcAttrNode.getAttribute(TurXMLConstant.RELATION_ATT).split("\\.")));

		if (srcAttrNode.hasAttribute(TurXMLConstant.MANDATORY_ATT)) {
			if (log.isDebugEnabled()) {
				log.debug("MANDATORY: " + srcAttrNode.getAttribute(TurXMLConstant.MANDATORY_ATT));

			}
			turingTag.setSrcMandatory(
					Boolean.valueOf(srcAttrNode.getAttribute(TurXMLConstant.MANDATORY_ATT)).booleanValue());

		} else
			turingTag.setSrcMandatory(false);

		if (log.isDebugEnabled())
			log.debug(String.format("Mandatory: %b", turingTag.getSrcMandatory()));

		if (srcAttrNode.hasAttribute(TurXMLConstant.UNIQUE_VALUES_ATT))
			turingTag.setSrcUniqueValues(
					Boolean.valueOf(srcAttrNode.getAttribute(TurXMLConstant.UNIQUE_VALUES_ATT)).booleanValue());
		else
			turingTag.setSrcUniqueValues(false);

		if (log.isDebugEnabled())
			log.debug(String.format("Unique Values: %b", turingTag.isSrcUniqueValues()));

		if ((turingTag.getSrcAttribute() != null) || (turingTag.getSrcClassName() != null)) {
			NodeList tagList = (srcAttrNode).getElementsByTagName("tag");
			if (log.isDebugEnabled()) {
				log.debug("Node Parent: " + turingTag.getSrcAttribute());
				log.debug("Node.getLength(): " + tagList.getLength());
			}

			ArrayList<TuringTag> turingTags = new ArrayList<TuringTag>();

			for (int nodePos = 0; nodePos < tagList.getLength(); nodePos++) {
				if (log.isDebugEnabled())
					log.debug("Node: " + nodePos);

				String tagName = null;
				Node tagNode = tagList.item(nodePos);
				tagName = tagNode.getFirstChild().getNodeValue();
				if (log.isDebugEnabled())
					log.debug("NodeName:" + tagName);
				
				if (tagName != null) {
					turingTag.setTagName(tagName);
					if ((turingTag.getSrcAttribute() == null) && (turingTag.getSrcClassName() != null))
						turingTag.setSrcAttribute(TuringUtils.getIndexTagName(turingTag));

					turingTags.add(turingTag);

				}
			}
			if (turingTags.size() > 0)
				genericIndexAttrMap.put(turingTag.getSrcAttribute(), turingTags);
		}
	}
}