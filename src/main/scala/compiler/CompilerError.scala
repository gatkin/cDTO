package compiler

/**
  * Contains types to represent errors that occur parsing and analyzing a protocol definition file
  */
sealed trait CompilerError

case class ParserError(location: Location, errorMessage: String) extends CompilerError

sealed trait SemanticError extends CompilerError
case class ObjectTypeNotDefinedError(objectName: String, messageName: String) extends SemanticError

sealed trait MessageDefinitionError extends SemanticError
case class DuplicateFieldError(fields: Seq[String], messageName: String) extends MessageDefinitionError
case class FieldErrors(errors: Seq[FieldDefinitionError], messageName: String) extends MessageDefinitionError

sealed trait FieldDefinitionError extends SemanticError
case class DuplicateAttributeError(attribute: String, field: String) extends FieldDefinitionError
case class TypeAliasNotAllowedError(underlyingType: String, field: String) extends FieldDefinitionError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}
