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
 * Model for represent Choice element in the given XSD
 */
public class XSChoice {

    List<XSSequence> sequenceList = new ArrayList<>();
    List<XSChoice> choiceList = new ArrayList<>();
    List<XSGroup> groupsList = new ArrayList<>();
    List<XSElement> elementList = new ArrayList<>();

    public List<XSSequence> getSequenceList() {

        return sequenceList;
    }

    public List<XSChoice> getChoiceList() {

        return choiceList;
    }

    public List<XSGroup> getGroupsList() {

        return groupsList;
    }

    public List<XSElement> getElementList() {

        return elementList;
    }

    public void addElement(XSElement element) {

        elementList.add(element);
    }
}
