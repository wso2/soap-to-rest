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
 * Model for represent Data Type in the given XSD
 */
public class XSDataType {

    QName name;
    QName extensionBase;
    XSSequence xsSequence;
    XSChoice xsChoice;
    XSGroup xsGroup;

    public QName getName() {

        return name;
    }

    public void setName(QName name) {

        this.name = name;
    }

    public XSSequence getSequence() {

        return xsSequence;
    }

    public void setSequence(XSSequence sequence) {

        this.xsSequence = sequence;
    }

    public XSChoice getChoice() {

        return xsChoice;
    }

    public void setChoice(XSChoice choice) {

        this.xsChoice = choice;
    }

    public XSGroup getGroup() {

        return xsGroup;
    }

    public void setGroup(XSGroup group) {

        this.xsGroup = group;
    }

    public QName getExtensionBase() {

        return extensionBase;
    }

    public void setExtensionBase(QName extensionBase) {

        this.extensionBase = extensionBase;
    }
}
