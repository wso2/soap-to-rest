/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.soaptorest;

import org.apache.ws.commons.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.soaptorest.exceptions.SOAPToRESTException;
import org.wso2.soaptorest.models.*;
import org.wso2.soaptorest.utils.WSDLProcessingUtil;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The class that processes WSDL 1.1 documents
 */
public class WSDLProcessor {

    private static final String JAVAX_WSDL_VERBOSE_MODE = "javax.wsdl.verbose";
    private static final String JAVAX_WSDL_IMPORT_DOCUMENTS = "javax.wsdl.importDocuments";
    private static volatile WSDLFactory wsdlFactoryInstance;
    Logger log = LoggerFactory.getLogger(WSDLProcessor.class);
    Set<XmlSchema> wsdlSchemaList;
    List<XSModel> xsdDataModels = new ArrayList<>();
    private Definition wsdlDefinition;

    public static WSDLFactory getWsdlFactoryInstance() throws SOAPToRESTException {

        if (wsdlFactoryInstance == null) {
            try {
                synchronized (WSDLProcessor.class) {
                    if (wsdlFactoryInstance == null) {
                        wsdlFactoryInstance = WSDLFactory.newInstance();
                    }
                }
            } catch (WSDLException e) {
                throw new SOAPToRESTException("Error while instantiating WSDL 1.1 factory", e);
            }
        }
        return wsdlFactoryInstance;
    }

    /**
     * Initialize the processor based on a provided file path which contains WSDL file.
     *
     * @param path File path with WSDL file
     * @throws SOAPToRESTException Unexpected error while initialization
     */
    void init(String path) throws SOAPToRESTException {

        WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();

        //Switch off the verbose mode
        wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
        try {
            wsdlDefinition = wsdlReader.readWSDL(null, WSDLProcessingUtil.getSecuredParsedDocumentFromPath(path));
            initializeModels(wsdlDefinition);
            if (log.isDebugEnabled()) {
                log.debug("Successfully initialized the WSDL File from given path");
            }
        } catch (WSDLException | IOException e) {
            //This implementation class cannot process the WSDL.
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            throw new SOAPToRESTException("Cannot process the provide WSDL file", e);
        }
    }

    /**
     * Initialize the processor based on a provided URL of the WSDL file.
     *
     * @param url URL of the WSDL file
     * @throws SOAPToRESTException Unexpected error while initialization
     */
    void init(URL url) throws SOAPToRESTException {

        WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();

        //Switch off the verbose mode
        wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
        try {
            wsdlDefinition = wsdlReader.readWSDL(url.toString(),
                    WSDLProcessingUtil.getSecuredParsedDocumentFromURL(url));
            initializeModels(wsdlDefinition);
            if (log.isDebugEnabled()) {
                log.debug("Successfully initialized the WSDL File from given URL");
            }
        } catch (WSDLException | IOException e) {
            //This implementation class cannot process the WSDL.
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            throw new SOAPToRESTException("Cannot process the provide WSDL file", e);
        }
    }

    /**
     * This method will extract the XSD Schemas from the WSDL file and generate the data model
     *
     * @param wsdlDefinition input WSDL definition
     */
    public void initializeModels(Definition wsdlDefinition) {

        wsdlSchemaList = WSDLProcessingUtil.getXMLSchemasFromWSDL(wsdlDefinition);
        for (XmlSchema xmlSchema : wsdlSchemaList) {
            // Process single XSD Schema file from the available schema list
            XSModel xsModel = new XSModel();
            xsModel.setTargetNamespace(xmlSchema.getTargetNamespace());

            //Process Elements in the XSD
            Iterator<?> elementsIterator = xmlSchema.getElements().getValues();
            while (elementsIterator.hasNext()) {
                Object xmlSchemaObject = elementsIterator.next();
                if (xmlSchemaObject instanceof XmlSchemaElement) {
                    XSElement xsElement = processXmlSchemaElement((XmlSchemaElement) xmlSchemaObject);
                    xsModel.addElement(xsElement);
                }
            }

            //Process Attributes in the XSD
            Iterator<?> attributesIterator = xmlSchema.getAttributes().getValues();
            while (attributesIterator.hasNext()) {
                Object xmlSchemaObject = attributesIterator.next();
                if (xmlSchemaObject instanceof XmlSchemaAttribute) {
                    XSAttribute xsAttribute = processXmlSchemaAttribute((XmlSchemaAttribute) xmlSchemaObject);
                    xsModel.addAttribute(xsAttribute);
                }
            }

            //Process XSD Group data type
            Iterator<?> groupIterator = xmlSchema.getGroups().getValues();
            while (groupIterator.hasNext()) {
                XSSequence xsSequence = new XSSequence();
                XSGroup xsGroup = new XSGroup();
                Object schemaGroupObject = groupIterator.next();
                if (schemaGroupObject instanceof XmlSchemaGroup) {
                    XmlSchemaGroup xmlSchemaGroup = (XmlSchemaGroup) schemaGroupObject;
                    int numOfGroupElements = xmlSchemaGroup.getParticle().getItems().getCount();
                    for (int i = 0; i < numOfGroupElements; i++) {
                        XmlSchemaObject xmlSchemaObject = xmlSchemaGroup.getParticle().getItems().getItem(i);
                        if (xmlSchemaObject instanceof XmlSchemaElement) {
                            XSElement xsElement = processXmlSchemaElement((XmlSchemaElement) xmlSchemaObject);
                            xsSequence.addElement(xsElement);
                        }
                    }
                    xsGroup.addSequence(xsSequence);
                }
                xsModel.addGroup(xsGroup);
            }

            //Process Data Types defined in the schema
            Iterator<?> schemaTypeIterator = xmlSchema.getSchemaTypes().getValues();
            while (schemaTypeIterator.hasNext()) {
                Object schemaTypeObject = schemaTypeIterator.next();
                if (schemaTypeObject instanceof XmlSchemaType) {
                    XSDataType xsDataType = processXSDataType((XmlSchemaType) schemaTypeObject);
                    xsModel.addXSDataType(xsDataType);
                }

            }
            xsdDataModels.add(xsModel);
        }
    }

    private XSElement processXmlSchemaElement(XmlSchemaElement xmlSchemaElement) {

        XSElement xsElement = new XSElement();
        xsElement.setName(xmlSchemaElement.getQName());
        xsElement.setArray(xmlSchemaElement.getMaxOccurs() > 1);
        if (xmlSchemaElement.getSchemaTypeName() != null) {
            xsElement.setType(xmlSchemaElement.getSchemaTypeName());
        } else if (xmlSchemaElement.getSchemaType() != null) {
            // If the schema type is inline, then the Schema type name will be null
            XSDataType inlineComplexType = processXSDataType(xmlSchemaElement.getSchemaType());
            xsElement.setInlineComplexType(inlineComplexType);
        } else if (xmlSchemaElement.getRefName() != null) {
            xsElement.setRefKey(xmlSchemaElement.getRefName());
        } else {
            log.warn("Data type for the child element " + xmlSchemaElement.getName() + "did " + "not processed");
        }
        return xsElement;
    }

    public XSDataType processXSDataType(XmlSchemaType xmlSchemaType) {

        XSDataType xsDataType = new XSDataType();
        if (xmlSchemaType.getName() != null) {
            xsDataType.setName(xmlSchemaType.getQName());
        }
        if (xmlSchemaType instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType complexType = (XmlSchemaComplexType) xmlSchemaType;

            //Process SimpleContent and ComplexContent
            XmlSchemaContentModel xmlSchemaContentModel = complexType.getContentModel();
            if (xmlSchemaContentModel != null) {
                XmlSchemaContent xmlSchemaContent = xmlSchemaContentModel.getContent();
                if (xmlSchemaContent instanceof XmlSchemaSimpleContentExtension) {
                    XmlSchemaSimpleContentExtension simpleContentExtension =
                            (XmlSchemaSimpleContentExtension) xmlSchemaContent;
                    xsDataType.setExtensionBase(simpleContentExtension.getBaseTypeName());
                } else if (xmlSchemaContent instanceof XmlSchemaComplexContentExtension) {
                    XmlSchemaComplexContentExtension complexContentExtension =
                            (XmlSchemaComplexContentExtension) xmlSchemaContent;
                    xsDataType.setExtensionBase(complexContentExtension.getBaseTypeName());
                }
            }

            // Process child elements of the complexTypes
            XmlSchemaParticle xmlSchemaParticle = complexType.getParticle();
            // Process XSD All and Sequence as single type since no need to support sequence in openAPI
            if (xmlSchemaParticle instanceof XmlSchemaAll || xmlSchemaParticle instanceof XmlSchemaSequence) {
                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) complexType.getParticle();
                int numElements = xmlSchemaGroupBase.getItems().getCount();
                XSSequence xsSequence = new XSSequence();
                for (int i = 0; i < numElements; i++) {
                    XmlSchemaObject xmlSchemaObject = xmlSchemaGroupBase.getItems().getItem(i);
                    if (xmlSchemaObject instanceof XmlSchemaElement) {
                        XSElement xsElement = processXmlSchemaElement((XmlSchemaElement) xmlSchemaObject);
                        xsSequence.addElement(xsElement);
                    }
                }
                xsDataType.setSequence(xsSequence);
            }

            //Process Choice types
            if (xmlSchemaParticle instanceof XmlSchemaChoice) {
                XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice) xmlSchemaParticle;
                int numElements = xmlSchemaChoice.getItems().getCount();
                XSChoice xsChoice = new XSChoice();
                for (int i = 0; i < numElements; i++) {
                    XmlSchemaObject xmlSchemaObject = xmlSchemaChoice.getItems().getItem(i);
                    if (xmlSchemaObject instanceof XmlSchemaElement) {
                        XSElement xsElement = processXmlSchemaElement((XmlSchemaElement) xmlSchemaObject);
                        xsChoice.addElement(xsElement);
                    }
                }
                xsDataType.setChoice(xsChoice);
            }

            //Process Group types
            if (xmlSchemaParticle instanceof XmlSchemaGroupRef) {
                XmlSchemaGroupRef xmlSchemaGroupRef = (XmlSchemaGroupRef) xmlSchemaParticle;
                XSGroup xsGroup = new XSGroup();
                xsGroup.setRefKey(xmlSchemaGroupRef.getRefName());
                xsDataType.setGroup(xsGroup);
            }
        }
        return xsDataType;
    }

    private XSAttribute processXmlSchemaAttribute(XmlSchemaAttribute xmlSchemaAttribute) {

        XSAttribute xsAttribute = new XSAttribute();
        xsAttribute.setName(xmlSchemaAttribute.getQName());
        if (xmlSchemaAttribute.getSchemaTypeName() != null) {
            xsAttribute.setType(xmlSchemaAttribute.getSchemaTypeName());
        } else if (xmlSchemaAttribute.getSchemaType() != null) {
            // If the schema type is inline, then the Schema type name will be null
            XSDataType inlineComplexType = processXSDataType(xmlSchemaAttribute.getSchemaType());
            xsAttribute.setInlineComplexType(inlineComplexType);
        } else if (xmlSchemaAttribute.getRefName() != null) {
            xsAttribute.setRefKey(xmlSchemaAttribute.getRefName());
        } else {
            log.warn("Data type for the child element " + xmlSchemaAttribute.getName() + "did " + "not processed");
        }
        return xsAttribute;
    }

    public Definition getWsdlDefinition() {

        return wsdlDefinition;
    }
}
