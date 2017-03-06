package codegen.json

import codegen.Constants
import codegen.functions._
import codegen.json.parsing._
import codegen.json.serialization._
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
    val functions = protocolJSONFunctions(protocol)

    SourceFilePair(
      headerFile = headerFile(protocol.name, functions),
      cFile = cFile(protocol.name, functions)
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
    * Gets the list of all functions necessary to transform protocol messages to and
    * from JSON
    * @param protocol Protocol
    * @return List of all functions needed to transform protocol messages to and from
    *         JSON
    */
  private def protocolJSONFunctions(protocol: Protocol): Seq[FunctionDefinition] = {
    protocolParseFunctions(protocol) ++ protocolSerializeFunctions(protocol)
  }

  /**
    * Gets the list of all functions necessary to parse protocol
    * @param protocol Protocol
    * @return List of all functions to parse protocol messages from JSOn
    */
  private def protocolParseFunctions(protocol: Protocol): Seq[FunctionDefinition] = {
    val messageParseFunctions = protocol.messages.flatMap(message => List(
      MessageJSONStringParser(message),
      MessageJSONObjectParser(message)
    ))
    val baseTypeParseFunctions = protocolFieldTypes(protocol).flatMap(baseTypeParseFunction)

    messageParseFunctions ++ baseTypeParseFunctions ++ arrayParseFunctions(protocol)
  }

  /**
    * Gets the list of functions to parse all array types used in the protocol
    * @param protocol Message protocol
    * @return List of functions to parse all array types used in the protocol
    */
  private def arrayParseFunctions(protocol: Protocol): Seq[FunctionDefinition] = {
    val arrayFields = protocolFieldTypes(protocol).collect({ case array @ ArrayType(_) => array })

    arrayFields.map(array => ArrayJSONParser(array.elementType)).toSeq
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

  /** Gets the list of all functions necessary to serialize protocol messages
    * to JSON
    * @param protocol Protocol
    * @return List of all functions to serialize protocol messages to JSON
    */
  private def protocolSerializeFunctions(protocol: Protocol): Seq[FunctionDefinition] = {
    val messageSerializeFunctions = protocol.messages.flatMap(message => List(
      MessageJSONObjectSerializer(message),
      MessageJSONStringSerializer(message),
      MessageJSONPrettyStringSerializer(message)
    ))

    val arraySerializeFunctions = messageArraySerializeFunctions(protocol) ++ booleanArraySerializeFunction(protocol)

    messageSerializeFunctions ++ arraySerializeFunctions
  }

  /**
    * If any messages contain fields of arrays of other messages, then this
    * returns the functions necessary to serialize any arrays of messages
    * used in the protocol
    * @param protocol Message protocol
    * @return List of functions necessary to serialize any arrays of messages
    *         that are defined in the protocol
    */
  private def messageArraySerializeFunctions(protocol: Protocol): Seq[FunctionDefinition] = {
    val objectArrayFields = protocolFieldTypes(protocol).collect({ case ArrayType(ObjectType(objectName)) => objectName})

    objectArrayFields.map(MessageArrayJSONSerializer(_)).toSeq
  }

  /**
    * If any messages have a field of an array of boolean values, then this gets
    * the function to serialize an array of boolean values to JSON. If no messages
    * contain such a field, then None is returned
    * @param protocol Message protocol
    * @return Definition of function to serialize an array of boolean values if any
    *         messages contain such a field, None otherwise
    */
  private def booleanArraySerializeFunction(protocol: Protocol): Option[FunctionDefinition] = {
    val booleanArrayFields = protocolFieldTypes(protocol).collect({ case ArrayType(BooleanType) => BooleanType })

    if(booleanArrayFields.isEmpty) None else Some(BooleanArrayJSONSerializer.definition)
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
      description = "Contains functions for parsing and serializing messages to and from JSON",
      includes = includes,
      functions = parseFunctions
    )

    FileDefinition(name, contents)
  }

  /**
    * Gets the set of all field types used in the protocol messages
    * @param protocol Message protocol
    * @return Set of field types used in the protocol messages
    */
  private def protocolFieldTypes(protocol: Protocol): Set[FieldType] = {
    val fieldTypes = for {
      message <- protocol.messages
      field <- message.fields
    } yield field.fieldType

    fieldTypes.toSet
  }
}
