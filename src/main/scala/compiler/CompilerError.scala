package compiler

/**
  * Contains types to represent errors that occur parsing and analyzing a protocol definition file
  */
sealed trait CompilerError

case class ParserError(location: Location, msg: String) extends CompilerError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}
