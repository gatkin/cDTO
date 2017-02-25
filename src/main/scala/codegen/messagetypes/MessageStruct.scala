package codegen.messagetypes

import codegen.Constants
import codegen.types._
import datamodel._

object MessageStruct {

  /**
    * Generates a C-struct definition based on the provided message.
    * @param message cDTO message
    * @return Definition of the struct corresponding to the cDTO message
    */
  def apply(message: Message): StructDefinition = {
    StructDefinition(
      name = message.name,
      fields = message.fields.flatMap(structField)
    )
  }

  /**
    * Gets the name of the C struct corresponding to the given message
    * @param message cDTO message
    * @return Name of struct corresponding to the message
    */
  def structName(message: Message): String = {
    message.name
  }

  /**
    * Gets the type declaration string for an array field that contains
    * the given type of elements.
    * @param elementType Type of elements contained in the array
    * @return String of the array's type declaration
    */
  def arrayFieldType(elementType: SimpleFieldType): String = {
    elementType match {
      case AliasedType(alias, _) => s"$alias*"
      case ObjectType(objectName) => s"$objectName*"
      case BooleanType => s"${Constants.defaultBooleanCType}*"
      case DynamicStringType => s"${Constants.defaultCharacterCType}**"
      case FixedStringType(_) => s"${Constants.defaultCharacterCType}**"
      case NumberType => s"${Constants.defaultNumberCType}*"
    }
  }

  /**
    * Returns the name of the count field to use for an array field
    * @param arrayFieldName Name of the array field
    * @return Name of the count field corresponding to the give array field
    */
  def arrayCountFieldName(arrayFieldName: String): String = {
    s"${arrayFieldName}_cnt"
  }

  /**
    * Gets a list of C-struct field definitions corresponding to the
    * provided cDTO message field. If the field is an array field, then
    * this will return a field definition for the array itself as well
    * as a definition for a field to contain the array's count.
    * @param field cDTO field
    * @return A list of fields to declare the cDTO field as a member of
    *         a C struct
    */
  private def structField(field: Field): Seq[StructField] = {
    field.fieldType match {
      case ArrayType(elementType) => arrayField(field.name, elementType)
      case simpleType:SimpleFieldType => List(simpleField(field.name, simpleType))
    }
  }

  /**
    * Gets the C struct field definitions corresponding to a cDTO array
    * field with the given name and element type.
    * @param fieldName Name of array field
    * @param elementType Type of elements contained within the array field
    * @return Definitions for both the array field and the array count field
    */
  private def arrayField(fieldName: String, elementType: SimpleFieldType): Seq[StructField] = {
    val typeDeclaration = arrayFieldType(elementType)

    List(
      SimpleStructField(fieldName, typeDeclaration),
      SimpleStructField(arrayCountFieldName(fieldName), Constants.defaultIntCType)
    )
  }

  /**
    * Gets the definition for a simple-type field with the given name and type
    * @param fieldName Name of field
    * @param fieldType Type of field
    * @return String to declare field as a member of a C struct
    */
  private def simpleField(fieldName: String, fieldType: SimpleFieldType): StructField = {
    fieldType match {
      case AliasedType(alias, _) => SimpleStructField(fieldName, alias)
      case ObjectType(objectName) => SimpleStructField(fieldName, objectName)
      case BooleanType => SimpleStructField(fieldName, Constants.defaultBooleanCType)
      case DynamicStringType => SimpleStructField(fieldName, Constants.defaultCharacterCType + "*")
      case FixedStringType(maxLength) => FixedArrayStructField(fieldName, Constants.defaultCharacterCType, maxLength + 1) // include room for null-terminator
      case NumberType => SimpleStructField(fieldName, Constants.defaultNumberCType)
    }
  }
}
