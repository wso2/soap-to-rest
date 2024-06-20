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

import java.io.File;
import java.net.URL;

public class SOAPToRESTConverter {

    public static SOAPtoRESTConversionData getSOAPtoRESTConversionData(URL url, String apiTitle, String apiVersion) throws
            SOAPToRESTException {

        WSDLProcessor wsdlProcessor = new WSDLProcessor();
        wsdlProcessor.init(url);
        return getSOAPtoRESTConversionData(wsdlProcessor, apiTitle, apiVersion);
    }

    public static SOAPtoRESTConversionData getSOAPtoRESTConversionData(String filePath, String apiTitle,
                                                                       String apiVersion) throws SOAPToRESTException {

        WSDLProcessor wsdlProcessor = new WSDLProcessor();
        wsdlProcessor.init(filePath);
        return getSOAPtoRESTConversionData(wsdlProcessor, apiTitle, apiVersion);
    }

    public static SOAPtoRESTConversionData getSOAPtoRESTConversionData(
        WSDLProcessor wsdlProcessor, String apiTitle, String apiVersion) throws SOAPToRESTException
    {
        SOAPOperationExtractingUtil soapOperationExtractingUtil = new SOAPOperationExtractingUtil();
        WSDLInfo wsdlInfo = soapOperationExtractingUtil.getWsdlInfo(wsdlProcessor.getWsdlDefinition());
        OpenAPI openAPI = OASGenerator.generateOpenAPIFromWSDL(wsdlInfo, wsdlProcessor.xsdDataModels, apiTitle,
                apiVersion);
        return SOAPRequestBodyGenerator.generateSOAPtoRESTConversionObjectFromOAS(openAPI, wsdlInfo.getSoapService(),
                wsdlInfo.getSoapPort());
    }

    public static void main(String[] args)
    {
        try {
            String wsdlFile = args[0];
            String apiTitle = args.length > 1 ? args[1] : (new File(wsdlFile)).getName();
            String apiVersion = args.length > 2 ? args[2] : "1.0.0";

            SOAPtoRESTConversionData data = getSOAPtoRESTConversionData(wsdlFile, apiTitle, apiVersion);

            System.out.println(data.getOASString());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: java -jar soaptorest-1.6-jar-with-dependencies.jar " +
                "{wsdl_file_path} [apiTitle] [apiVersion]");
        }
        catch (SOAPToRESTException e) {
            e.printStackTrace();
        }
    }
}
