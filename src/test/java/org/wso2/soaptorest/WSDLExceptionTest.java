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

import org.junit.jupiter.api.Test;
import org.wso2.soaptorest.exceptions.SOAPToRESTException;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WSDLExceptionTest {

    @Test
    void invalidWSDLFileLocation() {
        Exception exception = assertThrows(SOAPToRESTException.class, () -> SOAPToRESTConverter.getSOAPtoRESTConversionData("invalid " +
                "file", "test api", "1.0.0"));

        String expectedMessage = "Error while reading WSDL document";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void invalidWSDLURLLocation() {
        Exception exception = assertThrows(MalformedURLException.class, () -> SOAPToRESTConverter.getSOAPtoRESTConversionData(new URL(
                "invalid url"), "test api", "1.0.0"));

        String expectedMessage = "invalid url";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void invalidXSDReferredFileLocation() {
        Exception exception = assertThrows(SOAPToRESTException.class, () -> SOAPToRESTConverter.getSOAPtoRESTConversionData("src/" +
                "test/resources/complex/invalid.wsdl", "Test API", "1.0.0"));

        String expectedMessage = "Cannot process the provide WSDL file";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
