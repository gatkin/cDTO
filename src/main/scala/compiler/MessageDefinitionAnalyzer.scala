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
  def apply(definition: MessageDefinition): Either[MessageDefinitionError, Message] = {
    for {
      fields <- fieldsGetAll(definition).right
      fields <- fieldsCheckDuplicates(fields, definition.name).right
    } yield Message(definition.name, fields)
  }

  /**
    * Gets all fields from the provided message definition
    * @param definition - Parsed message definition
    * @return Either an error if any of the field definitions were invalid, or the list
    *         of fields corresponding to the definition
    */
  def fieldsGetAll(definition: MessageDefinition): Either[MessageDefinitionError, Seq[Field]] = {
    val fields =  definition.fields.map(field => MessageFieldDefinitionAnalyzer(field))

    val errors = fields.flatMap(field => field.left.toOption)
    val validFields = fields.flatMap(field => field.right.toOption)

    errors match {
      case Nil => Right(validFields)
      case _ => Left(FieldErrors(errors, definition.name))
    }
  }

  /**
    * Check for duplicate fields
    * @param fields - List of fields contained within a message
    * @param messageName - Name of the message
    * @return If there are no duplicate fields, the provided fields sequence is returned unmodified. Otherwise,
    *         an error is returned if there are any duplicate fields.
    */
  def fieldsCheckDuplicates(fields: Seq[Field], messageName: String): Either[MessageDefinitionError, Seq[Field]] = {
    val fieldsByName = fields.groupBy(field => field.name)

    val duplicateFields = fieldsByName.keys.filter(fieldName => fieldsByName(fieldName).size > 1).toSeq

    duplicateFields match {
      case Nil => Right(fields)
      case _ => Left(DuplicateFieldError(duplicateFields, messageName))
    }
  }
}
