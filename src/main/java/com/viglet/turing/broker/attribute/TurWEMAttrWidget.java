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
import java.util.List;

import com.viglet.turing.beans.TurAttrDefContext;
import com.viglet.turing.beans.TurAttrDefMap;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.broker.relator.TurWEMRelator;
import com.viglet.turing.broker.update.TurWEMUpdateContentSelectWidget;
import com.viglet.turing.broker.update.TurWEMUpdateFileWidget;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.logging.context.ContextLogger;

public class TurWEMAttrWidget {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMAttrWidget.class);

	public static TurAttrDefMap attributeByWidget(TurAttrDefContext turAttrDefContext, AttributeData attributeData)
			throws Exception {
		TuringTag turingTag = turAttrDefContext.getTuringTag();
		ContentInstance ci = turAttrDefContext.getContentInstance();
		String key = turAttrDefContext.getKey();
		String widgetName = null;

		if (turingTag.getSrcAttributeRelation() != null && turingTag.getSrcAttributeRelation().size() > 0) {
			AttributedObject[] relation = ci.getRelations(turingTag.getSrcAttributeRelation().get(0));

			if (turingTag.getSrcAttributeRelation().size() > 1) {
				List<AttributedObject[]> nestedRelation = new ArrayList<AttributedObject[]>();
				nestedRelation.add(relation);
				relation = TurWEMRelator.nestedRelators(turingTag.getSrcAttributeRelation(), nestedRelation, 0);
			}

			if (relation.length > 0) {
				widgetName = relation[0].getAttribute(key).getAttributeDefinition().getWidgetName();
			}

		} else {
			widgetName = ci.getAttribute(key).getAttributeDefinition().getWidgetName();
		}

		if (widgetName != null && widgetName.equals("WCMContentSelectWidget")
				&& attributeData.getValue().toString().length() == 40 && turingTag.getSrcClassName() == null) {
			if (log.isDebugEnabled()) {
				log.debug("WCMContentSelectWidget value: " + attributeData.getValue().toString());
				log.debug("WCMContentSelectWidget length: " + attributeData.getValue().toString().length());
			}
			return TurWEMUpdateContentSelectWidget.attributeContentSelectUpdate(turAttrDefContext,
					attributeData);

		} else if (widgetName != null && widgetName.equals("VCMFileWidget") && turingTag.getSrcClassName() == null) {
			if (log.isDebugEnabled()) {
				log.debug("VCMFileWidget value: " + attributeData.getValue().toString());
				log.debug("VCMFileWidget length: " + attributeData.getValue().toString().length());
			}
			return TurWEMUpdateFileWidget.attributeFileWidgetUpdate(turAttrDefContext,
					attributeData);
		} else {
			return TurWEMAttrClass.attributeByClass(turAttrDefContext,
					attributeData);
		}

	}
}
