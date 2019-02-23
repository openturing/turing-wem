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
package com.viglet.turing.util;

import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.viglet.turing.beans.TuringTagMap;
import com.viglet.turing.beans.TuringTag;
import com.vignette.logging.context.ContextLogger;

public class TuringUtils {	
	private static final ContextLogger log = ContextLogger.getLogger(TuringUtils.class);

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
	
	// Old turIndexAttMapToSet
	public static Set<TuringTag> turingTagMapToSet(TuringTagMap turingTagMap) {
		Set<TuringTag> turingTags = new HashSet<TuringTag>(); 
		for (Entry<String, ArrayList<TuringTag>> entryCtd : turingTagMap.entrySet()) {
			for (TuringTag turingTag : entryCtd.getValue()) {
				turingTags.add(turingTag);
			}
		}
		return turingTags;
	}
}
