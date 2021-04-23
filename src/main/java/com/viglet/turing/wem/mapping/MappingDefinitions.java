package com.viglet.turing.wem.mapping;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "miscAttrs",
    "commonIndexAttrs",
    "mappingDefinition"
})
@XmlRootElement(name = "mappingDefinitions")
public class MappingDefinitions {

    @XmlElement(name = "misc-attrs", required = true)
    protected MiscAttrs miscAttrs;
    @XmlElement(name = "common-index-attrs", required = true)
    protected CommonIndexAttrs commonIndexAttrs;
    @XmlElement(required = true)
    protected List<MappingDefinition> mappingDefinition;

    public MiscAttrs getMiscAttrs() {
        return miscAttrs;
    }

    public void setMiscAttrs(MiscAttrs value) {
        this.miscAttrs = value;
    }

    public CommonIndexAttrs getCommonIndexAttrs() {
        return commonIndexAttrs;
    }

    public void setCommonIndexAttrs(CommonIndexAttrs value) {
        this.commonIndexAttrs = value;
    }

    public List<MappingDefinition> getMappingDefinition() {
        if (mappingDefinition == null) {
            mappingDefinition = new ArrayList<MappingDefinition>();
        }
        return this.mappingDefinition;
    }

}
