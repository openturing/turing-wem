package com.viglet.turing.util;

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
import java.util.List;

import com.viglet.turing.beans.TuringTag;
import com.vignette.logging.context.ContextLogger;

public class TuringUtils {	
	private static final ContextLogger log = ContextLogger.getLogger(TuringUtils.class);

	/**
	 * Returns the index Tag name
	 * 
	 * @param turingTag
	 * @return String
	 */
	public static String getIndexTagName(TuringTag turingTag) {
		String srcId = null;
		if ((turingTag.getSrcXmlName() == null) && (turingTag.getSrcClassName() != null)) {
			srcId = String.format("CLASSNAME_%s", turingTag.getSrcClassName());
			
		} else {
			srcId = turingTag.getSrcXmlName();
			if (turingTag.getSrcClassName() != null) {
				srcId = String.format("%s_%s", srcId, turingTag.getSrcClassName());
			}
		}

		if (turingTag.getSrcAttributeRelation() != null) {
			srcId = String.format("%s_%s", srcId, turingTag.getSrcAttributeRelation());
		}
		if (log.isDebugEnabled())
			log.debug(String.format("IndexTagName: %s", srcId));

		return srcId;
	}

	public static String listToString(List<String> stringList) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String s : stringList) {
			if (i++ != stringList.size() - 1) {
				sb.append(s);
				sb.append(", ");
			}
		}
		return sb.toString();
	}
}
