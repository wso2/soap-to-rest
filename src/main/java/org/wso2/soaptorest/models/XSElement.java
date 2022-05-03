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

import javax.xml.namespace.QName;

/**
 * Model for represent Element in the given XSD
 */
public class XSElement {

    QName name;
    QName type;
    QName refKey;
    XSDataType inlineComplexType;
    boolean isArray;

    public QName getName() {

        return name;
    }

    public void setName(QName name) {

        this.name = name;
    }

    public QName getType() {

        return type;
    }

    public void setType(QName type) {

        this.type = type;
    }

    public XSDataType getInlineComplexType() {

        return inlineComplexType;
    }

    public void setInlineComplexType(XSDataType inlineComplexType) {

        this.inlineComplexType = inlineComplexType;
    }

    public QName getRefKey() {

        return refKey;
    }

    public void setRefKey(QName refKey) {

        this.refKey = refKey;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }
}
