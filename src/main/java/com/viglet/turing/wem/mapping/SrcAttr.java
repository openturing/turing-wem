package com.viglet.turing.wem.mapping;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "tag"
})
@XmlRootElement(name = "srcAttr")
public class SrcAttr {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected List<String> tag;
    @XmlAttribute(name = "className")
    @XmlSchemaType(name = "anySimpleType")
    protected String className;
    @XmlAttribute(name = "mandatory")
    protected Boolean mandatory;
    @XmlAttribute(name = "relation")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String relation;
    @XmlAttribute(name = "uniqueValues")
    protected Boolean uniqueValues;
    @XmlAttribute(name = "valueType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String valueType;
    @XmlAttribute(name = "xmlName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String xmlName;

    public List<String> getTag() {
        if (tag == null) {
            tag = new ArrayList<String>();
        }
        return this.tag;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String value) {
        this.className = value;
    }

    public Boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean value) {
        this.mandatory = value;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String value) {
        this.relation = value;
    }

    public Boolean isUniqueValues() {
        return uniqueValues;
    }

    public void setUniqueValues(Boolean value) {
        this.uniqueValues = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String value) {
        this.valueType = value;
    }

    public String getXmlName() {
        return xmlName;
    }

    public void setXmlName(String value) {
        this.xmlName = value;
    }

}
