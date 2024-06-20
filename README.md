# SOAP to REST Conversion Java Library

## Introduction

This open source library is capable of converting SOAP backend service to REST backends. The library is written to get
the SOAP Webservice contract using WSDL file and generate OpenAPI Specification
(Swagger file) along with the required SOAP Request bodies that will convert the REST JSON input to SOAP message. There
are two methods to convert the SOAP Endpoints. Either using the URL of the real SOAP backend or the Zip file contain all
the WSDL files and respective XSD files.

## How to build

Clone the repository and build the project using executing following command in terminal

```mvn clean install```

Prerequisites

* Maven 3.6.3 or higher (tested on 3.6.3)
* Java 11.0 or higher (tested on java 11)

## How to use

There are two methods in SOAPToRestConverter.java that convert the WSDL to REST Endpoint

1) Use the following method to generate REST service from WSDL URL

```java
getSOAPtoRESTConversionData(URL url,String apiTitle,String apiVersion)
```

2) Use the following method to generate REST service from WSDL File

```java
getSOAPtoRESTConversionData(String filePath,String apiTitle,String apiVersion)
```

Both methods will return an object from SOAPtoRESTConversionData.java class which contains following

```java
OpenAPI openAPI;
        Map<String, SOAPRequestElement> soapRequestBodyMapping;
        String soapService;
        String soapPort;
```

* ``openAPI``, ``soapService`` and ``soapPort`` are the respective representation of the SOAP endpoint
* ``soapRequestBodyMapping`` contains a map of SOAPRequestElement for all the SOAP operations.

``SOAPRequestElement`` Contains following,

```java
Document soapRequestBody;
        String soapAction;
        String soapNamespace;
        String nameSpace;
```

``Document `` contains ```org.w3c.dom.Document``` of the Request message that need to send to the SOAP backend under
``soapAction``. This Document contains the message with the placeholders for JSON input

## Run in Command-line

Build the JAR with command ``mvn package``. Then you can generate a YAML output for any WSDL file like this:

```
java -jar target/soaptorest-1.6-jar-with-dependencies.jar src/test/resources/complex/nested.wsdl MyNestedRestAPI 1.6.2
```

After the jar file in the command, the 1st parameter (required) is the path to the WSDL file. The 2nd parameter is the REST API title (default WSDL file name), and the 3rd is the version number (default "1.0.0").


## License

```
Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

WSO2 Inc. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
```

