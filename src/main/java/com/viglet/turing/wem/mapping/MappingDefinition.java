package com.viglet.turing.wem.mapping;

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
    "indexAttrs"
})
@XmlRootElement(name = "mappingDefinition")
public class MappingDefinition {

    @XmlElement(name = "index-attrs", required = true)
    protected IndexAttrs indexAttrs;
    @XmlAttribute(name = "contentType", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String contentType;
    @XmlAttribute(name = "validToIndex")
    @XmlSchemaType(name = "anySimpleType")
    protected String validToIndex;

    public IndexAttrs getIndexAttrs() {
        return indexAttrs;
    }

    public void setIndexAttrs(IndexAttrs value) {
        this.indexAttrs = value;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String value) {
        this.contentType = value;
    }

    public String getValidToIndex() {
        return validToIndex;
    }

    public void setValidToIndex(String value) {
        this.validToIndex = value;
    }

}
