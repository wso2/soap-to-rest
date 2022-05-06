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

import io.swagger.v3.oas.models.OpenAPI;
import org.wso2.soaptorest.exceptions.SOAPToRESTException;
import org.wso2.soaptorest.models.SOAPtoRESTConversionData;
import org.wso2.soaptorest.models.WSDLInfo;
import org.wso2.soaptorest.utils.SOAPOperationExtractingUtil;

import java.net.URL;

public class SOAPToRESTConverter {
    public static SOAPtoRESTConversionData getSOAPtoRESTConversionData(URL url, String apiTitle, String apiVersion) throws
            SOAPToRESTException {

        WSDLProcessor wsdlProcessor = new WSDLProcessor();
        wsdlProcessor.init(url);
        SOAPOperationExtractingUtil soapOperationExtractingUtil = new SOAPOperationExtractingUtil();
        WSDLInfo wsdlInfo = soapOperationExtractingUtil.getWsdlInfo(wsdlProcessor.getWsdlDefinition());
        OpenAPI openAPI = OASGenerator.generateOpenAPIFromWSDL(wsdlInfo, wsdlProcessor.xsdDataModels, apiTitle,
                apiVersion);
        return SOAPRequestBodyGenerator.generateSOAPtoRESTConversionObjectFromOAS(openAPI, wsdlInfo.getSoapService(),
                wsdlInfo.getSoapPort());
    }

    public static SOAPtoRESTConversionData getSOAPtoRESTConversionData(String filePath, String apiTitle,
                                                                       String apiVersion) throws SOAPToRESTException {

        WSDLProcessor wsdlProcessor = new WSDLProcessor();
        wsdlProcessor.init(filePath);
        SOAPOperationExtractingUtil soapOperationExtractingUtil = new SOAPOperationExtractingUtil();
        WSDLInfo wsdlInfo = soapOperationExtractingUtil.getWsdlInfo(wsdlProcessor.getWsdlDefinition());
        OpenAPI openAPI = OASGenerator.generateOpenAPIFromWSDL(wsdlInfo, wsdlProcessor.xsdDataModels, apiTitle,
                apiVersion);
        return SOAPRequestBodyGenerator.generateSOAPtoRESTConversionObjectFromOAS(openAPI, wsdlInfo.getSoapService(),
                wsdlInfo.getSoapPort());
    }
}
