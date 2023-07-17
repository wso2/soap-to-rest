# Limitations of SOAP to REST Feature

## XSD Schema Support
- xs:choice: ignore the ability to choose and generate with all the elements as required(need to change the generated body as required)
- xs:annotations: not supported
- xs:attributes: Working for basic attributes
	- Not supported: Default and Fixed Values for Attributes, Optional and Required Attributes
- xs:restrictions: Read base type only. Restrictions won’t apply
- xs:extensions: Generates with some usability issues. Can edit via IDE
- xs:complexType: If extended from the base, not generating 100% correct mapping, but can edit in the IDE
- xs:complexContent: works but with limitations if nested extensions present
- xs:simpleContent: works but with limitations if nested extensions present
- xs:all: generates the mapping but ignores the maxOccures restrictions
- xs:groups: generate the mapping without errors, group elements are ignored
- xs:attributeGroups: generate the mapping without errors, group elements are ignored
- xs:any: ignored but generated without errors
- xs:anyAttribute: ignored but generated without errors
- maxOccurs: only taken care of the unbounded case for arrays
- minOccurs: ignored and generate the mapping
- Mixed Content: ignored but generated normal XML payload
- substitutionGroup: ignored and generated
- Unions: are not supported
- Enumeration: not supported

Primitive Data types defined in XSD Schema Specification
Following data types are treated as string:
- anyURI
- base64Binary
- Duration
- hexBinary
- gDay
- gMonth
- gMonthDay
- gYear
- gYearMonth
- NOTATION
- QName

First-class support
- Boolean
- Date
- dateTime
- Decimal
- Double
- Float
- String
- Time

NOTE: All the not supported or partially generated items can be edited in the generated payload from Integration Studio IDE
