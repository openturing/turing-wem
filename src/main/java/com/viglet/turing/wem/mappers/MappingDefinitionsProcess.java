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
package com.viglet.turing.wem.mappers;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.viglet.turing.wem.beans.TurCTDMappingMap;
import com.viglet.turing.wem.beans.TuringTag;
import com.viglet.turing.wem.beans.TuringTagMap;
import com.viglet.turing.wem.config.IHandlerConfiguration;
import com.viglet.turing.wem.config.TurXMLConstant;
import com.vignette.logging.context.ContextLogger;

// Open and process Mappping XML File structure
public class MappingDefinitionsProcess {
	private static final ContextLogger log = ContextLogger.getLogger(MappingDefinitionsProcess.class);

	public static MappingDefinitions loadMappings(String resourceXml) {
		TurCTDMappingMap mappings = null;

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

			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error when loading mappings");
			return null;
		}
		return new MappingDefinitions(resourceXml, mappings);
	}

	/**
	 * Loading mapping definitions
	 * 
	 * @param rootElement
	 * @return TurCTDMappingMap
	 */
	public static TurCTDMappingMap readCTDMappings(Element rootElement) {
		TurCTDMappingMap mappings = new TurCTDMappingMap();

		// Read <common-index-attrs/>
		List<TuringTag> commonIndexAttrs = readIndexAttributeMappings(rootElement,
				TurXMLConstant.TAG_COMMON_INDEX_DATA);

		// Get <mappingdefinition/> List
		NodeList ctdMappingDefList = rootElement.getElementsByTagName(TurXMLConstant.TAG_MAPPING_DEF);

		for (int i = 0; i < ctdMappingDefList.getLength(); i++) {
			Element mappingDefinition = (Element) ctdMappingDefList.item(i);

			// If it have contenttype attribute
			if (mappingDefinition.hasAttribute(TurXMLConstant.TAG_ATT_MAPPING_DEF)) {
				String ctdXmlName = mappingDefinition.getAttribute(TurXMLConstant.TAG_ATT_MAPPING_DEF);

				// Read <index-attr/>
				List<TuringTag> indexAttrs = readIndexAttributeMappings((Element) ctdMappingDefList.item(i),
						TurXMLConstant.TAG_INDEX_DATA);

				// Merge CommonIndexAttrs into IndexAttrs
				TuringTagMap turingTagMap = mergeCommonAttrs(commonIndexAttrs, indexAttrs);

				// Add attributes common and index attributes into CTDMapping
				CTDMappings ctdMapping = new CTDMappings(turingTagMap);

				// Set isValidToIndex
				if (mappingDefinition.hasAttribute(TurXMLConstant.TAG_ATT_CLASS_VALID_TOINDEX))
					ctdMapping.setClassValidToIndex(
							mappingDefinition.getAttribute(TurXMLConstant.TAG_ATT_CLASS_VALID_TOINDEX));

				/// HashMap of CTDs
				mappings.put(ctdXmlName, ctdMapping);
				if (log.isDebugEnabled()) {
					int index = 0;
					for (Entry<String, CTDMappings> mappingEntry : mappings.entrySet()) {
						log.debug(String.format("%d - MappingEntry CTD : %s", index, mappingEntry.getKey()));
						for (Entry<String, ArrayList<TuringTag>> turingTagEntry : mappingEntry.getValue()
								.getTuringTagMap().entrySet()) {
							log.debug("TuringTag Key (TagName): " + turingTagEntry.getKey());
							for (TuringTag turingTag : turingTagEntry.getValue()) {
								log.debug("TuringTag Item - getTagName : " + turingTag.getTagName());
								log.debug("TuringTag Item - getSrcAttributeType : " + turingTag.getSrcAttributeType());
								log.debug("TuringTag Item - getSrcClassName : " + turingTag.getSrcClassName());
								log.debug("TuringTag Item - getSrcXmlName : " + turingTag.getSrcXmlName());
								log.debug("TuringTag Item - getSrcAttributeRelation : "
										+ turingTag.getSrcAttributeRelation());
								log.debug("TuringTag Item - getSrcMandatory : " + turingTag.getSrcMandatory());
							}
						}
						index++;
					}
				}
			}
		}
		return mappings;
	}

	public static TuringTagMap mergeCommonAttrs(List<TuringTag> commonIndexAttrs, List<TuringTag> indexAttrs) {

		TuringTagMap indexAttrsMapMerged = new TuringTagMap();

		Map<String, TuringTag> commonIndexAttrMap = new HashMap<String, TuringTag>();
		for (TuringTag turingTag : commonIndexAttrs)
			commonIndexAttrMap.put(turingTag.getTagName(), turingTag);

		for (TuringTag turingTag : indexAttrs) {
			if (turingTag != null) {
				if (commonIndexAttrs != null && turingTag.getSrcClassName() == null) {
					if (commonIndexAttrMap.get(turingTag.getTagName()) != null) {
						// Common always have one item
						// Add ClassName of Common into Index, if doesn't have ClassName
						turingTag.setSrcClassName(commonIndexAttrMap.get(turingTag.getTagName()).getSrcClassName());
					}
				}

				if (!indexAttrsMapMerged.containsKey(turingTag.getTagName()))
					indexAttrsMapMerged.put(turingTag.getTagName(), new ArrayList<TuringTag>());
				indexAttrsMapMerged.get(turingTag.getTagName()).add(turingTag);
			}
		}

		// Add only Mandatory Attributes
		for (TuringTag commonTuringTag : commonIndexAttrs) {
			// Doesn't repeat tags that exist in Ctd
			if (commonTuringTag.getSrcMandatory()) {
				if (!indexAttrsMapMerged.containsKey(commonTuringTag.getTagName())) {
					ArrayList<TuringTag> turingTags = new ArrayList<TuringTag>();
					turingTags.add(commonTuringTag);
					indexAttrsMapMerged.put(commonTuringTag.getTagName(), turingTags);
				}
			}
		}

		return indexAttrsMapMerged;
	}

	// Read <index-attrs/> or <common-index-attrs/>
	public static List<TuringTag> readIndexAttributeMappings(Element rootElement, String genericIndexAttrsTag) {
		List<TuringTag> turingTagMap = new ArrayList<TuringTag>();
		NodeList mappingList = rootElement.getElementsByTagName(genericIndexAttrsTag);
		for (int i = 0; i < mappingList.getLength(); i++) {
			// Load <srcAttr/> List
			List<TuringTag> turingTagsPerSrcAttr = loadAtributesFromAttrsElement((Element) mappingList.item(i));
			if (turingTagsPerSrcAttr != null)
				turingTagMap.addAll(turingTagsPerSrcAttr);
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s Attributes", genericIndexAttrsTag));
			for (TuringTag turingTag : turingTagMap)
				log.debug(String.format(" Tag %s - Attribute %s", turingTag.getTagName(), turingTag.getSrcXmlName()));
		}
		return turingTagMap;
	}

	// Load <srcAttr/> List
	public static List<TuringTag> loadAtributesFromAttrsElement(Element AttrsElement) {
		NodeList srcNodeList = AttrsElement.getElementsByTagName("srcAttr");
		List<TuringTag> turingTagsPerSrcAttr = new ArrayList<TuringTag>();

		for (int i = 0; i < srcNodeList.getLength(); i++) {
			Element srcAttrNode = (Element) srcNodeList.item(i);
			if (srcAttrNode.hasAttributes() && (srcAttrNode.hasAttribute(TurXMLConstant.XML_NAME_ATT)
					|| srcAttrNode.hasAttribute(TurXMLConstant.CLASS_NAME_ATT))) {
				List<TuringTag> turingTags = loadSrcAttr(srcAttrNode);
				if (turingTags != null)
					turingTagsPerSrcAttr.addAll(turingTags);
			}
		}
		return turingTagsPerSrcAttr;
	}

	// Read <srcAttr/>
	public static List<TuringTag> loadSrcAttr(Element srcAttrNode) {
		TuringTag turingTag = new TuringTag();
		if (srcAttrNode.hasAttribute(TurXMLConstant.XML_NAME_ATT))
			turingTag.setSrcXmlName(srcAttrNode.getAttribute(TurXMLConstant.XML_NAME_ATT));

		if (srcAttrNode.hasAttribute(TurXMLConstant.CLASS_NAME_ATT))
			turingTag.setSrcClassName(srcAttrNode.getAttribute(TurXMLConstant.CLASS_NAME_ATT));

		if (srcAttrNode.hasAttribute(TurXMLConstant.VALUE_TYPE_ATT))
			turingTag.setSrcAttributeType(srcAttrNode.getAttribute(TurXMLConstant.VALUE_TYPE_ATT));

		if (srcAttrNode.hasAttribute(TurXMLConstant.RELATION_ATT))
			turingTag.setSrcAttributeRelation(
					Arrays.asList(srcAttrNode.getAttribute(TurXMLConstant.RELATION_ATT).split("\\.")));

		if (srcAttrNode.hasAttribute(TurXMLConstant.MANDATORY_ATT)) {
			if (log.isDebugEnabled())
				log.debug(String.format("MANDATORY: %s", srcAttrNode.getAttribute(TurXMLConstant.MANDATORY_ATT)));

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

		if ((turingTag.getSrcXmlName() != null) || (turingTag.getSrcClassName() != null)) {
			NodeList tagList = (srcAttrNode).getElementsByTagName("tag");
			if (log.isDebugEnabled()) {
				log.debug("Node Parent: " + turingTag.getSrcXmlName());
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
					log.debug("tagName:" + tagName);

				if (tagName != null) {
					turingTag.setTagName(tagName);
					turingTags.add(turingTag);

				}
			}
			if (turingTags.size() > 0) {
				return turingTags;
			}

		}
		return null;
	}

	public static MappingDefinitions getMappingDefinitions(IHandlerConfiguration config) {

		MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.loadMappings(config.getMappingsXML());

		if (mappingDefinitions == null && log.isDebugEnabled())
			log.error("Mapping definitions are not loaded properly from mappingsXML: " + config.getMappingsXML());

		return mappingDefinitions;
	}

}
