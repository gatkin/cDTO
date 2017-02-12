package compiler

/**
  * Contains types to represent errors that occur parsing and analyzing a protocol definition file
  */
sealed trait CompilerError

case class ParserError(location: Location, errorMessage: String) extends CompilerError

sealed trait SemanticError extends CompilerError
case class DuplicateFieldError(field: String, messageName: String) extends SemanticError
case class ObjectTypeNotDefinedError(objectName: String, messageName: String) extends SemanticError
case class FieldErrors(errors: Seq[MessageFieldError], messageName: String) extends SemanticError

sealed trait MessageFieldError extends SemanticError
case class DuplicateAttributeError(attribute: String, field: String) extends MessageFieldError
case class TypeAliasNotAllowedError(underlyingType: String, field: String) extends MessageFieldError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}
