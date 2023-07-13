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

/**
 * Constants used for wsdl processing in soap to rest mapping.
 */
public class SOAPToRESTConstants {

    public static final String EMPTY_STRING = "";
    public static final String SOAP_VERSION_11 = "1.1";
    public static final String SOAP_VERSION_12 = "1.2";
    public static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP12_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
    public static final String X_NAMESPACE_QUALIFIED = "x-namespace-qualified";

    public static final String QUALIFIED = "qualified";
    public static final String ROOT_ELEMENT_PREFIX = "rootElement_";
    public static final String XMLNS = "xmlns";
    public static final String X_WSO2_UNIQUE_NAMESPACE = "x-wso2-empty-namespace";
    public static final String BASE_CONTENT_KEYWORD = "_base";
    public static final String ATTR_CONTENT_KEYWORD = "_attr";
    public static final String SOAP_RPC_MESSAGE_TYPE = "rpc";
    public static final String DEFAULT_DESCRIPTION = "Default Description";
    public static final String WSDL_VERSION_11 = "1.1";

    //OAS Constants
    public static final String DEFAULT_CONTENT_TYPE = "*/*";
    public static final String SOAP_ACTION = "soap-action";
    public static final String SOAP_OPERATION = "soap-operation";
    public static final String NAMESPACE = "namespace";
    public static final String WSO2_SOAP = "x-wso2-soap";
    public static final String SOAP_VERSION = "x-soap-version";
    public static final String SOAP_MESSAGE_TYPE = "x-soap-message-type";
    public static final String SOAP_STYLE = "x-soap-style";

    //Sequence Generator Constants
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String PATH_SEPARATOR = "/";

    public static final String NAMESPACE_PREFIX = "web";
    public static final String ARRAY_PLACEHOLDER = "ARRAY_PLACEHOLDER";

    // HTTP Constants
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_GET = "GET";


    //OAS Generator Constants
    public static final String OAS_DEFINITIONS_PREFIX = "#/components/schemas/";
    public static final String OAS_DEFINITIONS_ROOT_ELEMENT_PATH = "#/components/schemas/rootElement_";
    public static final String EXTENSION_NAME = "ExtensionObject";
    public static final String OAS_ALL_MEDIA_TYPE = "*/*";
    public static final String OBJECT_TYPE = "object";
    public static final String ARRAY_TYPE = "array";
    public static final String IF_PLACEHOLDER = "ifPlaceholder";
    public static final String ELSE_PLACEHOLDER = "elsePlaceholder";
    public static final String QUESTION_MARK_PLACEHOLDER = "questionPlaceholder";
    public static final String ATTRIBUTE_PLACEHOLDER = "attributePlaceholder";
    public static final String IS_EMPTY_ATTRIBUTE = "addIsEmptyCheck";

}
