package com.viglet.turing.beans;

public class TuringTag {
    private String tagName;
    private String srcAttributeRelation;
    private String srcAttributeType;
    private String srcAttribute;
    private String srcClassName;
    private boolean srcMandatory;
    private boolean srcUniqueValues;
    
    public TuringTag(String tagName,String srcAttribute,String srcAttributeType, String srcAttributeRelation, String srcClassName, boolean srcMandatory, boolean srcUniqueValues) {
        setTagName(tagName);
        setSrcAttribute(srcAttribute);
        setSrcAttributeType(srcAttributeType);
        setSrcAttributeRelation(srcAttributeRelation);
        setSrcClassName(srcClassName);
        setSrcMandatory(srcMandatory);
        setSrcUniqueValues(srcUniqueValues);
    }

    public TuringTag() {
    }

    public String getSrcAttributeType() {
        return srcAttributeType;
    }
    
    public String getSrcAttributeRelation() {
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

    public void setSrcAttributeRelation(String srcAttributeRelation) {
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
    public boolean equals(Object obj){
        if(obj instanceof TuringTag){
            return this.getTagName().equals(((TuringTag)obj).getTagName());
        }else{
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
