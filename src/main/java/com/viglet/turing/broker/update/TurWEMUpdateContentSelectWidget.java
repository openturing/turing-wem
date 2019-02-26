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

import java.util.ArrayList;
import java.util.List;

import com.viglet.turing.beans.TurAttrDef;
import com.viglet.turing.beans.TurAttrDefContext;
import com.viglet.turing.beans.TurCTDMappingMap;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.attribute.TurWEMAttrXML;
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

	public static List<TurAttrDef> attributeContentSelectUpdate(TurAttrDefContext turAttrDefContext,
			AttributeData attributeData) throws Exception {

		MappingDefinitions mappingDefinitions = turAttrDefContext.getMappingDefinitions();
		List<TurAttrDef> attributesDefs = new ArrayList<TurAttrDef>();

		ContentInstance ciRelated = (ContentInstance) ManagedObject
				.findByContentManagementId(new ManagedObjectVCMRef(attributeData.getValue().toString()));
		if (ciRelated != null) {
			String contentTypeName = ciRelated.getObjectType().getData().getName();
			if (log.isDebugEnabled())
				log.debug(String.format("CTD Related: %s", contentTypeName));

			// we force the type on the Viglet Turing side
			TurCTDMappingMap relatedMappings = mappingDefinitions.getMappingDefinitions();

			CTDMappings ctdRelatedMappings = relatedMappings.get(contentTypeName);

			if (ctdRelatedMappings == null && log.isErrorEnabled())
				log.error(String.format("Mapping definition is not found in the mappingXML for the CTD: %s",
						contentTypeName));

			// Process URL from Relation.
			for (String tag : ctdRelatedMappings.getTagList()) {
				TurAttrDefContext turAttrDefContextRelated = new TurAttrDefContext(turAttrDefContext);
				turAttrDefContextRelated.setContentInstance(ciRelated);
				for (TuringTag tagRelated : ctdRelatedMappings.getTuringTagMap().get(tag)) {
					if (tag != null && tagRelated != null && tagRelated.getTagName() != null
							&& tagRelated.getTagName().equals("url")) {
						if (log.isDebugEnabled())
							log.debug(
									String.format("Key Related: %s,  Tag Related: %s, relation: %s, content Type: %s ",
											tag, tagRelated.getTagName(),
											TuringUtils.listToString(tagRelated.getSrcAttributeRelation()),
											tagRelated.getSrcAttributeType()));
						attributesDefs = TurWEMAttrXML.attributeXML(turAttrDefContextRelated);
					}

				}
			}
		}
		return attributesDefs;
	}
}
