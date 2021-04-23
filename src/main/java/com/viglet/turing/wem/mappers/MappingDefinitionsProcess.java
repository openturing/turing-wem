/*
 * Copyright (C) 2016-2021 Alexandre Oliveira <alexandre.oliveira@viglet.com> 
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.viglet.turing.wem.beans.TuringTag;
import com.viglet.turing.wem.beans.TuringTagMap;
import com.viglet.turing.wem.config.IHandlerConfiguration;
import com.viglet.turing.wem.mapping.MappingDefinitions;
import com.vignette.logging.context.ContextLogger;

// Open and process Mappping XML File structure
public class MappingDefinitionsProcess {
	private static final ContextLogger log = ContextLogger.getLogger(MappingDefinitionsProcess.class);

	private MappingDefinitionsProcess() {
		throw new IllegalStateException("MappingDefinitionsProcess");
	}

	public static MappingDefinitions loadMappings(String resourceXml) {
		MappingDefinitions mappingDefinitions = null;
		File packageBodyFile = new File(resourceXml);
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(MappingDefinitions.class);
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		mappingDefinitions = (MappingDefinitions) unmarshaller.unmarshal(packageBodyFile);
		} catch (JAXBException e) {
			log.error(e);
		}
	
		return mappingDefinitions;
	}
	
	public static MappingDefinitions getMappingDefinitions(IHandlerConfiguration config) {

		MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.loadMappings(config.getMappingsXML());

		if (mappingDefinitions == null && log.isDebugEnabled())
			log.error("Mapping definitions are not loaded properly from mappingsXML: " + config.getMappingsXML());

		return mappingDefinitions;
	}
	
	public static TuringTagMap mergeCommonAttrs(List<TuringTag> commonIndexAttrs, List<TuringTag> indexAttrs) {

		TuringTagMap indexAttrsMapMerged = new TuringTagMap();

		Map<String, TuringTag> commonIndexAttrMap = new HashMap<String, TuringTag>();
		for (TuringTag turingTag : commonIndexAttrs)
			commonIndexAttrMap.put(turingTag.getTagName(), turingTag);

		for (TuringTag turingTag : indexAttrs) {
			if (turingTag != null) {
				if (commonIndexAttrs != null && turingTag.getSrcClassName() == null
						&& commonIndexAttrMap.get(turingTag.getTagName()) != null) {
					// Common always have one item
					// Add ClassName of Common into Index, if doesn't have ClassName
					turingTag.setSrcClassName(commonIndexAttrMap.get(turingTag.getTagName()).getSrcClassName());
				}

				if (!indexAttrsMapMerged.containsKey(turingTag.getTagName()))
					indexAttrsMapMerged.put(turingTag.getTagName(), new ArrayList<TuringTag>());
				indexAttrsMapMerged.get(turingTag.getTagName()).add(turingTag);
			}
		}

		// Add only Mandatory Attributes
		for (TuringTag commonTuringTag : commonIndexAttrs) {
			// Doesn't repeat tags that exist in Ctd
			if (commonTuringTag.getSrcMandatory() && !indexAttrsMapMerged.containsKey(commonTuringTag.getTagName())) {
				List<TuringTag> turingTags = new ArrayList<TuringTag>();
				turingTags.add(commonTuringTag);
				indexAttrsMapMerged.put(commonTuringTag.getTagName(), turingTags);
			}
		}

		return indexAttrsMapMerged;
	}
}
