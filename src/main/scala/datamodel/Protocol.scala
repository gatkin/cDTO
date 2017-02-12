package datamodel

/**
  * Contains type definitions to internally represent a cDTO protocol.
  */

case class Protocol(name: String, messages: Seq[Message])

case class Message(name: String, fields: Seq[Field])

case class Field(name: String, fieldType: FieldType, jsonKey: String)

sealed trait FieldType
case class AliasedType(alias: String, underlyingType: BaseFieldType) extends FieldType

sealed trait BaseFieldType extends FieldType
case class ArrayType(elementType: BaseFieldType) extends BaseFieldType
case object BooleanType extends BaseFieldType
case object DynamicStringType extends BaseFieldType
case class FixedStringType(maxLength: Int) extends BaseFieldType
case object NumberType extends BaseFieldType
case class ObjectType(objectName: String) extends BaseFieldType
