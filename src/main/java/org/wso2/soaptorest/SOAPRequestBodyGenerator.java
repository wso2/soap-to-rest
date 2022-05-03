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
                        String jsonExample = Json.pretty(example);
                        if (jsonExample != null && !"null".equals(jsonExample)) {
                            parameterJsonPathMapping = ListJSONPaths.getJsonPaths(jsonExample);
                        }
                    } catch (Exception e) {
                        throw new SOAPToRESTException("Cannot generate JSON body from the OpenAPI", e);
                    }

                }

                Document soapRequestBody = createSOAPRequestXMLForOperation(parameterJsonPathMapping, queryParameters,
                        namespace, operationId, openAPI);

                requestBodies.put(operationId, new SOAPRequestElement(soapRequestBody, soapAction, namespace,
                        soapNamespace));
            }
        }
        return new SOAPtoRESTConversionData(openAPI, requestBodies, soapService, soapPort);
    }

    private static Document createSOAPRequestXMLForOperation(ArrayList<String> parameterJsonPathMapping, Map<String,
            String> queryPathParamMapping, String namespace, String operationId, OpenAPI openAPI) throws
            SOAPToRESTException {

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
                    parameterJsonPathMapping.size() == 0) {
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
                for (int i = 0; i < length; i++) {
                    String parameterTreeNode = parameterTreeNodes[i];
                    boolean isArray = false;
                    currentJSONPath = currentJSONPath.isEmpty() ? parameterTreeNode :
                            currentJSONPath + "." + parameterTreeNode;
                    if (parameterTreeNode.endsWith("[0]")) {
                        isArray = true;
                        parameterTreeNode = parameterTreeNode.replace("[0]", "");
                    }
                    Schema<?> schema = openAPI.getComponents().getSchemas().get(parameterTreeNode);
                    if (schema != null) {
                        Map<String, Object> vendorExtensions = schema.getExtensions();
                        if (vendorExtensions.get(SOAPToRESTConstants.X_NAMESPACE_QUALIFIED) != null
                                && Boolean.parseBoolean(vendorExtensions.get(SOAPToRESTConstants.X_NAMESPACE_QUALIFIED).
                                toString())) {
                            isNamespaceQualified = true;
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
                            element = doc.createElementNS(null, parameterTreeNode);
                            element.setAttribute(SOAPToRESTConstants.XMLNS,
                                    SOAPToRESTConstants.X_WSO2_UNIQUE_NAMESPACE);
                        }
                        if (isArray) {
                            element.setAttribute(SOAPToRESTConstants.ARRAY_PLACEHOLDER,
                                    currentJSONPath.replace("[0]", ""));
                            currentJSONPath = parameterTreeNode;
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
                            if (prevElement != null) {
                                prevElement.appendChild(element);
                            } else {
                                doc.appendChild(element);
                            }
                            prevElement = element;
                        }
                        elemPos++;
                    }
                }
            }
            if (parameterJsonPathMapping.size() == 0) {
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

}
