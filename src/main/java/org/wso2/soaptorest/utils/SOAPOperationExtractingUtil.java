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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.soaptorest.exceptions.SOAPToRESTException;
import org.wso2.soaptorest.models.WSDLInfo;
import org.wso2.soaptorest.models.WSDLParameter;
import org.wso2.soaptorest.models.WSDLSOAPOperation;

import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import java.util.*;

/**
 * Utility class to extract the SOAP Operations from the given WSDL Definition file
 */
public class SOAPOperationExtractingUtil {

    static Logger log = LoggerFactory.getLogger(SOAPOperationExtractingUtil.class);
    String targetNamespace;

    /**
     * This method will return the extracted SOAP Operation details of the given WSDL definition
     *
     * @param wsdlDefinition input wsdl definition to extract the SOAP Operations
     * @return returns the {@link WSDLInfo} object carrying the info extracted from the WSDL file
     * @throws SOAPToRESTException exception will throw if the SOAP Operation detail extraction is not success
     */
    public WSDLInfo getWsdlInfo(Definition wsdlDefinition) throws SOAPToRESTException {

        WSDLInfo wsdlInfo = new WSDLInfo();
        if (wsdlDefinition != null) {
            Set<WSDLSOAPOperation> soapOperations = getSoapBindingOperations(wsdlDefinition);
            //the current implementation only supports WSDL 1.1 version
            wsdlInfo.setVersion(SOAPToRESTConstants.WSDL_VERSION_11);

            if (!soapOperations.isEmpty()) {
                wsdlInfo.setSoapBindingOperations(soapOperations);
            }
            wsdlInfo.setHasSoapBindingOperations(hasSoapBindingOperations(wsdlDefinition));
            wsdlInfo.setHasSoap12BindingOperations(hasSoap12BindingOperations(wsdlDefinition));

            //only support for single service and a port per WSDL file, hence getting the first one
            Service service = (Service) wsdlDefinition.getServices().values().iterator().next();
            Port port = (Port) service.getPorts().values().iterator().next();
            wsdlInfo.setSoapService(service.getQName().getLocalPart());
            wsdlInfo.setSoapPort(port.getName());

        } else {
            throw new SOAPToRESTException("WSDL Definition is not initialized.");
        }
        return wsdlInfo;
    }

    /**
     * Retrieves all the operations defined in the provided WSDL definition.
     *
     * @param definition WSDL Definition
     * @return a set of {@link WSDLSOAPOperation} defined in the provided WSDL definition
     */
    private Set<WSDLSOAPOperation> getSoapBindingOperations(Definition definition) throws SOAPToRESTException {

        targetNamespace = definition.getTargetNamespace();
        Set<WSDLSOAPOperation> allOperations = new HashSet<>();
        for (Object bindingObj : definition.getAllBindings().values()) {
            if (bindingObj instanceof Binding) {
                Binding binding = (Binding) bindingObj;
                Set<WSDLSOAPOperation> operations = getSOAPBindingOperations(binding);
                allOperations.addAll(operations);
            }
        }
        return allOperations;
    }

    /**
     * Retrieves all the operations defined in the provided Binding.
     *
     * @param binding WSDL binding
     * @return a set of {@link WSDLSOAPOperation} defined in the provided Binding
     */
    private Set<WSDLSOAPOperation> getSOAPBindingOperations(Binding binding) throws SOAPToRESTException {

        Set<WSDLSOAPOperation> allBindingOperations = new HashSet<>();
        if (binding.getExtensibilityElements() != null && binding.getExtensibilityElements().size() > 0) {
            List<?> extensibilityElements = binding.getExtensibilityElements();
            for (Object extensibilityElement : extensibilityElements) {
                if (extensibilityElement instanceof SOAPBinding || extensibilityElement instanceof SOAP12Binding) {
                    for (Object opObj : binding.getBindingOperations()) {
                        BindingOperation bindingOperation = (BindingOperation) opObj;
                        WSDLSOAPOperation wsdlSoapOperation = getSOAPOperation(bindingOperation);
                        if (wsdlSoapOperation != null) {
                            allBindingOperations.add(wsdlSoapOperation);
                        } else {
                            log.warn("Unable to get soap operation details: " + bindingOperation.getName());
                        }
                    }
                }
            }
        } else {
            throw new SOAPToRESTException("Cannot further process to get soap binding operations");
        }
        return allBindingOperations;
    }

    /**
     * Retrieves WSDL operation given the soap binding operation
     *
     * @param bindingOperation {@link BindingOperation} object
     * @return a set of {@link WSDLSOAPOperation} defined in the provided Binding
     */
    private WSDLSOAPOperation getSOAPOperation(BindingOperation bindingOperation) {

        WSDLSOAPOperation wsdlOperation = null;
        if (bindingOperation.getExtensibilityElements().isEmpty() && bindingOperation.getOperation() != null) {
            Operation soapOperation = bindingOperation.getOperation();
            wsdlOperation = new WSDLSOAPOperation();
            wsdlOperation.setName(bindingOperation.getName());
            wsdlOperation.setTargetNamespace(getTargetNamespace(bindingOperation));
            wsdlOperation.setStyle(String.valueOf(soapOperation.getStyle()));
            wsdlOperation.setInputParameterModel(getSoapInputParameterModel(bindingOperation));
            wsdlOperation.setOutputParameterModel(getSoapOutputParameterModel(bindingOperation));
            wsdlOperation.setMessageType(getSoapMessageType(bindingOperation));

        } else {
            for (Object boExtElement : bindingOperation.getExtensibilityElements()) {
                if (boExtElement instanceof SOAPOperation) {
                    SOAPOperation soapOperation = (SOAPOperation) boExtElement;
                    wsdlOperation = new WSDLSOAPOperation();
                    wsdlOperation.setName(bindingOperation.getName());
                    wsdlOperation.setSoapAction(soapOperation.getSoapActionURI());
                    wsdlOperation.setTargetNamespace(getTargetNamespace(bindingOperation));
                    wsdlOperation.setStyle(soapOperation.getStyle());
                    wsdlOperation.setInputParameterModel(getSoapInputParameterModel(bindingOperation));
                    wsdlOperation.setOutputParameterModel(getSoapOutputParameterModel(bindingOperation));
                    wsdlOperation.setMessageType(getSoapMessageType(bindingOperation));
                } else if (boExtElement instanceof SOAP12Operation) {
                    SOAP12Operation soapOperation = (SOAP12Operation) boExtElement;
                    wsdlOperation = new WSDLSOAPOperation();
                    wsdlOperation.setName(bindingOperation.getName());
                    wsdlOperation.setSoapAction(soapOperation.getSoapActionURI());
                    wsdlOperation.setTargetNamespace(getTargetNamespace(bindingOperation));
                    wsdlOperation.setStyle(soapOperation.getStyle());
                    wsdlOperation.setInputParameterModel(getSoapInputParameterModel(bindingOperation));
                    wsdlOperation.setOutputParameterModel(getSoapOutputParameterModel(bindingOperation));
                    wsdlOperation.setMessageType(getSoapMessageType(bindingOperation));
                }
            }
        }
        return wsdlOperation;
    }

    /**
     * Gets the target namespace given the soap binding operation
     *
     * @param bindingOperation soap operation
     * @return target name space
     */
    private String getTargetNamespace(BindingOperation bindingOperation) {

        Operation operation = bindingOperation.getOperation();
        if (operation != null) {
            Input input = operation.getInput();

            if (input != null) {
                Message message = input.getMessage();
                if (message != null) {
                    Map<?, ?> partMap = message.getParts();

                    for (Map.Entry<?, ?> obj : partMap.entrySet()) {
                        Part part = (Part) obj.getValue();
                        if (part != null) {
                            if (part.getElementName() != null) {
                                return part.getElementName().getNamespaceURI();
                            }
                        }
                    }
                }
            }
        }
        return targetNamespace;
    }

    /**
     * Gets swagger input parameter model for a given soap operation
     *
     * @param bindingOperation soap operation
     * @return list of swagger models for the parameters
     */
    private List<WSDLParameter> getSoapInputParameterModel(BindingOperation bindingOperation) {

        List<WSDLParameter> inputParameterModelList = new ArrayList<>();
        Operation operation = bindingOperation.getOperation();
        if (operation != null) {
            Input input = operation.getInput();

            if (input != null) {
                Message message = input.getMessage();
                if (message != null) {
                    Map<?, ?> map = message.getParts();

                    for (Map.Entry<?, ?> obj : map.entrySet()) {
                        Part part = (Part) obj.getValue();
                        if (part != null) {
                            if (part.getElementName() != null) {
                                inputParameterModelList.add(new WSDLParameter(part.getElementName(),
                                        WSDLParameter.MessageType.ELEMENT));
                            } else {
                                if (part.getTypeName() != null) {
                                    inputParameterModelList.add(new WSDLParameter(part.getTypeName(),
                                            WSDLParameter.MessageType.TYPE));
                                }
                            }
                        }
                    }
                }
            }
        }
        return inputParameterModelList;
    }

    /**
     * Gets swagger output parameter model for a given soap operation
     *
     * @param bindingOperation soap operation
     * @return list of swagger models for the parameters
     */
    private List<WSDLParameter> getSoapOutputParameterModel(BindingOperation bindingOperation) {

        List<WSDLParameter> outputParameterModelList = new ArrayList<>();
        Operation operation = bindingOperation.getOperation();
        if (operation != null) {
            Output output = operation.getOutput();
            if (output != null) {
                Message message = output.getMessage();
                if (message != null) {
                    Map<?, ?> map = message.getParts();

                    for (Map.Entry<?, ?> obj : map.entrySet()) {
                        Part part = (Part) obj.getValue();
                        if (part != null) {
                            if (part.getElementName() != null) {
                                outputParameterModelList.add(new WSDLParameter(part.getElementName(),
                                        WSDLParameter.MessageType.ELEMENT));
                            } else {
                                if (part.getTypeName() != null) {
                                    outputParameterModelList.add(new WSDLParameter(part.getTypeName(),
                                            WSDLParameter.MessageType.TYPE));
                                }
                            }
                        }
                    }
                }
            }
        }
        return outputParameterModelList;
    }

    /**
     * Gets message type for a given soap operation
     *
     * @param bindingOperation soap operation
     * @return String for message type
     */
    private String getSoapMessageType(BindingOperation bindingOperation) {

        Operation operation = bindingOperation.getOperation();
        String messageType = "";
        boolean hasRPCMessages = false;
        if (operation != null) {
            Input input = operation.getInput();

            if (input != null) {
                Message message = input.getMessage();
                if (message != null) {
                    Map<?, ?> map = message.getParts();

                    for (Map.Entry<?, ?> obj : map.entrySet()) {
                        Part part = (Part) obj.getValue();
                        if (part != null) {
                            if (part.getElementName() != null) {
                                messageType = "document";
                            } else if (part.getTypeName() != null) {
                                messageType = "rpc";
                                hasRPCMessages = true;
                            }
                        }
                    }
                }
            }
        }
        if (hasRPCMessages) {
            return "rpc";
        } else {
            return messageType;
        }
    }

    /**
     * Returns if the provided WSDL definition contains SOAP binding operations
     *
     * @return whether the provided WSDL definition contains SOAP binding operations
     */
    private boolean hasSoapBindingOperations(Definition wsdlDefinition) {

        if (wsdlDefinition == null) {
            return false;
        }
        for (Object bindingObj : wsdlDefinition.getAllBindings().values()) {
            if (bindingObj instanceof Binding) {
                Binding binding = (Binding) bindingObj;
                for (Object ex : binding.getExtensibilityElements()) {
                    if (ex instanceof SOAPBinding) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns if the provided WSDL definition contains SOAP 1.2 binding operations
     *
     * @return whether the provided WSDL definition contains SOAP 1.2 binding operations
     */
    private boolean hasSoap12BindingOperations(Definition wsdlDefinition) {

        if (wsdlDefinition == null) {
            return false;
        }
        for (Object bindingObj : wsdlDefinition.getAllBindings().values()) {
            if (bindingObj instanceof Binding) {
                Binding binding = (Binding) bindingObj;
                for (Object ex : binding.getExtensibilityElements()) {
                    if (ex instanceof SOAP12Binding) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
