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
import java.util.ArrayList;
import java.util.List;

/**
 * Model for represent Group element in the given XSD
 */
public class XSGroup {

    QName refKey;
    QName name;
    List<XSSequence> sequenceList = new ArrayList<>();
    List<XSChoice> choiceList = new ArrayList<>();

    public List<XSSequence> getSequenceList() {

        return sequenceList;
    }

    public void addSequence(XSSequence sequence) {

        sequenceList.add(sequence);
    }

    public List<XSChoice> getChoiceList() {

        return choiceList;
    }

    public QName getRefKey() {

        return refKey;
    }

    public void setRefKey(QName refKey) {

        this.refKey = refKey;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }
}
