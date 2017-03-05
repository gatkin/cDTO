package compiler

import datamodel._

/**
  * Contains functionality for analyzing and validating parsed definitions
  * of protocol messages.
  */
object MessageDefinitionAnalyzer {

  /**
    * Analyzes and validates the provided parsed message definition
    * @param definition - Parsed message definition
    * @return Either an error if the message definition is invalid, or the message
    *         corresponding to the definition.
    */
  def apply(definition: MessageDefinition): Either[InvalidMessageError, Message] = {
    val message = for {
      fields <- fieldsGetAll(definition).right
      fields <- fieldsCheckDuplicates(fields).right
      fields <- jsonKeysCheckDuplicates(fields).right
    } yield Message(definition.name, fields)

    message.fold(
      error => Left(InvalidMessageError(definition.name, error)),
      validMessage => Right(validMessage)
    )
  }

  /**
    * Gets all fields from the provided message definition
    * @param definition - Parsed message definition
    * @return Either an error if any of the field definitions were invalid, or the list
    *         of fields corresponding to the definition
    */
  def fieldsGetAll(definition: MessageDefinition): Either[MessageDefinitionError, Seq[Field]] = {
    val fields =  definition.fields.map(field => FieldDefinitionAnalyzer(field))

    val errors = fields.flatMap(field => field.left.toOption)
    val validFields = fields.flatMap(field => field.right.toOption)

    errors match {
      case Nil => Right(validFields)
      case _ => Left(FieldErrors(errors))
    }
  }

  /**
    * Check for duplicate fields
    * @param fields - List of fields contained within a message
    * @return If there are no duplicate fields, the provided fields sequence is returned unmodified. Otherwise,
    *         an error is returned if there are any duplicate fields.
    */
  def fieldsCheckDuplicates(fields: Seq[Field]): Either[MessageDefinitionError, Seq[Field]] = {
    val fieldsByName = fields.groupBy(_.name)

    val duplicateFields = fieldsByName.keys.filter(fieldName => fieldsByName(fieldName).size > 1).toSeq

    duplicateFields match {
      case Nil => Right(fields)
      case _ => Left(DuplicateFieldsError(duplicateFields))
    }
  }

  /**
    * Check for duplicate JSON keys
    * @param fields List of fields contained within a message
    * @return If there are no duplicate JSON keys, the provided fields sequence is returned
    *         unmodified. Otherwise, an error is returned with the set of duplicate fields.
    */
  def jsonKeysCheckDuplicates(fields: Seq[Field]): Either[MessageDefinitionError, Seq[Field]] = {
    val fieldsByKeys = fields.groupBy(_.jsonKey)

    val duplicateKeys = fieldsByKeys.keys.filter(jsonKey => fieldsByKeys(jsonKey).size > 1).toSeq

    duplicateKeys match {
      case Nil => Right(fields)
      case _ => Left(DuplicateJSONKeysError(duplicateKeys))
    }
  }
}
