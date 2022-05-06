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

import java.util.Set;

/**
 * Information extracted from WSDL.
 */
public class WSDLInfo {

    private String version;
    private boolean hasSoapBindingOperations;
    private boolean hasSoap12BindingOperations;
    private Set<WSDLSOAPOperation> soapBindingOperations;
    private String soapService;
    private String soapPort;

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public void setHasSoapBindingOperations(boolean hasSoapBindingOperations) {

        this.hasSoapBindingOperations = hasSoapBindingOperations;
    }

    public boolean hasSoapBindingOperations() {

        return hasSoapBindingOperations;
    }

    public boolean isHasSoap12BindingOperations() {

        return hasSoap12BindingOperations;
    }

    public void setHasSoap12BindingOperations(boolean hasSoap12BindingOperations) {

        this.hasSoap12BindingOperations = hasSoap12BindingOperations;
    }

    public Set<WSDLSOAPOperation> getSoapBindingOperations() {

        return soapBindingOperations;
    }

    public void setSoapBindingOperations(Set<WSDLSOAPOperation> soapBindingOperations) {

        this.soapBindingOperations = soapBindingOperations;
    }

    public String getSoapService() {

        return this.soapService;
    }

    public void setSoapService(String soapService) {

        this.soapService = soapService;
    }

    public String getSoapPort() {

        return this.soapPort;
    }

    public void setSoapPort(String soapPort) {

        this.soapPort = soapPort;
    }
}