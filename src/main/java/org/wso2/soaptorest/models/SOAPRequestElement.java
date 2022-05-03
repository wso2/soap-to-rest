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

import org.w3c.dom.Document;

/**
 * Data model for a single soap request.
 */
public class SOAPRequestElement {

    private final Document soapRequestBody;
    private final String soapAction;
    private final String soapNamespace;
    private final String nameSpace;

    public SOAPRequestElement(Document soapRequestBody, String soapAction, String namespace, String soapNamespace) {

        this.soapRequestBody = soapRequestBody;
        this.soapAction = soapAction;
        this.nameSpace = namespace;
        this.soapNamespace = soapNamespace;
    }

    public Document getSoapRequestBody() {

        return soapRequestBody;
    }

    public String getSoapAction() {

        return soapAction;
    }

    public String getSoapNamespace() {

        return soapNamespace;
    }

    public String getNamespace() {

        return nameSpace;
    }

}
