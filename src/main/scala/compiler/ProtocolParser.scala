package compiler

import scala.util.parsing.combinator.RegexParsers


object ProtocolParser extends RegexParsers {

  /**
    * Parses a cDTO protocol definition
    * @param input - String containing a cDTO protocol definition
    * @return An error if parsing failed or the AST obtained from the parsed protocol definition
    */
  def apply(input: String): Either[CompilerError,ProtocolAST] = {
    parse(protocol, input) match {
      case NoSuccess(msg, next) => Left(ParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(ast, _) => Right(ast)
    }
  }

  private def protocol: Parser[ProtocolAST] = {
    phrase(rep(message)) ^^ { case messages => ProtocolAST(messages) }
  }

  private def message: Parser[Message] = {
    identifier ~ openBrace ~ rep1(field) ~ closeBrace ^^ {
      case Identifier(name) ~ _ ~ fields ~ _ => Message(name, fields)
    }
  }

  private def field: Parser[Field] = {
    identifier ~ fieldType ~ rep(fieldAttribute) ~ lineEnd ^^ {
      case Identifier(name) ~ fType ~ attributes ~ _ => Field(name, fType, attributes)
    }
  }

  /*
  * Parsers for field attributes
  */
  private def fieldAttribute: Parser[FieldAttribute] = {
    cTypeAttribute | jsonKeyAttribute
  }

  private def cTypeAttribute: Parser[CTypeAttribute] = {
    "cType" ~ equals ~ identifier ^^ { case _ ~ _ ~ Identifier(cType) => CTypeAttribute(cType)}
  }

  private def jsonKeyAttribute: Parser[JSONKeyAttribute] = {
    "jsonKey" ~ equals ~ identifier ^^ { case _ ~ _ ~ Identifier(key) => JSONKeyAttribute(key) }
  }

  /*
  * Parsers and recognizers for field types
  */
  private def fieldType: Parser[FieldType] = {
    numberType | booleanType | fixedStringType | dynamicStringType | arrayType | objectType
  }

  private def arrayType: Parser[ArrayType] = {
    "Array" ~ arrayOpen ~ fieldType ~ arrayClose ^^ {
      case _ ~ _ ~ elementType ~ _ => ArrayType(elementType)
    }
  }

  private def booleanType: Parser[BooleanType] = {
    "Boolean" ^^ { _ => BooleanType() }
  }

  private def dynamicStringType: Parser[DynamicStringType] = {
    stringType ^^ { _ => DynamicStringType() }
  }

  private def fixedStringType: Parser[FixedStringType] = {
    stringType ~ arrayOpen ~ integerLiteral ~ arrayClose ^^ {
      case  _ ~ _ ~ IntegerLiteral(maxLength) ~ _ => FixedStringType(maxLength)
    }
  }

  private def numberType: Parser[NumberType] = {
    "Number" ^^ { _ => NumberType() }
  }

  private def objectType: Parser[ObjectType] = {
    identifier ^^ { case Identifier(objectName) => ObjectType(objectName) }
  }

  /*
  * Recognizers for tokens and terminals
  */
  private def arrayClose: Parser[ArrayClose] = {
    "]" ^^ { _ => ArrayClose() }
  }

  private def arrayOpen: Parser[ArrayOpen] = {
    "[" ^^ { _ => ArrayOpen() }
  }

  private def closeBrace: Parser[CloseBrace] = {
    "}" ^^ { _ => CloseBrace() }
  }

  private def equals: Parser[Equals] = {
    "=" ^^ { _ => Equals() }
  }

  private def identifier: Parser[Identifier] = {
    """[_a-zA-Z][_a-zA-Z0-9]*""".r ^^ { id => Identifier(id) }
  }

  private def integerLiteral: Parser[IntegerLiteral] = {
    """[1-9][0-9]*""".r ^^ { value => IntegerLiteral(value.toInt) }
  }

  private def lineEnd: Parser[LineEnd] = {
    ";" ^^ { _ => LineEnd() }
  }

  private def openBrace: Parser[OpenBrace] = {
    "{" ^^ { _ => OpenBrace() }
  }

  private def stringType: Parser[StringType] = {
    "String" ^^ {_ =>  StringType() }
  }
}
