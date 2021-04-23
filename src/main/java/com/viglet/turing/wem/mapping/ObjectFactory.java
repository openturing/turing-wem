//
// Este arquivo foi gerado pela Arquitetura JavaTM para Implementação de Referência (JAXB) de Bind XML, v2.2.8-b130911.1802 
// Consulte <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas as modificações neste arquivo serão perdidas após a recompilação do esquema de origem. 
// Gerado em: 2021.04.22 às 10:00:28 PM BRT 
//


package com.viglet.turing.wem.mapping;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.viglet.turing.wem.mapping package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Tag_QNAME = new QName("", "tag");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.viglet.turing.wem.mapping
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MappingDefinition }
     * 
     */
    public MappingDefinition createMappingDefinition() {
        return new MappingDefinition();
    }

    /**
     * Create an instance of {@link IndexAttrs }
     * 
     */
    public IndexAttrs createIndexAttrs() {
        return new IndexAttrs();
    }

    /**
     * Create an instance of {@link SrcAttr }
     * 
     */
    public SrcAttr createSrcAttr() {
        return new SrcAttr();
    }

    /**
     * Create an instance of {@link Attr }
     * 
     */
    public Attr createAttr() {
        return new Attr();
    }

    /**
     * Create an instance of {@link MappingDefinitions }
     * 
     */
    public MappingDefinitions createMappingDefinitions() {
        return new MappingDefinitions();
    }

    /**
     * Create an instance of {@link MiscAttrs }
     * 
     */
    public MiscAttrs createMiscAttrs() {
        return new MiscAttrs();
    }

    /**
     * Create an instance of {@link CommonIndexAttrs }
     * 
     */
    public CommonIndexAttrs createCommonIndexAttrs() {
        return new CommonIndexAttrs();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "tag")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createTag(String value) {
        return new JAXBElement<String>(_Tag_QNAME, String.class, null, value);
    }

}
