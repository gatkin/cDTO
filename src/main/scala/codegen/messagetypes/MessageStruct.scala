package codegen.types

import codegen.Constants
import datamodel._

object MessageStruct {

  /**
    * Generates a C-struct definition string based on the provided message.
    * @param message cDTO message
    * @return A string containing a definition for the C struct corresponding to
    *         the provided message
    */
  def apply(message: Message): String = {
    s"""typedef struct {
       |${structBodyGet(message.fields)}
       |} ${structName(message)};""".stripMargin
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
    // Need to handle the special case of fixed strings being declared
    // differently when they are array elements than when they are
    // simple struct fields.
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
    * Returns a string for the body of a struct definition consisting of
    * declarations for all fields in the struct.
    * @param fields List of message fields
    * @return A string containing declarations for all struct fields
    */
  private def structBodyGet(fields: Seq[Field]): String = {
    val fieldDeclarations = for {
      field <- fields
      fieldDeclaration <- structFieldDeclarationsGet(field)
    } yield s"${Constants.indentation}$fieldDeclaration"

    fieldDeclarations.mkString("\n")
  }

  /**
    * Gets a list of C-struct field definitions corresponding to the
    * provided cDTO message field. If the field is an array field, then
    * this will return a field declaration string for the array itself
    * as well as a field declaration string for the array's count.
    * @param field cDTO field
    * @return A list of strings to declare the cDTO field as a member of
    *         a C struct
    */
  private def structFieldDeclarationsGet(field: Field): Seq[String] = {
    field.fieldType match {
      case ArrayType(elementType) => arrayFieldDeclarationGet(field.name, elementType)
      case simpleType:SimpleFieldType => List(simpleFieldDeclarationGet(field.name, simpleType))
    }
  }

  /**
    * Gets the C struct member definition strings corresponding to a
    * cDTO array field with the given name and element type.
    * @param fieldName Name of array field
    * @param elementType Type of elements contained within the array field
    * @return A list of strings to declare the array and its count as members
    *         of a struct
    */
  private def arrayFieldDeclarationGet(fieldName: String, elementType: SimpleFieldType): Seq[String] = {
    val typeDeclaration = arrayFieldType(elementType)

    // Also need a declaration for the array count struct field
    List(
      s"$typeDeclaration $fieldName;",
      s"int ${arrayCountFieldName(fieldName)};"
    )
  }

  /**
    * Gets the declaration string to declare a simple-type field
    * as a C struct member with the given name and type
    * @param fieldName Name of field
    * @param fieldType Type of field
    * @return String to declare field as a member of a C struct
    */
  private def simpleFieldDeclarationGet(fieldName: String, fieldType: SimpleFieldType): String = {
    val fieldDeclaration = fieldType match {
      case AliasedType(alias, _) => s"$alias $fieldName"
      case ObjectType(objectName) => s"$objectName $fieldName"
      case BooleanType => s"${Constants.defaultBooleanCType} $fieldName"
      case DynamicStringType => s"${Constants.defaultCharacterCType}* $fieldName"
      case FixedStringType(maxLength) => s"${Constants.defaultCharacterCType} $fieldName[$maxLength + 1]"
      case NumberType => s"${Constants.defaultNumberCType} $fieldName"
    }

    s"$fieldDeclaration;"
  }
}
