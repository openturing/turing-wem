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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.viglet.turing.beans.TurIndexAttrMap;
import com.viglet.turing.beans.TuringTag;
import com.viglet.turing.util.TuringUtils;
import com.vignette.logging.context.ContextLogger;

public class CTDMappings {
	private TurIndexAttrMap commonIndexAttrMap;
	private TurIndexAttrMap indexAttrMap;
	private String customClassName = null;
	private String classValidToIndex = null;
	private static final ContextLogger log = ContextLogger.getLogger(CTDMappings.class);

	public ArrayList<TuringTag> getIndexAttrTag(String ctdAttribute) {

		ArrayList<TuringTag> indexAttrTags = null;
		if (log.isDebugEnabled())
			log.debug("CTDMappings attribute: " + ctdAttribute);

		if (indexAttrMap != null) {
			indexAttrTags = indexAttrMap.get(ctdAttribute);
			if (indexAttrTags != null) {
				for (TuringTag turingTag : indexAttrTags)
					if (commonIndexAttrMap != null && turingTag != null && turingTag.getSrcClassName() == null) {
						if (commonIndexAttrMap.get(TuringUtils.getIndexTagName(turingTag)) != null) {
							// Common always have one item
							turingTag.setSrcClassName(commonIndexAttrMap.get(TuringUtils.getIndexTagName(turingTag))
									.get(0).getSrcClassName());
						}
					}
			}
		}
		if (indexAttrTags == null && commonIndexAttrMap != null)
			indexAttrTags = commonIndexAttrMap.get(ctdAttribute);

		return indexAttrTags;
	}

	/**
	 * @param turingTagName
	 * @return TuringTag
	 */
	public TuringTag findIndexTagInMappings(String turingTagName) {
		TuringTag turingTag = null;

		if (indexAttrMap != null) {
			for (ArrayList<TuringTag> nTags : indexAttrMap.values()) {
				for (TuringTag nTag : nTags) {
					if (nTag.getTagName().equals(turingTagName)) {
						if (log.isDebugEnabled()) {
							log.debug("Found the value in index-attr");
						}
						turingTag = nTag;
					}
				}
			}
		}
		if (turingTag == null && commonIndexAttrMap != null) {
			for (ArrayList<TuringTag> nTags : commonIndexAttrMap.values()) {
				for (TuringTag nTag : nTags) {
					if (nTag.getTagName().equals(turingTagName)) {
						if (log.isDebugEnabled()) {
							log.debug("Found the value in common-index-attr");
						}
						turingTag = nTag;
					}
				}
			}
		}

		return turingTag;
	}

	public Set<String> getIndexAttrs() {
		Set<String> returnSet = new HashSet<String>();

		HashMap<String, String> tagCtds = new HashMap<String, String>();
		for (Entry<String, ArrayList<TuringTag>> entryCtd : indexAttrMap.entrySet()) {
			String keyCtd = entryCtd.getKey();
			for (TuringTag tagCtd : entryCtd.getValue()) {

				tagCtds.put(tagCtd.getTagName(), keyCtd);
				returnSet.add(keyCtd);
			}
		}

		// Add only Mandatory Attributes
		for (Entry<String, ArrayList<TuringTag>> entry : commonIndexAttrMap.entrySet()) {
			String key = entry.getKey();
			for (TuringTag tag : entry.getValue()) {
				// Doesn't repeat tags that exist in Ctd
				if (tag.getSrcMandatory()) {
					if ((!key.startsWith("CLASSNAME_"))
							|| (key.startsWith("CLASSNAME_") && tagCtds.get(tag.getTagName()) == null))
						returnSet.add(key);
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("CTDMappings getIndexAttrs");
			for (String setItem : returnSet)
				log.debug(setItem);
		}

		return returnSet;
	}

	public CTDMappings(TurIndexAttrMap commonIndexAttrMap, TurIndexAttrMap indexAttrMap) {
		this.commonIndexAttrMap = commonIndexAttrMap;
		this.indexAttrMap = indexAttrMap;
	}

	public TurIndexAttrMap getCommonIndexAttrMap() {
		return commonIndexAttrMap;
	}

	public TurIndexAttrMap getIndexAttrMap() {
		return indexAttrMap;
	}

	public String getCustomClassName() {
		return customClassName;
	}

	public void setCustomClassName(String customClassName) {
		this.customClassName = customClassName;
	}

	public String getClassValidToIndex() {
		return classValidToIndex;
	}

	public void setClassValidToIndex(String classValidToIndex) {
		this.classValidToIndex = classValidToIndex;
	}

}