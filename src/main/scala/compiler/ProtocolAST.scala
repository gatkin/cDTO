package compiler

import scala.util.parsing.input.Positional

/**
  * Defines types to represent a parsed cDTO protocol definition file
  */

case class ProtocolAST(messages: Seq[Message]) extends Positional

case class Message(name: String, fields: Seq[Field]) extends Positional

case class Field(name: String, fieldType: FieldType, attributes: Seq[FieldAttribute])

sealed trait FieldType extends Positional
case class ArrayType(elementType: FieldType) extends FieldType
case class BooleanType() extends FieldType
case class DynamicStringType() extends FieldType
case class FixedStringType(maxLength: Int) extends FieldType
case class NumberType() extends FieldType
case class ObjectType(objectName: String) extends FieldType

sealed trait FieldAttribute extends Positional
case class CTypeAttribute(cType: String) extends FieldAttribute
case class JSONKeyAttribute(key: String) extends FieldAttribute

/*
 Tokens
 */
case class ArrayClose() extends Positional
case class ArrayOpen() extends Positional
case class CloseBrace() extends Positional
case class Equals() extends Positional
case class Identifier(id: String) extends Positional
case class IntegerLiteral(value: Int) extends Positional
case class JSONKey() extends Positional
case class LineEnd() extends Positional
case class OpenBrace() extends Positional
case class StringType() extends Positional
