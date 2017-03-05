package compiler

/**
  * Contains types to represent errors that occur parsing and analyzing a protocol definition file
  */
sealed trait CompilerError

/**
  * Possible semantic errors in protocol definitions
  */
  sealed trait SemanticError extends CompilerError
case class DuplicateMessagesError(duplicateMessages: Seq[String]) extends SemanticError
case class MessageErrors(errors: Seq[InvalidMessageError]) extends SemanticError
case class ObjectTypesNotDefinedError(objectNames: Seq[String]) extends SemanticError

/**
  * Decorates a message definition error with the name of the message to provide
  * more context about the error in error messages
  */
case class InvalidMessageError(messageName: String, error: MessageDefinitionError)

/**
  * Possible semantic errors in a message definition
  */
sealed trait MessageDefinitionError extends SemanticError
case class DuplicateFieldsError(fields: Seq[String]) extends MessageDefinitionError
case class DuplicateJSONKeysError(jsonKeys: Seq[String]) extends MessageDefinitionError
case class FieldErrors(errors: Seq[InvalidFieldError]) extends MessageDefinitionError

/**
  * Decorates a field definition error with the name of the field to provide more
  * context about the error in error messages
  */
case class InvalidFieldError(fieldName: String, error: FieldDefinitionError)

/**
  * Possible semantic errors in a field definition
  */
sealed trait FieldDefinitionError extends SemanticError
case class DuplicateAttributeError(attribute: String) extends FieldDefinitionError
case class TypeAliasNotAllowedError(underlyingType: String) extends FieldDefinitionError

/**
  * Contains information about a syntax error encountered by the parser
  */
case class ParserError(location: Location, errorMessage: String) extends CompilerError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}
