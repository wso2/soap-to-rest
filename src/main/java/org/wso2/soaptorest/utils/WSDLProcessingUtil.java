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
package org.wso2.soaptorest.utils;

import com.ibm.wsdl.extensions.schema.SchemaReferenceImpl;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.xerces.impl.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.soaptorest.exceptions.SOAPToRESTException;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Utility class to read the WSDL file from URL or file system safely and read all imported XSD Schemas
 * and return them as {@link Document} and Set of {@link XmlSchema}
 */
public class WSDLProcessingUtil {

    static Logger log = LoggerFactory.getLogger(WSDLProcessingUtil.class);

    /**
     * Returns a secured document builder to avoid XXE attacks
     *
     * @return secured document builder to avoid XXE attacks
     */
    private static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            // Skip throwing the error as this exception doesn't break actual DocumentBuilderFactory creation
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                    + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE, e);
        }
        return dbf;
    }

    /**
     * Returns an "XXE safe" built DOM XML object by reading the content from the provided file path.
     *
     * @param path path to fetch the content
     * @return an "XXE safe" built DOM XML object by reading the content from the provided file path
     * @throws SOAPToRESTException When error occurred while reading from file path
     */
    public static Document getSecuredParsedDocumentFromPath(String path, String systemId) throws SOAPToRESTException, IOException {

        InputStream inputStream = null;
        try {
            DocumentBuilderFactory factory = getSecuredDocumentBuilder();
            DocumentBuilder builder = factory.newDocumentBuilder();
            inputStream = new FileInputStream(path);
            return builder.parse(inputStream, systemId);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new SOAPToRESTException("Error while reading WSDL document", e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static Document getSecuredParsedDocumentFromURL(URL url) throws SOAPToRESTException, IOException {

        InputStream inputStream = null;
        try {
            DocumentBuilderFactory factory = getSecuredDocumentBuilder();
            DocumentBuilder builder = factory.newDocumentBuilder();
            inputStream = url.openStream();
            return builder.parse(inputStream);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new SOAPToRESTException("Error while reading WSDL document", e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * This method will read the given WSDL definition and extract all the referred XSD files and create List of
     * XSD Schemas
     *
     * @param wsdlDefinition input WSDL definition
     * @return returns Set of {@link XmlSchema} objects of all the referred XSDs of the WSDL file
     */
    public static Set<XmlSchema> getXMLSchemasFromWSDL(Definition wsdlDefinition, String systemId) {

        Set<XmlSchema> schemaArrayList = new HashSet<>();
        Types types = wsdlDefinition.getTypes();
        List<?> typeList = new ArrayList<>();
        if (types != null) {
            typeList = types.getExtensibilityElements();
        }
        if (typeList != null) {
            for (Object ext : typeList) {
                if (ext instanceof Schema) {
                    Schema schema = (Schema) ext;
                    XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
                    schemaArrayList.add(schemaCollection.read(schema.getElement(), systemId));
                    // Process imported XSDs if available
                    Map<?, ?> importedSchemas = schema.getImports();
                    if (importedSchemas != null) {
                        processImportedSchemas(importedSchemas, schemaArrayList);
                    }
                    List<?> schemaIncludes = schema.getIncludes();
                    if (schemaIncludes != null) {
                        processIncludedSchemas(schemaIncludes, schemaArrayList);
                    }
                }
            }
        }
        return schemaArrayList;
    }

    private static void processImportedSchemas(Map<?, ?> importedSchemas, Set<XmlSchema> schemaArrayList) {

        for (Object importedSchemaObj : importedSchemas.keySet()) {
            String schemaUrl = (String) importedSchemaObj;
            if (importedSchemas.get(schemaUrl) != null) {
                Vector<?> vector = (Vector<?>) importedSchemas.get(schemaUrl);
                for (Object schemaVector : vector) {
                    if (schemaVector instanceof SchemaImport) {
                        Schema referencedSchema = ((SchemaImport) schemaVector).getReferencedSchema();
                        if (referencedSchema != null && referencedSchema.getElement() != null) {
                            String systemId = referencedSchema.getDocumentBaseURI();
                            XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
                            schemaArrayList.add(schemaCollection.read(referencedSchema.getElement(), systemId));
                            Map<?, ?> nestedImportedSchemas = referencedSchema.getImports();
                            if (nestedImportedSchemas != null) {
                                processImportedSchemas(nestedImportedSchemas, schemaArrayList);
                            }
                            List<?> nestedSchemaIncludes = referencedSchema.getIncludes();
                            if (nestedSchemaIncludes != null) {
                                processIncludedSchemas(nestedSchemaIncludes, schemaArrayList);
                            }
                        } else {
                            log.warn("Cannot access referenced schema for the schema defined at: " + schemaUrl);
                        }
                    }
                }
            }
        }
    }

    private static void processIncludedSchemas(List<?> schemaIncludes, Set<XmlSchema> schemaArrayList) {

        for (Object includedSchemaRef : schemaIncludes) {
            if (includedSchemaRef instanceof SchemaReferenceImpl) {
                Schema referencedSchema = ((SchemaReferenceImpl) includedSchemaRef).getReferencedSchema();

                if (referencedSchema != null && referencedSchema.getElement() != null) {
                    String systemId = referencedSchema.getDocumentBaseURI();
                    XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
                    schemaArrayList.add(schemaCollection.read(referencedSchema.getElement(), systemId));
                    Map<?, ?> nestedImportedSchemas = referencedSchema.getImports();
                    if (nestedImportedSchemas != null) {
                        processImportedSchemas(nestedImportedSchemas, schemaArrayList);
                    }
                    List<?> nestedSchemaIncludes = referencedSchema.getIncludes();
                    if (nestedSchemaIncludes != null) {
                        processIncludedSchemas(nestedSchemaIncludes, schemaArrayList);
                    }
                }
            }
        }
    }
}
