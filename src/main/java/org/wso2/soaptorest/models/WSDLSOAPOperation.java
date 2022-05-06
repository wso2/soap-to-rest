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
package org.wso2.soaptorest.models;

import java.util.List;

/**
 * Extracted SOAP operation details representation.
 */
public class WSDLSOAPOperation {

    private String name;
    private String soapBindingOpName;
    private String soapAction;
    private String targetNamespace;
    private String style;
    private String messageType;
    private String httpVerb;
    private List<WSDLParameter> inputParameterModel;
    private List<WSDLParameter> outputParameterModel;

    public WSDLSOAPOperation() {

    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getSoapAction() {

        return soapAction;
    }

    public void setSoapAction(String soapAction) {

        this.soapAction = soapAction;
    }

    public String getTargetNamespace() {

        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {

        this.targetNamespace = targetNamespace;
    }

    public String getStyle() {

        return style;
    }

    public void setStyle(String style) {

        this.style = style;
    }

    public String getHttpVerb() {

        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {

        this.httpVerb = httpVerb;
    }


    public String getSoapBindingOpName() {

        return soapBindingOpName;
    }

    public void setSoapBindingOpName(String soapBindingOpName) {

        this.soapBindingOpName = soapBindingOpName;
    }

    public List<WSDLParameter> getInputParameterModel() {

        return inputParameterModel;
    }

    public void setInputParameterModel(List<WSDLParameter> inputParameterModel) {

        this.inputParameterModel = inputParameterModel;
    }

    public List<WSDLParameter> getOutputParameterModel() {

        return outputParameterModel;
    }

    public void setOutputParameterModel(List<WSDLParameter> outputParameterModel) {

        this.outputParameterModel = outputParameterModel;
    }

    public String getMessageType() {

        return messageType;
    }

    public void setMessageType(String messageType) {

        this.messageType = messageType;
    }
}

