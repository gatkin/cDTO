package compiler

/**
  * Contains types to represent errors that occur parsing and analyzing a protocol definition file
  */
sealed trait CompilerError

case class ParserError(location: Location, errorMessage: String) extends CompilerError

sealed trait SemanticError extends CompilerError
case class DuplicateMessagesError(duplicateMessages: Seq[String]) extends SemanticError
case class InvalidMessagesError(errors: Seq[MessageDefinitionError]) extends SemanticError
case class ObjectTypesNotDefinedError(objectNames: Seq[String]) extends SemanticError

sealed trait MessageDefinitionError extends SemanticError
case class DuplicateFieldsError(fields: Seq[String], messageName: String) extends MessageDefinitionError
case class InvalidFieldsError(errors: Seq[FieldDefinitionError], messageName: String) extends MessageDefinitionError

sealed trait FieldDefinitionError extends SemanticError
case class DuplicateAttributeError(attribute: String, field: String) extends FieldDefinitionError
case class TypeAliasNotAllowedError(underlyingType: String, field: String) extends FieldDefinitionError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}
