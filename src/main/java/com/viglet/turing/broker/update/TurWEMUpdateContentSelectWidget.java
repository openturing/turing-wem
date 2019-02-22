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
package com.viglet.turing.broker.update;

import java.util.HashMap;
import java.util.List;

import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.attribute.TurWEMAttrXML;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.exceptions.MappingNotFoundException;
import com.viglet.turing.mappers.CTDMappings;
import com.viglet.turing.mappers.MappingDefinitions;
import com.viglet.turing.util.TuringUtils;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.logging.context.ContextLogger;

public class TurWEMUpdateContentSelectWidget {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMUpdateContentSelectWidget.class);

	public static HashMap<String, List<String>> attributeContentSelectUpdate(ContentInstance ci,
			HashMap<String, List<String>> attributesDefs, TuringTag tag, String key, AttributeData attributeData,
			IHandlerConfiguration config, MappingDefinitions mappingDefinitions) throws Exception {

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

						attributesDefs = TurWEMAttrXML.attributeXML(ciRelated, attributesDefs, tagRelated, keyRelated,
								config, mappingDefinitions);

					} else {
						// Teste123
						attributesDefs = TurWEMAttrXML.attributeXML(ciRelated, attributesDefs, tagRelated, keyRelated,
								config, mappingDefinitions);

					}
				}
			}
		}
		return attributesDefs;
	}
}
