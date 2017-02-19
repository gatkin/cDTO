package compiler

import datamodel._

/**
  * Contains functionality for analyzing and validating parsed definitions of
  * message fields
  */
object FieldDefinitionAnalyzer {

  /**
    * Analyzes and validates the provided parsed field definition
    * @param definition - Parsed field definition
    * @return Field that corresponds to the definition or an error if the definition
    *         is invalid
    */
  def apply(definition: FieldDefinition): Either[InvalidFieldError, Field] = {
     val field = for {
       fieldType <- fieldTypeGet(definition).right
       jsonKey <- jsonKeyGet(definition).right
     } yield Field(definition.name, fieldType, jsonKey)

      field.fold(
        error => Left(InvalidFieldError(definition.name, error)),
        validField => Right(validField)
      )
    }

  /**
    * Returns the type to use for the field based on the definition
    * @param definition - Field definition
    * @return Type of field or an error if the field definition is invalid
    */
  private def fieldTypeGet(definition: FieldDefinition): Either[FieldDefinitionError, FieldType] = {
    definition.fieldType match {
      case ArrayTypeDefinition(elementTypeDef) => arrayFieldTypeGet(elementTypeDef, definition.attributes)
      case simpleTypeDef:SimpleTypeDefinition => simpleFieldTypeGet(simpleTypeDef, definition.attributes)
    }
  }

  /**
    * Gets the field type for array field.
    * @param attributes - Field attributes provided in the field definition
    * @param elementTypeDef - Definition of the array's element type
    * @return An array type with the defined element type or an error if the definition is invalid.
    */
  private def arrayFieldTypeGet(elementTypeDef: SimpleTypeDefinition, attributes: Seq[FieldAttribute]) = {
    simpleFieldTypeGet(elementTypeDef, attributes)
      .right.map(elementType => ArrayType(elementType))
  }

  /**
    * Gets the field type for a simple non-array field
    * @param attributes - Field attributes provided in the field definition
    * @param typeDefinition - Definition of the field's type
    * @return Type of the field corresponding to the definition or an error if the definition is invalid
    */
  private def simpleFieldTypeGet(typeDefinition: SimpleTypeDefinition, attributes: Seq[FieldAttribute]) = {
    val simpleFieldType = typeDefinition match {
      case BooleanTypeDefinition() => BooleanType
      case DynamicStringTypeDefinition() => DynamicStringType
      case FixedStringTypeDefinition(maxLength) => FixedStringType(maxLength)
      case NumberTypeDefinition() => NumberType
      case ObjectTypeDefinition(objectName) => ObjectType(objectName)
    }

    typeCheckAlias(simpleFieldType, attributes)
  }

  /**
    * Checks whether the field type should be aliased based on whether an
    * C-type field attributes were provided.
    * @param attributes - Field attributes provided in the field definition
    * @param fieldType - Type of the field
    * @return An aliased type if an alias was provided, the unmodified field type if no alias was
    *         specified, or an error if the definition is invalid
    */
  private def typeCheckAlias(fieldType: SimpleFieldType, attributes: Seq[FieldAttribute]) = {
    val cTypeAliases = attributes.collect({ case CTypeAttribute(cType) => cType })

    cTypeAliases match {
      case Nil => Right(fieldType)
      case cTypeAlias :: Nil => typeSetAlias(cTypeAlias, fieldType)
      case _ => Left(DuplicateAttributeError(Constants.C_TYPE_ATTRIBUTE))
    }
  }

  /**
    * Creates an aliased type with the provided underlying type and alias name
    * @param cTypeAlias - C-type alias
    * @param underlyingType - Underlying type to alias
    * @return An aliased type or an error if the underlying type cannot be aliased
    */
  private def typeSetAlias(cTypeAlias: String, underlyingType: SimpleFieldType): Either[FieldDefinitionError, SimpleFieldType] = {
    underlyingType match {
      case AliasedType(_, _) => Left(TypeAliasNotAllowedError(underlyingType.toString))
      case ObjectType(_) => Left(TypeAliasNotAllowedError(underlyingType.toString))
      case baseType:BaseFieldType => Right(AliasedType(cTypeAlias, baseType))
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
      case _ => Left(DuplicateAttributeError(Constants.JSON_KEY_ATTRIBUTE))
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
