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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.wso2.soaptorest.exceptions.SOAPToRESTException;
import org.wso2.soaptorest.models.SOAPtoRESTConversionData;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SOAPToRESTConverterTest {

    @Test
    void testFileGetSOAPtoRESTConversionData() throws SOAPToRESTException {

        SOAPtoRESTConversionData soaPtoRESTConversionData = SOAPToRESTConverter.getSOAPtoRESTConversionData("src/test/resources" +
                "/calculator/calculator.wsdl", "Test API", "1.0.0");
        assertTrue(StringUtils.isNotBlank(soaPtoRESTConversionData.getOASString()));
        assertEquals(soaPtoRESTConversionData.getAllSOAPRequestBodies().size(), 4);
        assertEquals(soaPtoRESTConversionData.getSoapService(), "CalculatorService");
        assertEquals(soaPtoRESTConversionData.getSoapPort(), "CalculatorPort");

    }

    @Test
    void testURLGetSOAPtoRESTConversionData() throws MalformedURLException, SOAPToRESTException {

        URL url = new URL("https://raw.githubusercontent.com/wso2/soap-to-rest/main/src/test/resources/calculator-remote/calculator.wsdl");
        SOAPtoRESTConversionData soaPtoRESTConversionData = SOAPToRESTConverter.getSOAPtoRESTConversionData(url, "Test API", "1.0.0");
        assertTrue(StringUtils.isNotBlank(soaPtoRESTConversionData.getOASString()));
        assertEquals(soaPtoRESTConversionData.getAllSOAPRequestBodies().size(), 4);
        assertEquals(soaPtoRESTConversionData.getSoapService(), "CalculatorService");
        assertEquals(soaPtoRESTConversionData.getSoapPort(), "CalculatorPort");

    }

    @Test
    void testIssue28() throws MalformedURLException, SOAPToRESTException {

        URL url = new URL(
            "https://raw.githubusercontent.com/indika-dev/soap-to-rest/main/src/test/resources/issue-28/failing.wsdl");
        SOAPtoRESTConversionData soaPtoRESTConversionData =
            SOAPToRESTConverter.getSOAPtoRESTConversionData(url, "Test API", "1.0.0");
        assertTrue(StringUtils.isNotBlank(soaPtoRESTConversionData.getOASString()));
        assertEquals(1, soaPtoRESTConversionData.getAllSOAPRequestBodies().size());
        assertEquals(soaPtoRESTConversionData.getSoapService(), "myServer");
        assertEquals(soaPtoRESTConversionData.getSoapPort(), "myServerSOAP");

    }

}
