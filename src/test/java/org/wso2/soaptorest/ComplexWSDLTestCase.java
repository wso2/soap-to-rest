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
import org.wso2.soaptorest.models.SOAPRequestElement;
import org.wso2.soaptorest.models.SOAPtoRESTConversionData;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplexWSDLTestCase {

    @Test
    void testNestedWSDL() throws SOAPToRESTException {

        SOAPtoRESTConversionData soaPtoRESTConversionData = SOAPToRESTConverter.getSOAPtoRESTConversionData("src/test/resources" +
                "/complex/nested.wsdl", "Test API", "1.0.0");
        assertTrue(StringUtils.isNotBlank(soaPtoRESTConversionData.getOASString()));
        assertEquals(soaPtoRESTConversionData.getAllSOAPRequestBodies().size(), 1);
        assertEquals(soaPtoRESTConversionData.getSoapService(), "nestedSampleService");
        assertEquals(soaPtoRESTConversionData.getSoapPort(), "nestedSamplePort");

    }

    @Test
    void testArrayWSDL() throws SOAPToRESTException, TransformerException {

        SOAPtoRESTConversionData soaPtoRESTConversionData = SOAPToRESTConverter.getSOAPtoRESTConversionData("src/test/resources" +
                "/complex/arrays.wsdl", "Test API", "1.0.0");
        assertTrue(StringUtils.isNotBlank(soaPtoRESTConversionData.getOASString()));
        assertEquals(soaPtoRESTConversionData.getAllSOAPRequestBodies().size(), 1);
        assertEquals(soaPtoRESTConversionData.getSoapService(), "arraysSampleService");
        assertEquals(soaPtoRESTConversionData.getSoapPort(), "arraysSamplePort");

        Set<Map.Entry<String, SOAPRequestElement>> soapElementEntry = soaPtoRESTConversionData.getAllSOAPRequestBodies();
        for (Map.Entry<String, SOAPRequestElement> requestElementEntry : soapElementEntry) {
            SOAPRequestElement soapRequestElement = requestElementEntry.getValue();
            StringWriter writer = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.transform(new DOMSource(soapRequestElement.getSoapRequestBody()),
                    new StreamResult(writer));
            String soapPayload = writer.getBuffer().toString();
            String complexInnerType = "<inner3 xmlns=\"\" ARRAY_PLACEHOLDER=\"inner1.inner2.inner3\">";
            assertTrue(soapPayload.contains(complexInnerType));
            assertEquals("http://example.com/", soapRequestElement.getNamespace());
            assertEquals("http://schemas.xmlsoap.org/soap/envelope/", soapRequestElement.getSoapNamespace());
            assertEquals("", soapRequestElement.getSoapAction());
        }
    }

    @Test
    void testGroupWSDL() throws SOAPToRESTException {

        SOAPtoRESTConversionData soaPtoRESTConversionData = SOAPToRESTConverter.getSOAPtoRESTConversionData("src/test/resources" +
                "/complex/groups.wsdl", "Test API", "1.0.0");
        assertTrue(StringUtils.isNotBlank(soaPtoRESTConversionData.getOASString()));
        assertEquals(soaPtoRESTConversionData.getAllSOAPRequestBodies().size(), 1);
        assertEquals(soaPtoRESTConversionData.getSoapService(), "groupsSampleService");
        assertEquals(soaPtoRESTConversionData.getSoapPort(), "groupsSamplePort");
    }

    @Test
    void testChoiceWSDL() throws SOAPToRESTException {

        SOAPtoRESTConversionData soaPtoRESTConversionData = SOAPToRESTConverter.getSOAPtoRESTConversionData("src/test/resources" +
                "/complex/choice.wsdl", "Test API", "1.0.0");
        assertTrue(StringUtils.isNotBlank(soaPtoRESTConversionData.getOASString()));
        assertEquals(soaPtoRESTConversionData.getAllSOAPRequestBodies().size(), 1);
        assertEquals(soaPtoRESTConversionData.getSoapService(), "choiceSampleService");
        assertEquals(soaPtoRESTConversionData.getSoapPort(), "choiceSamplePort");
    }
}