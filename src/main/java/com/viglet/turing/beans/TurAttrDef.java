package com.viglet.turing.beans;

import java.util.List;

public class TurAttrDef {
	private String tagName;
	private List<String> multiValue;

	public TurAttrDef (String tagName, List<String> multiValue) {
		this.tagName = tagName;
		this.multiValue = multiValue;
	}
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public List<String> getMultiValue() {
		return multiValue;
	}

	public void setMultiValue(List<String> multiValue) {
		this.multiValue = multiValue;
	}

}
