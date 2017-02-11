# Message Definition BNF Grammar

```bnf
Protocol := (<Message>)*

Message := <identifier> <openBrace> (<MessageField>)+ <closeBrace>

Field := <identifier> <FieldType> (<Attribute>)* <lineEnd>

FieldType := <string> | <number> | <boolean> | <Array> | <FixedString>

Array := <array> <arrayOpen> <FieldType> <arrayClose>

FixedString := <string> <arrayOpen> <integerLiteral> <arrayClose>

Attribute := <AttributeName> <equals> <AttributeValue>

AttributeName := <jsonKey> | <cType>

AttributeValue := <identifier>

identifier := [_a-zA-Z][_a-zA-Z0-9]*

openBrace := "{"

closeBrace := "}"

lineEnd := ";"

string := "String"

number := "Number"

boolean := "Boolean"

array := "Array"

arrayOpen := "["

arrayClose := "]"

integerLiteral := [1-9][0-9]*

equals := "="

jsonKey := "jsonKey"

cType := "cType"
```