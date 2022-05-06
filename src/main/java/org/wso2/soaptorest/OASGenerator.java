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

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.soaptorest.models.*;
import org.wso2.soaptorest.utils.SOAPToRESTConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OASGenerator {

    static Logger log = LoggerFactory.getLogger(OASGenerator.class);

    /**
     * Generate the swagger from the WSDL info
     *
     * @param wsdlInfo WSDLInfo object which has parsed WSDL data
     * @return Generated the swagger from the WSDL info
     */
    public static OpenAPI generateOpenAPIFromWSDL(WSDLInfo wsdlInfo, List<XSModel> xsModel, String APITitle,
                                                  String APIVersion) {

        Set<WSDLSOAPOperation> operations;
        operations = wsdlInfo.getSoapBindingOperations();
        populateSoapOperationParameters(operations);
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        for (WSDLSOAPOperation operation : operations) {
            PathItem pathItem = new PathItem();
            Operation swaggerOperation = new Operation();
            ObjectSchema inputModelSchema = new ObjectSchema();
            if (operation.getInputParameterModel() != null) {
                for (WSDLParameter inputQName : operation.getInputParameterModel()) {
                    if (inputQName.getMessageType() == WSDLParameter.MessageType.TYPE) {
                        Schema<?> inputProp = getDataTypesSchema(inputQName.getQName());
                        inputModelSchema.addProperties("parameter", inputProp);
                    } else {
                        Schema<?> inputRefProp = new Schema<>();
                        inputRefProp.setName(inputQName.getQName().getLocalPart().replaceAll("\\s+", ""));
                        inputRefProp.set$ref(SOAPToRESTConstants.OAS_DEFINITIONS_ROOT_ELEMENT_PATH + inputQName.
                                getQName().getLocalPart());
                        inputModelSchema.addProperties(inputQName.getQName().getLocalPart(), inputRefProp);
                    }
                }
            } else {
                inputModelSchema.set$ref(SOAPToRESTConstants.OAS_DEFINITIONS_PREFIX + operation.getName());
            }

            RequestBody requestBody = new RequestBody();
            requestBody.setRequired(true);
            Content content = new Content();
            MediaType mediaType = new MediaType();
            mediaType.setSchema(inputModelSchema);
            content.addMediaType(SOAPToRESTConstants.OAS_ALL_MEDIA_TYPE, mediaType);
            requestBody.setContent(content);
            swaggerOperation.setRequestBody(requestBody);

            //adding response
            ApiResponses responses = new ApiResponses();
            ApiResponse response = new ApiResponse();
            ObjectSchema outputModelSchema = new ObjectSchema();
            if (operation.getOutputParameterModel() != null) {
                for (WSDLParameter outputParamQName : operation.getOutputParameterModel()) {
                    if (outputParamQName.getMessageType() == WSDLParameter.MessageType.TYPE) {
                        Schema<?> outputProp = getDataTypesSchema(outputParamQName.getQName());
                        outputModelSchema.addProperties("parameter", outputProp);
                    } else {
                        Schema<?> outputRefProp = new Schema<>();
                        outputRefProp.setName(SOAPToRESTConstants.OAS_DEFINITIONS_ROOT_ELEMENT_PATH + outputParamQName.getQName().getLocalPart().replaceAll("\\s+", ""));
                        outputRefProp.set$ref(
                                outputParamQName.getQName().getLocalPart());
                        outputModelSchema.addProperties(outputParamQName.getQName().getLocalPart(), outputRefProp);
                    }
                }

            } else {
                outputModelSchema.set$ref(SOAPToRESTConstants.OAS_DEFINITIONS_PREFIX + operation.getName());
            }
            Content outputContent = new Content();
            MediaType outputMediaType = new MediaType();
            outputMediaType.setSchema(outputModelSchema);
            outputContent.addMediaType(SOAPToRESTConstants.OAS_ALL_MEDIA_TYPE, outputMediaType);
            response.setContent(outputContent);
            response.setDescription(SOAPToRESTConstants.DEFAULT_DESCRIPTION);
            responses.setDefault(response);
            swaggerOperation.setResponses(responses);
            swaggerOperation.setOperationId(operation.getSoapBindingOpName());
            //setting vendor extensions
            Map<String, String> extensions = new HashMap<>();
            extensions.put(SOAPToRESTConstants.SOAP_ACTION, operation.getSoapAction());
            extensions.put(SOAPToRESTConstants.SOAP_OPERATION, operation.getSoapBindingOpName());
            extensions.put(SOAPToRESTConstants.NAMESPACE, operation.getTargetNamespace());
            if (wsdlInfo.isHasSoap12BindingOperations()) {
                extensions.put(SOAPToRESTConstants.SOAP_VERSION, SOAPToRESTConstants.SOAP_VERSION_12);
            } else if (wsdlInfo.hasSoapBindingOperations()) {
                extensions.put(SOAPToRESTConstants.SOAP_VERSION, SOAPToRESTConstants.SOAP_VERSION_11);
            }
            extensions.put(SOAPToRESTConstants.SOAP_STYLE, operation.getStyle());
            extensions.put(SOAPToRESTConstants.SOAP_MESSAGE_TYPE, operation.getMessageType());
            Map<String, Object> extensionMap = new HashMap<>();
            extensionMap.put(SOAPToRESTConstants.WSO2_SOAP, extensions);
            swaggerOperation.setExtensions(extensionMap);

            if (operation.getHttpVerb().equals(SOAPToRESTConstants.HTTP_METHOD_GET)) {
                pathItem.setGet(swaggerOperation);
            } else {
                pathItem.setPost(swaggerOperation);
            }
            paths.addPathItem("/" + operation.getName(), pathItem);
        }

        openAPI.paths(paths);
        Info info = new Info();
        info.setTitle(APITitle != null ? APITitle : SOAPToRESTConstants.EMPTY_STRING);
        info.setVersion(APIVersion != null ? APIVersion : SOAPToRESTConstants.EMPTY_STRING);
        openAPI.info(info);
        openAPI.setComponents(generateOASSchemas(xsModel));

        return openAPI;

    }

    /**
     * gets parameters from the soap operation and populates them in {@link WSDLSOAPOperation}
     *
     * @param soapOperations soap binding operations
     */
    private static void populateSoapOperationParameters(Set<WSDLSOAPOperation> soapOperations) {

        if (soapOperations != null) {
            for (WSDLSOAPOperation operation : soapOperations) {
                String resourcePath;
                String operationName = operation.getName();
                operation.setSoapBindingOpName(operationName);
                operation.setHttpVerb(SOAPToRESTConstants.HTTP_METHOD_POST);
                resourcePath = operationName;
                resourcePath = resourcePath.substring(0, 1).toLowerCase() + resourcePath.substring(1);
                operation.setName(resourcePath.replaceAll("\\s+", ""));
                if (log.isDebugEnabled()) {
                    log.debug("REST resource path for SOAP operation: " + operationName + " is: " + resourcePath);
                }
            }
        } else {
            log.info("No SOAP operations found in the WSDL");
        }
    }

    private static Components generateOASSchemas(List<XSModel> xsModelList) {

        Components components = new Components();

        for (XSModel xsModel : xsModelList) {
            List<XSDataType> xsDataTypeList = xsModel.getXsDataTypes();
            for (XSDataType xsDataType : xsDataTypeList) {
                Schema<?> schema = getSchemaForXSDataType(xsDataType);
                components.addSchemas(schema.getName(), schema);
            }
            List<XSGroup> xsDataGroupList = xsModel.getGroups();
            for (XSGroup xsGroup : xsDataGroupList) {
                Schema<?> schema = new ObjectSchema();
                schema.setName(xsGroup.getName().getLocalPart());
                processXSGroup(xsGroup, schema);
                components.addSchemas(schema.getName(), schema);
            }

            if (xsModel.getElements().size() > 0) {
                //Process the elements defined in the root XSD and add 'rootElement_' prefix to identify uniquely
                for (XSElement xsElement : xsModel.getElements()) {
                    Schema<?> schema = getSchemaForXSElement(xsElement);
                    schema.setName("rootElement_" + xsElement.getName().getLocalPart().replaceAll("\\s+", ""));
                    schema.setType("object");
                    components.addSchemas(schema.getName(), schema);

                }

            }
        }
        return components;
    }

    private static Schema<?> getSchemaForXSDataType(XSDataType xsDataType) {
        Schema<?> schema = new ObjectSchema();
        if (xsDataType.getName() != null) {
            schema.setName(xsDataType.getName().getLocalPart().replaceAll("\\s+", ""));
        } else {
            schema.setName("Default_Object");
        }
        if (xsDataType.getExtensionBase() != null) {
            schema.addProperties(SOAPToRESTConstants.EXTENSION_NAME, getDataTypesSchema(xsDataType.getExtensionBase()));
            Map<String, Object> extensionStringObjectMap = new HashMap<>();
            XML xml = new XML();
            if (xsDataType.getName() != null && StringUtils.isNotBlank(xsDataType.getName().getNamespaceURI())) {
                xml.setNamespace(xsDataType.getName().getNamespaceURI());
                xml.setPrefix(xsDataType.getName().getPrefix());
            }
            extensionStringObjectMap.put("x-namespace-qualified", false);
            schema.setXml(xml);
            schema.setExtensions(extensionStringObjectMap);
        }
        if (xsDataType.getSequence() != null) {
            processXSSequence(xsDataType.getSequence(), schema);
            Map<String, Object> extensionStringObjectMap = new HashMap<>();
            XML xml = new XML();
            if (xsDataType.getName() != null && StringUtils.isNotBlank(xsDataType.getName().getNamespaceURI())) {
                xml.setNamespace(xsDataType.getName().getNamespaceURI());
                xml.setPrefix(xsDataType.getName().getPrefix());
            }
            extensionStringObjectMap.put("x-namespace-qualified", false);
            schema.setXml(xml);
            schema.setExtensions(extensionStringObjectMap);
        }
        if (xsDataType.getChoice() != null) {
            processXSChoice(xsDataType.getChoice(), schema);
        }
        if (xsDataType.getGroup() != null) {
            processXSGroup(xsDataType.getGroup(), schema);
        }
        return schema;
    }

    private static void processXSSequence(XSSequence xsSequence, Schema<?> parentSchema) {

        if (xsSequence.getElementList() != null) {
            List<XSElement> xsElementList = xsSequence.getElementList();
            for (XSElement xsElement : xsElementList) {
                Schema<?> innerSchema = getSchemaForXSElement(xsElement);
                parentSchema.addProperties(innerSchema.getName(), innerSchema);
            }
        }
        if (xsSequence.getSequenceList() != null) {
            for (XSSequence innerXSequence : xsSequence.getSequenceList()) {
                processXSSequence(innerXSequence, parentSchema);
            }
        }
        if (xsSequence.getChoiceList() != null) {
            List<XSChoice> xsChoiceList = xsSequence.getChoiceList();
            for (XSChoice xsChoice : xsChoiceList) {
                processXSChoice(xsChoice, parentSchema);
            }
        }
    }

    private static void processXSChoice(XSChoice xsChoice, Schema<?> parentSchema) {

        if (xsChoice.getSequenceList() != null) {
            for (XSSequence xsSequence : xsChoice.getSequenceList()) {
                processXSSequence(xsSequence, parentSchema);
            }
        }
        if (xsChoice.getChoiceList() != null) {
            for (XSChoice innerXsChoice : xsChoice.getChoiceList()) {
                processXSChoice(innerXsChoice, parentSchema);
            }
        }
        if (xsChoice.getGroupsList() != null) {
            for (XSGroup xsGroup : xsChoice.getGroupsList()) {
                processXSGroup(xsGroup, parentSchema);
            }
        }
        if (xsChoice.getElementList() != null) {
            for (XSElement xsElement : xsChoice.getElementList()) {
                Schema<?> innerSchema = getSchemaForXSElement(xsElement);
                parentSchema.addProperties(innerSchema.getName(), innerSchema);
            }
        }
    }

    private static void processXSGroup(XSGroup xsGroup, Schema<?> parentSchema) {

        if (xsGroup.getChoiceList() != null) {
            for (XSChoice xsChoice : xsGroup.getChoiceList()) {
                processXSChoice(xsChoice, parentSchema);
            }
        }
        if (xsGroup.getSequenceList() != null) {
            for (XSSequence xsSequence : xsGroup.getSequenceList()) {
                processXSSequence(xsSequence, parentSchema);
            }
        }
        if (xsGroup.getRefKey() != null) {
            parentSchema.set$ref(SOAPToRESTConstants.OAS_DEFINITIONS_PREFIX + xsGroup.getRefKey().getLocalPart());
        }
    }

    private static Schema<?> getSchemaForXSElement(XSElement xsElement) {

        Schema<?> schema = null;
        if (xsElement.getType() != null) {
            if (xsElement.isArray()) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.setItems(getDataTypesSchema(xsElement.getType()));
                schema = arraySchema;
            } else {
                schema = getDataTypesSchema(xsElement.getType());
            }
            if (xsElement.getName() != null) {
                schema.setName(xsElement.getName().getLocalPart().replaceAll("\\s+", ""));
            }
        } else if (xsElement.getRefKey() != null) {
            schema = new Schema<>();
            schema.setName(xsElement.getRefKey().getLocalPart().replaceAll("\\s+", ""));
            schema.$ref(SOAPToRESTConstants.OAS_DEFINITIONS_PREFIX + xsElement.getRefKey().getLocalPart());
        } else if (xsElement.getInlineComplexType() != null) {
            schema = getSchemaForXSDataType(xsElement.getInlineComplexType());
        }
        return schema;
    }

    public static Schema<?> getDataTypesSchema(QName type) {

        Schema<?> outputSchema;
        String dataType = type.getLocalPart();
        switch (dataType) {
            case "string":
            case "anyType":
            case "duration":
            case "time":
            case "gYearMonth":
            case "gMonthDay":
            case "gYear":
            case "gDay":
            case "gMonth":
            case "anyURI":
            case "QName":
            case "normalizedString":
            case "token":
                outputSchema = new StringSchema();
                outputSchema.setType("string");
                break;
            case "dateTime":
                outputSchema = new DateTimeSchema();
                outputSchema.setType("string");
                outputSchema.setFormat("date-time");
                break;
            case "date":
                outputSchema = new DateSchema();
                outputSchema.setType("string");
                outputSchema.setFormat("date");
                break;
            case "hexBinary":
                outputSchema = new StringSchema();
                outputSchema.setType("string");
                outputSchema.setFormat("binary");
                break;
            case "base64Binary":
            case "unsignedByte":
                outputSchema = new StringSchema();
                outputSchema.setType("string");
                outputSchema.setFormat("byte");
                break;
            case "integer":
            case "nonPositiveInteger":
            case "negativeInteger":
            case "positiveInteger":
            case "nonNegativeInteger":
            case "unsignedInt":
            case "int":
            case "short":
            case "unsignedShort":
                outputSchema = new IntegerSchema();
                outputSchema.setType("integer");
                outputSchema.setFormat("int32");
                break;
            case "long":
            case "unsignedLong":
                outputSchema = new IntegerSchema();
                outputSchema.setType("integer");
                outputSchema.setFormat("int64");
                break;
            case "boolean":
                outputSchema = new BooleanSchema();
                outputSchema.setType("boolean");
                break;
            case "decimal":
            case "float":
                outputSchema = new NumberSchema();
                outputSchema.setType("number");
                outputSchema.setFormat("float");
                break;
            case "double":
                outputSchema = new NumberSchema();
                outputSchema.setType("number");
                outputSchema.setFormat("double");
                break;
            default:
                outputSchema = new Schema<>();
                outputSchema.$ref(SOAPToRESTConstants.OAS_DEFINITIONS_PREFIX + type.getLocalPart());
        }
        return outputSchema;
    }

}
