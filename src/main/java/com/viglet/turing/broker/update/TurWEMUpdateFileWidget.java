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

import com.viglet.turing.beans.TurAttrDefContext;
import com.viglet.turing.beans.TurAttrDefMap;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.config.IHandlerConfiguration;
import com.viglet.turing.util.ETLTuringTranslator;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.javabean.ContentInstance;

import com.vignette.logging.context.ContextLogger;

public class TurWEMUpdateFileWidget {
	private static final ContextLogger log = ContextLogger.getLogger(TurWEMUpdateFileWidget.class);

	public static TurAttrDefMap attributeFileWidgetUpdate(TurAttrDefContext turAttrDefContext,
			AttributeData attributeData) throws Exception {
		
		TuringTag turingTag = turAttrDefContext.getTuringTag();
		ContentInstance ci = turAttrDefContext.getContentInstance();
		IHandlerConfiguration config = turAttrDefContext.getiHandlerConfiguration();	
		
		if (log.isDebugEnabled()) {
			log.debug("TurWEMUpdateFileWidget started");
		}
		
		ETLTuringTranslator etlTranslator = new ETLTuringTranslator(config);
		TurAttrDefMap attributesDefs = new TurAttrDefMap();
		
		if (turingTag.getSrcClassName() == null) {
			if (attributesDefs.get(turingTag.getTagName()) == null)
				attributesDefs.put(turingTag.getTagName(), new ArrayList<String>());

			String url = etlTranslator.getSiteDomain(ci) + attributeData.getValue().toString();
			if (log.isDebugEnabled())
				log.debug("TurWEMUpdateFileWidget url" + url);
				
			attributesDefs.get(turingTag.getTagName()).add(url);

		}

		if (log.isDebugEnabled())
			log.debug("TurWEMUpdateFileWidget finished");

		return attributesDefs;

	}
}
