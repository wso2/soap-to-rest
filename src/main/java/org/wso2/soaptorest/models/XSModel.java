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

import java.util.ArrayList;
import java.util.List;

/**
 * Model for represent the all the Data Types in the given XSD
 */
public class XSModel {

    List<XSElement> elements = new ArrayList<>();
    List<XSAttribute> attributes = new ArrayList<>();
    List<XSDataType> xsDataTypes = new ArrayList<>();
    List<XSGroup> groups = new ArrayList<>();
    String targetNamespace;

    private boolean elementFormDefaultQualified = false;

    public void addElement(XSElement element) {

        elements.add(element);
    }

    public void addAttribute(XSAttribute attribute) {

        attributes.add(attribute);
    }

    public List<XSDataType> getXsDataTypes() {

        return xsDataTypes;
    }

    public void addXSDataType(XSDataType complexType) {

        xsDataTypes.add(complexType);
    }

    public void addGroup(XSGroup group) {

        groups.add(group);
    }

    public List<XSGroup> getGroups() {
        return groups;
    }

    public void setTargetNamespace(String targetNamespace) {

        this.targetNamespace = targetNamespace;
    }

    public List<XSElement> getElements() {
        return elements;
    }

    public boolean isElementFormDefaultQualified() {

        return elementFormDefaultQualified;
    }

    public void setElementFormDefaultQualified(boolean elementFormDefaultQualified) {

        this.elementFormDefaultQualified = elementFormDefaultQualified;
    }
}
