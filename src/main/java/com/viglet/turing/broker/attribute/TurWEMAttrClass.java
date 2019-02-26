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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.viglet.turing.beans.TurAttrDef;
import com.viglet.turing.beans.TurAttrDefContext;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.ext.ExtAttributeInterface;
import com.viglet.turing.util.HtmlManipulator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurWEMAttrClass {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMAttrXML.class);

	public static List<TurAttrDef> attributeByClass(TurAttrDefContext turAttrDefContext, AttributeData attributeData)
			throws Exception {
		
		TuringTag turingTag = turAttrDefContext.getTuringTag();
		ContentInstance ci = turAttrDefContext.getContentInstance();
		IHandlerConfiguration config = turAttrDefContext.getiHandlerConfiguration();		
		List<TurAttrDef> attributesDefs = new ArrayList<TurAttrDef>();
		
		if (turingTag.getSrcClassName() != null) {
			String className = turingTag.getSrcClassName();
			if (log.isDebugEnabled())
				log.debug("ClassName : " + className);

			Object extAttribute = Class.forName(className).newInstance();
			TurAttrDef turAttrDef = new TurAttrDef(turingTag.getTagName(),
					Arrays.asList(((ExtAttributeInterface) extAttribute).consume(turingTag, ci, attributeData, config)));
			attributesDefs.add(turAttrDef);
		} else {
			if (turingTag.getSrcAttributeType() != null && turingTag.getSrcAttributeType().equals("html")) {
				TurAttrDef turAttrDef = new TurAttrDef(turingTag.getTagName(),Arrays.asList(HtmlManipulator.Html2Text(attributeData.getValue().toString())));
				attributesDefs.add(turAttrDef);
			} else if (attributeData != null && attributeData.getValue() != null) {
				TurAttrDef turAttrDef = new TurAttrDef(turingTag.getTagName(),Arrays.asList(attributeData.getValue().toString()));
				attributesDefs.add(turAttrDef);				
			}

		}
		return attributesDefs;
	}
}
