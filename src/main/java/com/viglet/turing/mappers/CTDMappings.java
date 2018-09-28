package com.viglet.turing.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.viglet.turing.beans.TuringTag;
import com.vignette.logging.context.ContextLogger;

public class CTDMappings {
	private HashMap<String, ArrayList<TuringTag>> commonDataMappings;
	private HashMap<String, ArrayList<TuringTag>> ctdSpecificMappings;
	private String customClassName = null;
	private String classValidToIndex = null;
	private static final ContextLogger log = ContextLogger.getLogger(CTDMappings.class);

	public ArrayList<TuringTag> getIndexAttrTag(String ctdAttribute) {

		ArrayList<TuringTag> indexAttrTags = null;
		if (log.isDebugEnabled()) {
			log.debug("CTDMappings attribute: " + ctdAttribute);
		}

		if (ctdSpecificMappings != null) {
			indexAttrTags = ctdSpecificMappings.get(ctdAttribute);
			if (indexAttrTags != null) {
				for (TuringTag indexAttrTag : indexAttrTags)
					if (commonDataMappings != null && indexAttrTag != null && indexAttrTag.getSrcClassName() == null) {

						if (commonDataMappings.get("CLASSNAME_" + indexAttrTag.getTagName()) != null) {
							// Common always have one item
							indexAttrTag.setSrcClassName(commonDataMappings
									.get("CLASSNAME_" + indexAttrTag.getTagName()).get(0).getSrcClassName());
						}
					}
			}
		}
		if (indexAttrTags == null && commonDataMappings != null)
			indexAttrTags = commonDataMappings.get(ctdAttribute);

		return indexAttrTags != null ? indexAttrTags : null;
	}

	public TuringTag findIndexTagInMappings(String TuringTagName) {
		TuringTag indexAttrTag = null;
		TuringTag dummyTuringTag = new TuringTag(TuringTagName, null, null, null, null, false, false);
		if (ctdSpecificMappings != null && ctdSpecificMappings.values().contains(dummyTuringTag)) {
			for (ArrayList<TuringTag> nTags : ctdSpecificMappings.values()) {
				for (TuringTag nTag : nTags) {
					if (nTag.getTagName().equals(TuringTagName)) {
						if (log.isDebugEnabled()) {
							log.debug("Found the value in ctdSpecificMappings");
						}
						indexAttrTag = nTag;
					}
				}
			}
		}
		if (indexAttrTag == null && commonDataMappings != null
				&& commonDataMappings.values().contains(dummyTuringTag)) {
			for (ArrayList<TuringTag> nTags : commonDataMappings.values()) {
				for (TuringTag nTag : nTags) {
					if (nTag.getTagName().equals(TuringTagName)) {
						if (log.isDebugEnabled()) {
							log.debug("Found the value in commonDataMappings");
						}
						indexAttrTag = nTag;
					}
				}
			}
		}

		return indexAttrTag != null ? indexAttrTag : null;
	}

	public Set<String> getIndexAttrs() {
		Set<String> returnSet = new HashSet<String>();

		HashMap<String, String> tagCtds = new HashMap<String, String>();
		for (Entry<String, ArrayList<TuringTag>> entryCtd : ctdSpecificMappings.entrySet()) {
			String keyCtd = entryCtd.getKey();
			for (TuringTag tagCtd : entryCtd.getValue()) {

				tagCtds.put(tagCtd.getTagName(), keyCtd);
				returnSet.add(keyCtd);
			}
		}

		// Add only Mandatory Attributes
		for (Entry<String, ArrayList<TuringTag>> entry : commonDataMappings.entrySet()) {
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
			for (String setItem : returnSet) {
				log.debug(setItem);
			}
		}

		return returnSet;
	}

	public CTDMappings(HashMap<String, ArrayList<TuringTag>> commonDataMappings,
			HashMap<String, ArrayList<TuringTag>> ctdSpecificMappings) {
		this.commonDataMappings = commonDataMappings;
		this.ctdSpecificMappings = ctdSpecificMappings;
	}

	public HashMap<String, ArrayList<TuringTag>> getCommonDataMappings() {
		return commonDataMappings;
	}

	public HashMap<String, ArrayList<TuringTag>> getCtdSpecificMappings() {
		return ctdSpecificMappings;
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