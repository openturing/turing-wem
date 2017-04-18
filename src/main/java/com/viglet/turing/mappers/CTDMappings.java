package com.viglet.turing.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.viglet.turing.beans.NsteinTag;
import com.vignette.logging.context.ContextLogger;

public class CTDMappings {
	private HashMap<String, ArrayList<NsteinTag>> commonDataMappings;
	private HashMap<String, ArrayList<NsteinTag>> ctdSpecificMappings;
	private String customClassName = null;
	private String classValidToIndex = null;
	private static final ContextLogger log = ContextLogger.getLogger(CTDMappings.class);

	public ArrayList<NsteinTag> getIndexAttrTag(String ctdAttribute) {

		ArrayList<NsteinTag> indexAttrTags = null;
		if (log.isDebugEnabled()) {
			log.debug("CTDMappings attribute: " + ctdAttribute);
		}

		if (ctdSpecificMappings != null) {
			indexAttrTags = ctdSpecificMappings.get(ctdAttribute);
			if (indexAttrTags != null) {
				for (NsteinTag indexAttrTag : indexAttrTags)
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

	public NsteinTag findIndexTagInMappings(String nsteinTagName) {
		NsteinTag indexAttrTag = null;
		NsteinTag dummyNsteinTag = new NsteinTag(nsteinTagName, null, null, null, null, false, false);
		if (ctdSpecificMappings != null && ctdSpecificMappings.values().contains(dummyNsteinTag)) {
			for (ArrayList<NsteinTag> nTags : ctdSpecificMappings.values()) {
				for (NsteinTag nTag : nTags) {
					if (nTag.getTagName().equals(nsteinTagName)) {
						if (log.isDebugEnabled()) {
							log.debug("Found the value in ctdSpecificMappings");
						}
						indexAttrTag = nTag;
					}
				}
			}
		}
		if (indexAttrTag == null && commonDataMappings != null
				&& commonDataMappings.values().contains(dummyNsteinTag)) {
			for (ArrayList<NsteinTag> nTags : commonDataMappings.values()) {
				for (NsteinTag nTag : nTags) {
					if (nTag.getTagName().equals(nsteinTagName)) {
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
		for (Entry<String, ArrayList<NsteinTag>> entryCtd : ctdSpecificMappings.entrySet()) {
			String keyCtd = entryCtd.getKey();
			for (NsteinTag tagCtd : entryCtd.getValue()) {
				tagCtds.put(tagCtd.getTagName(), keyCtd);
				returnSet.add(keyCtd);
			}
		}

		// Add only Mandatory Attributes
		for (Entry<String, ArrayList<NsteinTag>> entry : commonDataMappings.entrySet()) {
			String key = entry.getKey();
			for (NsteinTag tag : entry.getValue()) {
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

	public CTDMappings(HashMap<String, ArrayList<NsteinTag>> commonDataMappings,
			HashMap<String, ArrayList<NsteinTag>> ctdSpecificMappings) {
		this.commonDataMappings = commonDataMappings;
		this.ctdSpecificMappings = ctdSpecificMappings;
	}

	public void setCommonDataMappings(HashMap<String, ArrayList<NsteinTag>> commonDataMappings) {
		this.commonDataMappings = commonDataMappings;
	}

	public void setCtdSpecificMappings(HashMap<String, ArrayList<NsteinTag>> ctdSpecificMappings) {
		this.ctdSpecificMappings = ctdSpecificMappings;
	}

	public HashMap<String, ArrayList<NsteinTag>> getCommonDataMappings() {
		return commonDataMappings;
	}

	public HashMap<String, ArrayList<NsteinTag>> getCtdSpecificMappings() {
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