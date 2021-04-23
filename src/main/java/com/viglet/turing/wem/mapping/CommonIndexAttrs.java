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
    "srcAttr"
})
@XmlRootElement(name = "common-index-attrs")
public class CommonIndexAttrs {

    @XmlElement(required = true)
    protected List<SrcAttr> srcAttr;

    public List<SrcAttr> getSrcAttr() {
        if (srcAttr == null) {
            srcAttr = new ArrayList<SrcAttr>();
        }
        return this.srcAttr;
    }

}
