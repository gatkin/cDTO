package compiler

import datamodel._

/**
  * Contains functionality for analyzing and validating parsed definitions of
  * message fields
  */
object MessageFieldDefinitionAnalyzer {

  /**
    * Analyzes and validates the provided parsed field definition
    * @param definition - Parsed field definition
    * @return Field that corresponds to the definition or an error if the definition
    *         is invalid
    */
  def apply(definition: FieldDefinition): Either[FieldDefinitionError, Field] = {
     for {
       fieldType <- fieldTypeGet(definition).right
       jsonKey <- jsonKeyGet(definition).right
     } yield Field(definition.name, fieldType, jsonKey)
    }

  /**
    * Returns the type to use for the field based on the definition
    * @param definition - Field definition
    * @return Type of field or an error if the field definition is invalid
    */
  private def fieldTypeGet(definition: FieldDefinition): Either[FieldDefinitionError, FieldType] = {
    val baseType = baseTypeGet(definition.fieldType)

    val cTypeAttribute = definition.attributes.collect({ case CTypeAttribute(cType) => cType })

    // The C-type attribute is optional. If it is not present, then the field does not have
    // a type alias. Otherwise, only some types are allowed to have an alias.
    cTypeAttribute match {
      // No alias, just use the base type
      case Nil => Right(baseType)

      // A valid alias was provided
      case cType :: Nil if baseTypeCanHaveAlias(baseType) => Right(AliasedType(cType, baseType))

      case _ :: Nil => Left(TypeAliasNotAllowedError(baseType.toString, definition.name))

      case _ => Left(DuplicateAttributeError(Constants.C_TYPE_ATTRIBUTE, definition.name))
    }
  }

  /**
    * Returns the base type based on the provided type definition. The base type does not take into
    * account the possibility that the type might be aliased if a C-type attribute was provided in
    * the field definition
    * @param typeDefinition - Type definition
    * @return Base type that corresponds to the type definition
    */
  private def baseTypeGet(typeDefinition: FieldTypeDefinition): BaseFieldType = {
    typeDefinition match {
      case ArrayTypeDefinition(elementTypeDef) => ArrayType(baseTypeGet(elementTypeDef))
      case BooleanTypeDefinition() => BooleanType
      case DynamicStringTypeDefinition() => DynamicStringType
      case FixedStringTypeDefinition(maxLength) => FixedStringType(maxLength)
      case NumberTypeDefinition() => NumberType
      case ObjectTypeDefinition(objectName) => ObjectType(objectName)
    }
  }

  /**
    * Returns true if the provided base type can have a C-type alias. Only some base types can be aliased.
    * User-defined object types cannot be aliased because the object type should correspond to one of the
    * message definitions from the protocol definition. The name of the user-defined object type should already
    * be a type alias. Arrays can be type-aliased only if their element type can be aliased.
    * @param baseType - Base type
    * @return true if the given base type can have an alias
    */
  private def baseTypeCanHaveAlias(baseType: BaseFieldType): Boolean = {
    baseType match {
      case ArrayType(elementType) => baseTypeCanHaveAlias(elementType)
      case BooleanType => true
      case DynamicStringType => true
      case FixedStringType(_) => true
      case NumberType => true
      case ObjectType(_) => false
    }
  }

  /**
    * Returns the JSON key to used for the field based on the provided field definition.
    * If no JSON key attribute was provided, then this falls back to using a default value
    * for the fields JSON key
    * @param definition - Field definition
    * @return Either the field's JSON key or an error if duplicate JSON key attributes were
    *         provided
    */
  private def jsonKeyGet(definition: FieldDefinition): Either[FieldDefinitionError, String] = {
    val jsonKeyAttribute = definition.attributes.collect({ case JSONKeyAttribute(key) => key })

    jsonKeyAttribute match {
      case Nil => Right(defaultJSONKeyGet(definition))
      case key :: Nil => Right(key)
      case _ => Left(DuplicateAttributeError(Constants.JSON_KEY_ATTRIBUTE, definition.name))
    }
  }

  /**
    * Returns the default JSON key to use for the provided field if no JSON key attribute
    * was provided in the field definition. In this case, it simply falls back to using the
    * field's name as the JSON key
    * @param definition - Field definition
    * @return Default JSON key for the field
    */
  private def defaultJSONKeyGet(definition: FieldDefinition): String = {
    definition.name
  }
}
