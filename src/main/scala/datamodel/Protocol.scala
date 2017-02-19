package datamodel

/**
  * Contains type definitions to internally represent a cDTO protocol.
  */

case class Protocol(name: String, messages: Seq[Message])

case class Message(name: String, fields: Seq[Field])

case class Field(name: String, fieldType: FieldType, jsonKey: String)

/**
  * The FieldType trait represents all possible message field types. At the
  * root level is the ArrayType which can contain any other type except
  * another nested array. At the next level down is the AliasedType and
  * ObjectType. These types can be contained in arrays, but they cannot
  * be aliased. Finally, at the bottom level, are the base field types.
  * These types can be contained in arrays, aliased, or both.
  */
sealed trait FieldType
case class ArrayType(elementType: SimpleFieldType) extends FieldType

sealed trait SimpleFieldType extends FieldType
case class AliasedType(alias: String, underlyingType: BaseFieldType) extends SimpleFieldType
case class ObjectType(objectName: String) extends SimpleFieldType

sealed trait BaseFieldType extends SimpleFieldType
case object BooleanType extends BaseFieldType
case object DynamicStringType extends BaseFieldType
case class FixedStringType(maxLength: Int) extends BaseFieldType
case object NumberType extends BaseFieldType

