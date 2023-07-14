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

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.Map;
import java.util.Set;

/**
 * Data object that represent the SOAP Endpoint as OpenAPI including the soap request message format for all the
 * soap endpoints.
 */
public class SOAPtoRESTConversionData {

    private final OpenAPI openAPI;
    private final Map<String, SOAPRequestElement> soapRequestBodyMapping;
    private final String soapService;
    private final String soapPort;

    public SOAPtoRESTConversionData(OpenAPI openAPI, Map<String, SOAPRequestElement> soapRequestBodyMapping,
                                    String soapService, String soapPort) {

        this.openAPI = openAPI;
        this.soapRequestBodyMapping = soapRequestBodyMapping;
        this.soapService = soapService;
        this.soapPort = soapPort;
    }

    public Set<Map.Entry<String, SOAPRequestElement>> getAllSOAPRequestBodies() {

        return soapRequestBodyMapping.entrySet();
    }

    public String getOASString() {

        return Yaml.pretty(openAPI);
    }

    public String getSoapService() {

        return soapService;
    }

    public String getSoapPort() {

        return soapPort;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }
}
