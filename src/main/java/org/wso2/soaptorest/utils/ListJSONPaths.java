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

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import io.swagger.oas.inflector.examples.models.ArrayExample;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.examples.models.ObjectExample;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class to extract all the json paths of the given json string
 */
public class ListJSONPaths {

    /**
     * This method will return a list of all the available json paths of the input json string
     *
     * @param json input json file
     * @return the arraylist of the available json paths
     */
    public static ArrayList<String> getJsonPaths(String json) {

        ArrayList<String> pathList = new ArrayList<>();
        JSONObject object = new JSONObject(json);
        if (json != JSONObject.NULL) {
            readObject(object, null, pathList);
        }
        return pathList;
    }

    private static void readObject(JSONObject object, String jsonPath, ArrayList<String> pathList) {

        Iterator<?> keysItr = object.keys();
        String parentPath = jsonPath;
        if (keysItr.hasNext()) {
            while (keysItr.hasNext()) {
                String key = (String) keysItr.next();
                Object value = object.get(key);
                jsonPath = (jsonPath == null) ? key : parentPath + "." + key;

                if (value instanceof JSONArray) {
                    readArray((JSONArray) value, jsonPath, pathList);
                } else if (value instanceof JSONObject) {
                    readObject((JSONObject) value, jsonPath, pathList);
                } else {
                    pathList.add(jsonPath);
                }
            }
        } else if (StringUtils.isNotBlank(jsonPath)) {
            pathList.add(jsonPath);
        }
    }

    private static void readArray(JSONArray array, String jsonPath, ArrayList<String> pathList) {

        String parentPath = jsonPath;
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            jsonPath = parentPath + "[" + i + "]";

            if (value instanceof JSONArray) {
                readArray((JSONArray) value, jsonPath, pathList);
            } else if (value instanceof JSONObject) {
                readObject((JSONObject) value, jsonPath, pathList);
            } else {
                pathList.add(jsonPath);
            }
        }
    }

    /*
     * This method will return a list of all the available json paths of the input json string
     * @param example example object
     * @return the arraylist of the available json paths
     */
    public static ArrayList<String> getJsonPathsFromExample(Example example, Map<String, String> jsonPathSchemaMapping) {
        ArrayList<String> pathList = new ArrayList<>();
        if (example != null) {
            listExamples("", example, pathList, jsonPathSchemaMapping);
        }
        return pathList;
    }

    /*
     * This method will return a list of all the available json paths of the input json string
     * @param parent parent json path
     * @param example example object
     * @param parameterJsonPathMapping list of json paths
     * @return the arraylist of the available json paths
     */
    public static void listExamples(String parent, Example example, ArrayList<String> parameterJsonPathMapping
    , Map<String, String> jsonPathSchemaMapping) {
        if (example != null) {
            if (SOAPToRESTConstants.OBJECT_TYPE.equals(example.getTypeName())) {
                Map<String, Example> values = ((ObjectExample) example).getValues();
                if (values != null) {
                    for (Map.Entry<String, Example> entry : values.entrySet()) {
                        String childKey = parent.isEmpty() ? entry.getKey() : parent + "." + entry.getKey();
                        if (entry.getValue() != null && entry.getValue().getName() != null) {
                            jsonPathSchemaMapping.put(entry.getKey(), entry.getValue().getName());
                        }
                        listExamples(childKey, entry.getValue(), parameterJsonPathMapping, jsonPathSchemaMapping);
                    }
                } else if (StringUtils.isNotBlank(parent)) {
                    parameterJsonPathMapping.add(parent);
                }
            } else if (SOAPToRESTConstants.ARRAY_TYPE.equals(example.getTypeName())) {
                List<Example> exampleArray = ((ArrayExample) example).getItems();
                String jsonPath;
                if (exampleArray.size() > 0) {
                    for (int i = 0; i < exampleArray.size(); i++) {
                        Example exampleItem = exampleArray.get(i);
                        jsonPath = parent + "[" + i + "]";
                        if (SOAPToRESTConstants.OBJECT_TYPE.equals(exampleItem.getTypeName())) {
                            listExamples(jsonPath, exampleItem, parameterJsonPathMapping, jsonPathSchemaMapping);
                        } else if (SOAPToRESTConstants.ARRAY_TYPE.equals(exampleItem.getTypeName())) {
                            listExamples(jsonPath, exampleItem, parameterJsonPathMapping, jsonPathSchemaMapping);
                        } else {
                            parameterJsonPathMapping.add(jsonPath);
                        }
                    }
                }
            } else if (StringUtils.isNotBlank(parent)) {
                parameterJsonPathMapping.add(parent);
            }
        }
    }
}
