package codegen.json

import codegen.Constants
import codegen.functions._
import codegen.json.parsing._
import codegen.messagetypes._
import codegen.sourcefile._
import datamodel._

object MessageJSONFiles {

  /**
    * Gets the header and C source file definitions that contain functions
    * to parse and serialize messages to and from JSON
    * @param protocol Message protocol
    * @return Files containing functions to parse and serialize all messages
    *         in the protocol to and from JSON.
    */
  def apply(protocol: Protocol): SourceFilePair = {
    val messageParseFunctions = protocol.messages.flatMap(MessageJSONParser(_))
    val arrayFieldParseFunctions = protocol.messages.flatMap(MessageJSONParser.arrayFieldParseFunctions)
    val allParseFunctions = messageParseFunctions ++ arrayFieldParseFunctions ++ baseTypeParseFunctions(protocol)

    SourceFilePair(
      headerFile = headerFile(protocol.name, allParseFunctions),
      cFile = cFile(protocol.name, allParseFunctions)
    )
  }

  /**
    * Gets the name of the C source file containing the definitions of functions
    * to parse and serialize messages to and from JSON
    * @param protocolName Name of the protocol
    * @return Name of C source file containing JSON parsing/serialization function definitions
    */
  def cFileName(protocolName: String): String = {
    s"$protocolName.json.c"
  }

  /**
    * Gets the include string to include the protocol's JSON parsing/serialization
    * header
    * @param protocolName Name of the protocol
    * @return Name of the protocol's header file surrounded by quotes to be used
    *         in include statements
    */
  def headerFileInclude(protocolName: String): String = {
    s""""${headerFileName(protocolName)}""""
  }

  /**
    * Gets the name of the header file containing the declarations of functions
    * to parse and serialize messages to and from JSON
    * @param protocolName Name of the protocol
    * @return Name of protocol's JSON parsing/serialization header
    */
  def headerFileName(protocolName: String): String = {
    s"$protocolName.json.h"
  }

  /**
    * Gets the list of all base type parsing functions that are necessary for parsing
    * protocol messages. For instance if no messages contain fixed-length string fields,
    * then this will not include the definition for the function to parse fixed-length fields.
    * If at least one message contains a numeric field, then this will include the definition
    * for the function to parse numeric values from JSON
    * @param protocol Protocol
    * @return List of static functions to parse base types contained in the protocol
    */
  private def baseTypeParseFunctions(protocol: Protocol): Seq[FunctionDefinition] = {
    val fieldTypes = protocol.messages.flatMap(message => message.fields.map(_.fieldType)).toSet

    fieldTypes.flatMap(baseTypeParseFunction).toSeq
  }

  /**
    * Gets the function for parsing the provided type if it is a base field type
    * @param fieldType Type of field to parse
    * @return None if the provided type is not a base field type, the definition
    *         of the function to parse the type otherwise
    */
  private def baseTypeParseFunction(fieldType: FieldType): Option[FunctionDefinition] = {
    fieldType match {
      case ArrayType(_) => None
      case AliasedType(_, underlyingType) => baseTypeParseFunction(underlyingType)
      case ObjectType(_) => None
      case BooleanType => Some(BooleanJSONParser.parseFunction)
      case DynamicStringType => Some(DynamicStringJSONParser.parseFunction)
      case FixedStringType(_) => Some(FixedStringJSONParser.parseFunction)
      case NumberType => Some(NumberJSONParser.parseFunction)
    }
  }

  /**
    * Gets the definition of the protocol's JSON parsing/serialization header file
    * @param protocolName Name of the protocol
    * @param parseFunctions List of functions to to declare.
    * @return Definition for the protocol's JSON parsing/serialization header file
    */
  private def headerFile(protocolName: String, parseFunctions: Seq[FunctionDefinition]): FileDefinition = {
    val name = headerFileName(protocolName)

    val contents = HeaderFile(
      name = name,
      description = "Declares functions for parsing and serializing messages to and from JSON",
      includes = List(MessageTypeFiles.headerFileInclude(protocolName)),
      types = Nil,
      functions = parseFunctions
    )

    FileDefinition(name, contents)
  }

  /**
    * Gets the definition for the C source file containing the JSON parsing/serialization
    * functions for the protocol
    * @param protocolName Name of the protocol
    * @param parseFunctions List of all function definitions to include in the C source file
    * @return Definition for the protocol's JSON parsing/serialization C source file
    */
  private def cFile(protocolName: String, parseFunctions: Seq[FunctionDefinition]): FileDefinition = {
    val name = cFileName(protocolName)
    val includes = List(Constants.stdlibHeader, Constants.stringHeader, Constants.cJSONHeader, headerFileInclude(protocolName))

    val contents = CFile(
      name = name,
      description = "Declares functions for parsing and serializing messages to and from JSON",
      includes = includes,
      functions = parseFunctions
    )

    FileDefinition(name, contents)
  }
}
