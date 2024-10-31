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

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.soaptorest.exceptions.SOAPToRESTException;
import org.wso2.soaptorest.models.SOAPRequestElement;
import org.wso2.soaptorest.models.SOAPtoRESTConversionData;
import org.wso2.soaptorest.utils.ListJSONPaths;
import org.wso2.soaptorest.utils.SOAPToRESTConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.soaptorest.utils.SOAPToRESTConstants.ATTRIBUTE_PLACEHOLDER;
import static org.wso2.soaptorest.utils.SOAPToRESTConstants.IF_PLACEHOLDER;
import static org.wso2.soaptorest.utils.SOAPToRESTConstants.IS_EMPTY_ATTRIBUTE;
import static org.wso2.soaptorest.utils.SOAPToRESTConstants.QUESTION_MARK_PLACEHOLDER;
import static org.wso2.soaptorest.utils.SOAPToRESTConstants.VALUE_ATTRIBUTE;

/**
 * Class that reads OpenAPI and generate soap request payloads
 */
public class SOAPRequestBodyGenerator {

    private static final Logger log = LoggerFactory.getLogger(SOAPRequestBodyGenerator.class);
    private static String soapMessageType = SOAPToRESTConstants.EMPTY_STRING;
    private static String soapStyle = SOAPToRESTConstants.EMPTY_STRING;

    /**
     * Generates {@link SOAPtoRESTConversionData} with a map of SOAP payloads with JSON paths of REST payloads for
     * all generated REST endpoint. Mapping is done using Operation ID of REST service.
     *
     * @param openAPI open api definition of api
     * @return SOAPtoRESTConversionData Object that represent the OpenAPI with SOAP payloads which are needed
     * for SOAP backend calls
     * @throws SOAPToRESTException throws {@link SOAPToRESTException} if exception occur while generating SOAP
     *                             payloads
     */
    public static SOAPtoRESTConversionData generateSOAPtoRESTConversionObjectFromOAS(OpenAPI openAPI,
                                                                                     String soapService,
                                                                                     String soapPort) throws
            SOAPToRESTException {

        Map<String, SOAPRequestElement> requestBodies = new HashMap<>();
        Paths paths = openAPI.getPaths();

        // Configure serializers
        SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);
        Yaml.mapper().registerModule(simpleModule);
        Map<String,String> jsonPathAndSchemaMap = new HashMap<>();

        for (String pathName : paths.keySet()) {
            PathItem path = paths.get(pathName);
            List<Operation> operationMap = path.readOperations();
            for (Operation operation : operationMap) {
                ArrayList<String> parameterJsonPathMapping = new ArrayList<>();
                Map<String, String> queryParameters = new HashMap<>();
                String operationId = operation.getOperationId();

                //get vendor extensions
                Map<String, Object> vendorExtensions = operation.getExtensions();
                Object vendorExtensionObj = vendorExtensions.get(SOAPToRESTConstants.WSO2_SOAP);

                String soapAction = SOAPToRESTConstants.EMPTY_STRING;
                String namespace = SOAPToRESTConstants.EMPTY_STRING;
                String soapVersion = SOAPToRESTConstants.EMPTY_STRING;
                if (vendorExtensionObj != null) {
                    soapAction =
                            (String) ((HashMap<?, ?>) vendorExtensionObj).get(SOAPToRESTConstants.SOAP_ACTION);
                    namespace =
                            (String) ((HashMap<?, ?>) vendorExtensionObj).get(SOAPToRESTConstants.NAMESPACE);
                    soapVersion =
                            (String) ((HashMap<?, ?>) vendorExtensionObj).get(SOAPToRESTConstants.SOAP_VERSION);
                    soapMessageType =
                            (String) ((HashMap<?, ?>) vendorExtensionObj).get(SOAPToRESTConstants.SOAP_MESSAGE_TYPE);
                    soapStyle =
                            (String) ((HashMap<?, ?>) vendorExtensionObj).get(SOAPToRESTConstants.SOAP_STYLE);
                }
                String soapNamespace = SOAPToRESTConstants.SOAP12_NAMESPACE;
                if (StringUtils.isNotBlank(soapVersion) && SOAPToRESTConstants.SOAP_VERSION_11.equals(soapVersion)) {
                    soapNamespace = SOAPToRESTConstants.SOAP11_NAMESPACE;
                }

                List<Parameter> parameters = operation.getParameters();

                if (parameters != null) {
                    for (Parameter parameter : parameters) {
                        String name = parameter.getName();

                        if (parameter instanceof QueryParameter) {
                            String type = parameter.getSchema().getType();
                            queryParameters.put(name, type);
                        }
                    }
                } else {
                    try {
                        Schema<?> model =
                                operation.getRequestBody().getContent().get(SOAPToRESTConstants.
                                        DEFAULT_CONTENT_TYPE).getSchema();
                        Example example = ExampleBuilder.fromSchema(model, openAPI.getComponents().getSchemas());
                        parameterJsonPathMapping = ListJSONPaths.getJsonPathsFromExample(example, jsonPathAndSchemaMap);
                    } catch (Exception e) {
                        throw new SOAPToRESTException("Cannot generate JSON body from the OpenAPI", e);
                    }

                }

                Document soapRequestBody = createSOAPRequestXMLForOperation(parameterJsonPathMapping, queryParameters,
                        namespace, operationId, openAPI, jsonPathAndSchemaMap );

                iterateChildNodes(soapRequestBody.getDocumentElement(), soapRequestBody);
                requestBodies.put(operationId, new SOAPRequestElement(soapRequestBody, soapAction, namespace,
                        soapNamespace));
            }
        }
        return new SOAPtoRESTConversionData(openAPI, requestBodies, soapService, soapPort);
    }

    /**
     * Iterate through the given document and wrap the possible empty elements with <#if> statements.
     * @param node      Current node
     * @param document  Root document
     */
    private static void iterateChildNodes(Node node, Document document) {
        // Get the child nodes of the current node
        NodeList childNodes = node.getChildNodes();

        // Iterate over the child nodes
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            // Check if the element has the attribute "addIsEmptyCheck" with value "true"
            if (childNode instanceof Element) {
                Element element = (Element) childNode;
                if (element.hasAttribute(IS_EMPTY_ATTRIBUTE) &&
                        element.getAttribute(IS_EMPTY_ATTRIBUTE).equals("true")) {
                    // Create a new element <#if>
                    String value = null;
                    if (element.hasAttribute(VALUE_ATTRIBUTE)) {
                        String nodeName = element.getNodeName();
                        // remove namespace from the node name
                        if (nodeName.contains(":")) {
                            nodeName = nodeName.split(":")[1];
                        }
                        value = "${payload." + element.getAttribute(VALUE_ATTRIBUTE) + "." + nodeName + "}";
                        element.removeAttribute(VALUE_ATTRIBUTE);
                    }
                    element.removeAttribute(IS_EMPTY_ATTRIBUTE);

                    Element newElement = document.createElement(IF_PLACEHOLDER);
                    // remove ${} from the value and append has_content check
                    newElement.setAttribute(ATTRIBUTE_PLACEHOLDER, value.substring(2, value.length() - 1) +
                            QUESTION_MARK_PLACEHOLDER + "has_content");

                    Node parentNode = element.getParentNode();
                    parentNode.replaceChild(newElement, element);
                    newElement.appendChild(element);

                    iterateChildNodes(newElement, document);
                } else {
                    iterateChildNodes(element, document);
                }
            } else {
                iterateChildNodes(childNode, document);
            }
        }
    }
    private static Document createSOAPRequestXMLForOperation(ArrayList<String> parameterJsonPathMapping, Map<String,
            String> queryPathParamMapping, String namespace, String operationId, OpenAPI openAPI,
            Map<String,String> jsonPathAndSchemaMap) throws SOAPToRESTException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        StringWriter stringWriter = new StringWriter();
        boolean isNamespaceQualified = false;
        boolean isRootComplexType = false;
        Document doc;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
            Element rootElement = null;
            if (SOAPToRESTConstants.SOAP_RPC_MESSAGE_TYPE.equalsIgnoreCase(soapMessageType) ||
                    SOAPToRESTConstants.SOAP_RPC_MESSAGE_TYPE.equalsIgnoreCase(soapStyle) ||
                    parameterJsonPathMapping.isEmpty()) {
                rootElement = doc.createElementNS(namespace,
                        SOAPToRESTConstants.NAMESPACE_PREFIX +
                                SOAPToRESTConstants.NAMESPACE_SEPARATOR + operationId);
                doc.appendChild(rootElement);
            }
            for (String parameter : parameterJsonPathMapping) {
                String[] parameterTreeNodes = parameter.split("\\.");

                Element prevElement = rootElement;
                int elemPos = 0;
                int length = parameterTreeNodes.length;
                if (length > 0 && !isRootComplexType) {
                    isRootComplexType = true;
                }
                String currentJSONPath = "payload";
                String notEscapedJSONPath = "payload";
                for (int i = 0; i < length; i++) {
                    String parameterTreeNode = parameterTreeNodes[i];
                    boolean isArray = false;
                    currentJSONPath = currentJSONPath.isEmpty() ? escapeFreeMarkerTemplate(parameterTreeNode) :
                            currentJSONPath + "." + escapeFreeMarkerTemplate(parameterTreeNode);
                    notEscapedJSONPath = notEscapedJSONPath.isEmpty() ? parameterTreeNode :
                            notEscapedJSONPath + "." + parameterTreeNode;
                    if (parameterTreeNode.endsWith("[0]")) {
                        isArray = true;
                        parameterTreeNode = parameterTreeNode.replace("[0]", "");
                    }
                    Schema<?> schema = openAPI.getComponents().getSchemas().get(parameterTreeNode);
                    // Since we add the elements defined in the root XSD with the 'rootElement_' prefix, we need to
                    // check for the schema with the prefix if the schema is not found.
                    if (schema == null) {
                        schema = openAPI.getComponents().getSchemas()
                                .get(SOAPToRESTConstants.ROOT_ELEMENT_PREFIX + parameterTreeNode);
                    }
                    if (schema != null) {
                        Map<String, Object> vendorExtensions = schema.getExtensions();
                        if (vendorExtensions != null && vendorExtensions.get(SOAPToRESTConstants.X_NAMESPACE_QUALIFIED) != null
                                && Boolean.parseBoolean(vendorExtensions.get(SOAPToRESTConstants.X_NAMESPACE_QUALIFIED).
                                toString())) {
                            isNamespaceQualified = true;
                        }
                    }
                    Schema<?> parentSchema = null;
                    boolean needIsEmptyCheck = false;
                    // Check parent schema for required fields and wrap with isEmpty check if required
                    if (prevElement != null) {
                        String mapKey = parameterTreeNode;
                        // payload. is 8 characters long
                        if (notEscapedJSONPath.length() > 8 + parameterTreeNode.length()) {
                            mapKey = notEscapedJSONPath.substring(8, notEscapedJSONPath.length()
                                    - parameterTreeNode.length() - 1);
                        }
                        parentSchema = openAPI.getComponents().getSchemas().get(jsonPathAndSchemaMap.get(mapKey));
                        if (parentSchema == null) {
                            // check for the schema inside parent object's schema
                            String parentKey = mapKey.substring(0, mapKey.lastIndexOf('.'));
                            if (!StringUtils.isEmpty(parentKey)) {
                                Schema<?> enclosingSchema = openAPI.getComponents().getSchemas()
                                        .get(jsonPathAndSchemaMap.get(parentKey));
                                if (enclosingSchema != null && enclosingSchema.getProperties() != null &&
                                        enclosingSchema.getProperties().containsKey(prevElement.getLocalName())) {
                                    parentSchema = enclosingSchema.getProperties().get(prevElement.getLocalName());
                                }
                            }
                        }
                        if (parentSchema != null && (parentSchema.getRequired() == null ||
                                !parentSchema.getRequired().contains(parameterTreeNode))) {
                            needIsEmptyCheck = true;
                        }
                    }

                    String payloadPrefix = "${";
                    if (StringUtils.isNotBlank(parameterTreeNode)) {
                        if (SOAPToRESTConstants.ATTR_CONTENT_KEYWORD.equalsIgnoreCase(parameterTreeNode)) {
                            String attName = parameterTreeNodes[++i];
                            prevElement.setAttribute(attName, payloadPrefix + currentJSONPath + "}");
                            break;
                        }
                        if (SOAPToRESTConstants.BASE_CONTENT_KEYWORD.equalsIgnoreCase(parameterTreeNode)) {
                            prevElement.setTextContent(payloadPrefix + currentJSONPath + "}");
                            break;
                        }
                        Element element;

                        if (isNamespaceQualified) {
                            element = doc.createElementNS(namespace,
                                    SOAPToRESTConstants.NAMESPACE_PREFIX +
                                            SOAPToRESTConstants.NAMESPACE_SEPARATOR + parameterTreeNode);
                        } else if (isRootComplexType) {
                            element = doc.createElementNS(namespace,
                                    SOAPToRESTConstants.NAMESPACE_PREFIX +
                                            SOAPToRESTConstants.NAMESPACE_SEPARATOR + parameterTreeNode);
                            isRootComplexType = false;
                        } else {
                            element = doc.createElement( parameterTreeNode);
                        }
                        if (isArray) {
                            element.setAttribute(SOAPToRESTConstants.ARRAY_PLACEHOLDER,
                                    currentJSONPath.replace("[0]", ""));
                            currentJSONPath = escapeFreeMarkerTemplate(parameterTreeNode);
                        }

                        String xPathOfNode = StringUtils.EMPTY;
                        if (doc.getElementsByTagName(element.getTagName()).getLength() > 0) {
                            xPathOfNode = getXpath(doc.getElementsByTagName(element.getTagName()).item(0));
                            xPathOfNode = xPathOfNode.replaceAll("/+", ".");
                            if (xPathOfNode.startsWith(".")) {
                                xPathOfNode = xPathOfNode.substring(1);
                            }
                            if (xPathOfNode.contains(operationId + ".")) {
                                xPathOfNode = xPathOfNode.replace(operationId + ".", "");
                            }
                        }

                        if (doc.getElementsByTagName(element.getTagName()).getLength() > 0 &&
                                parameter.contains(xPathOfNode) &&
                                rootElement != doc.getElementsByTagName(element.getTagName()).item(0)) {
                            prevElement = (Element) doc.getElementsByTagName(element.getTagName()).item(0);
                        } else {
                            if (elemPos == length - 1) {
                                element.setTextContent(payloadPrefix + currentJSONPath + "}");
                            }
                            if (needIsEmptyCheck) {
                                String path = "";
                                for (int j = 0; j < i; j++) {
                                    path = path.concat(escapeFreeMarkerTemplate(parameterTreeNodes[j])).concat(".");
                                }
                                element.setAttribute(IS_EMPTY_ATTRIBUTE, "true");
                                if (path.length() == 0) {
                                    element.setAttribute(VALUE_ATTRIBUTE, "");
                                } else {
                                    element.setAttribute(VALUE_ATTRIBUTE, path.substring(0, path.length() - 1));
                                }
                            }
                            if (prevElement != null) {
                                prevElement.appendChild(element);
                            } else {
                                if (element.getLocalName() != null && !StringUtils.contains(element.getLocalName(), "null")) {
                                    doc.appendChild(element);
                                }
                            }
                            prevElement = element;
                        }
                        elemPos++;
                    }
                }
            }
            if (parameterJsonPathMapping.isEmpty()) {
                for (String queryParam : queryPathParamMapping.keySet()) {
                    Element element = doc.createElementNS(namespace,
                            SOAPToRESTConstants.NAMESPACE_PREFIX +
                                    SOAPToRESTConstants.NAMESPACE_SEPARATOR + queryParam);
                    element.setTextContent("${uri.var." + queryParam + "}");
                    if (rootElement != null) {
                        rootElement.appendChild(element);
                    } else {
                        doc.appendChild(element);
                    }
                }
            } else if (queryPathParamMapping.size() > 0) {
                log.warn("Query parameters along with the body parameter is not allowed");
            }
        } catch (ParserConfigurationException e) {
            throw new SOAPToRESTException("Error occurred when building in sequence xml", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("parameter mapping for used in payload factory for soap operation:" + operationId + " is " +
                    stringWriter);
        }

        return doc;
    }

    private static String getXpath(Node node) {

        if (node != null) {
            Node parent = node.getParentNode();
            if (parent == null && node.getLocalName() != null) {
                return node.getLocalName();
            } else if (node.getLocalName() != null) {
                return getXpath(parent) + SOAPToRESTConstants.PATH_SEPARATOR + node.getLocalName();
            } else {
                return getXpath(parent);
            }
        }
        return SOAPToRESTConstants.EMPTY_STRING;
    }

    /**
     * Escape the FreeMarker template. Since FreeMarker 2.3.22 the variable name can also contain minus (-), dot (.)
     * , and colon (:) at any position, but these must be escaped with a preceding backslash (\)
     *
     * @param template free marker template
     * @return escaped template
     */
    private static String escapeFreeMarkerTemplate(String template) {

        return template.replace("-", "\\-").replace(".", "\\.")
                .replace(":", "\\:");
    }
}
