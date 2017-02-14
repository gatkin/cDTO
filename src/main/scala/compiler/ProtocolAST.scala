package compiler

import scala.util.parsing.input.Positional

/**
  * Defines types to represent a parsed cDTO protocol definition file
  */

case class ProtocolAST(messages: Seq[MessageDefinition]) extends Positional

case class MessageDefinition(name: String, fields: Seq[FieldDefinition]) extends Positional

case class FieldDefinition(name: String, fieldType: FieldTypeDefinition, attributes: Seq[FieldAttribute]) extends Positional

sealed trait FieldTypeDefinition extends Positional
case class ArrayTypeDefinition(elementType: FieldTypeDefinition) extends FieldTypeDefinition
case class BooleanTypeDefinition() extends FieldTypeDefinition
case class DynamicStringTypeDefinition() extends FieldTypeDefinition
case class FixedStringTypeDefinition(maxLength: Int) extends FieldTypeDefinition
case class NumberTypeDefinition() extends FieldTypeDefinition
case class ObjectTypeDefinition(objectName: String) extends FieldTypeDefinition

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
