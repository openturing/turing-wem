package com.viglet.turing.beans;

import java.util.List;

public class TuringTag {
	private String tagName;
	private List<String> srcAttributeRelation;
	private String srcAttributeType;
	private String srcAttribute;
	private String srcClassName;
	private boolean srcMandatory;
	private boolean srcUniqueValues;

	public TuringTag(String tagName, String srcAttribute, String srcAttributeType, List<String> srcAttributeRelation,
			String srcClassName, boolean srcMandatory, boolean srcUniqueValues) {
		setTagName(tagName);
		setSrcAttribute(srcAttribute);
		setSrcAttributeType(srcAttributeType);
		setSrcAttributeRelation(srcAttributeRelation);
		setSrcClassName(srcClassName);
		setSrcMandatory(srcMandatory);
		setSrcUniqueValues(srcUniqueValues);
	}

	@Override
	public String toString() {
		return String.format(
				"tagName: %s, srcAttr: %s, srcAttrRelation: %s, srcAttrType: %s, className: %s, mandatory: %s",
				this.getTagName(), this.getSrcAttribute(), this.getSrcAttributeRelation(), this.getSrcAttributeType(),
				this.getSrcClassName(), this.getSrcMandatory());
	}

	public TuringTag() {
	}

	public String getSrcAttributeType() {
		return srcAttributeType;
	}

	public List<String> getSrcAttributeRelation() {
		return srcAttributeRelation;
	}

	public String getSrcClassName() {
		return srcClassName;
	}

	public boolean getSrcMandatory() {
		return srcMandatory;
	}

	public String getTagName() {
		return tagName;
	}

	public void setSrcAttributeType(String srcAttributeType) {
		this.srcAttributeType = srcAttributeType;
	}

	public void setSrcAttributeRelation(List<String> srcAttributeRelation) {
		this.srcAttributeRelation = srcAttributeRelation;
	}

	public void setSrcClassName(String srcClassName) {
		this.srcClassName = srcClassName;
	}

	public void setSrcMandatory(boolean srcMandatory) {
		this.srcMandatory = srcMandatory;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public void setSrcAttribute(String srcAttribute) {
		this.srcAttribute = srcAttribute;
	}

	public String getSrcAttribute() {
		return srcAttribute;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TuringTag) {
			return this.getTagName().equals(((TuringTag) obj).getTagName());
		} else {
			return false;
		}
	}

	public boolean isSrcUniqueValues() {
		return srcUniqueValues;
	}

	public void setSrcUniqueValues(boolean srcUniqueValues) {
		this.srcUniqueValues = srcUniqueValues;
	}
}
