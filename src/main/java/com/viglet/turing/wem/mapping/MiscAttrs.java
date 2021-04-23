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
    "attr"
})
@XmlRootElement(name = "misc-attrs")
public class MiscAttrs {

    @XmlElement(required = true)
    protected List<Attr> attr;

    public List<Attr> getAttr() {
        if (attr == null) {
            attr = new ArrayList<Attr>();
        }
        return this.attr;
    }

}
