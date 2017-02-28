package codegen.json.parsing

import codegen.Constants
import codegen.functions._
import codegen.messagetypes._
import datamodel._

object MessageJSONParser {

  private val jsonStringParam = "json_str"
  private val jsonObjectParam = "json_obj"
  private val messageOutputParam = "obj_out"
  private val successVar = "success"
  private val jsonObjectItemVar = "json_item"

  /**
    * Returns a two functions to parse a message object from JSON. The first returned
    * function takes as input a JSON string parameter from which it parses the message
    * object. This function is intended to be part of the external API and hence is
    * non-static. The second returned function is a static function that takes as input
    * a cJSON pointer parameter from which it parses a message object. This function is
    * intended only to be used internally. The public string parsing object creates a
    * cJSON object from the string input and delegates the actual parsing to the internal
    * object parsing function.
    * @param message Message to parse from JSON
    * @return Functions to parse the given message from JSON
    */
  def apply(message: Message): Seq[FunctionDefinition] = {
    List(
      stringParser(message),
      objectParser(message)
    )
  }

  /**
    * Gets the list of functions needed to parse the provided message's
    * array fields from JSON. These are static functions defined separately
    * from the message's parsing function.
    * @param message cDTO message
    * @return List of functions to parse
    */
  def arrayFieldParseFunctions(message: Message): Seq[FunctionDefinition] = {
    message.fields.flatMap(field => arrayFieldParseFunction(message.name, field))
  }

  /**
    * Gets the name of the public API function to parse a message object from a
    * JSON input string.
    * @param messageName Name of the message to parse
    * @return Function to parse a message object from a JSON string
    */
  def stringParserName(messageName: String): String = {
    s"${messageName}_json_parse"
  }

  /**
    * Gets the name of the internal static function to parse a message object from a
    * cJSON object
    * @param messageName Name of the message to parse
    * @return Name of the function to parse messages from a cJSON object
    */
  def objectParserName(messageName: String): String = {
    s"${messageName}_json_obj_parse"
  }

  /**
    * Returns the definition for the function that parses an input string into
    * objects of the given message type
    * @param message Message to parse
    * @return Function to parse the message from JSON strings
    */
  private def stringParser(message: Message): FunctionDefinition = {
    FunctionDefinition(
      name = stringParserName(message.name),
      documentation = stringParserDocumentation(message),
      prototype = stringParserPrototype(message),
      body = stringParserBody(message)
    )
  }

  /**
    * @param message Message to parse
    * @return Documentation of the function to parse a message from a JSON string
    */
  private def stringParserDocumentation(message: Message): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Parse a ${message.name}",
      description =
        s"Parses the provided JSON string into a ${message.name}. The caller must call ${MessageFreeFunction.name(message.name)} on $messageOutputParam."
    )
  }

  /**
    * @param message Message to parse
    * @return Prototype of the function to parse a message from a JSON string
    */
  private def stringParserPrototype(message: Message): FunctionPrototype = {
    FunctionPrototype(
      isStatic = false,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = "char const*", paramName = jsonStringParam),
        FunctionParameter(paramType = message.name + "*", paramName = messageOutputParam)
      )
    )
  }

  /**
    * @param message Message to parse
    * @return Body of the function to parse a message from a JSON string
    */
  private def stringParserBody(message: Message): String = {
    val jsonRootVar = "json_root"
    val parseJsonObject = s"${objectParserName(message.name)}( $jsonRootVar, $messageOutputParam );"

    s"""${Constants.defaultBooleanCType} $successVar;
       |cJSON* $jsonRootVar;
       |
       |${initializeMessageOutput(message.name, messageOutputParam)}
       |
       |$jsonRootVar = cJSON_Parse( $jsonStringParam );
       |$successVar = ( NULL != $jsonRootVar );
       |
       |if( $successVar )
       |{
       |    $successVar = $parseJsonObject
       |}
       |
       |// Reset the output on error
       |if( !$successVar )
       |{
       |    ${freeMessageOutput(message.name, messageOutputParam)}
       |}
       |
       |cJSON_Delete( $jsonRootVar );
       |
       |return $successVar;""".stripMargin
  }

  /**
    * Gets the string to initialize an output parameter of the given message type. The output
    * parameter should be a pointer to the message type.
    * @param messageName Name of the message
    * @param messageOutputParam Name of the message output parameter
    * @return String containing the function call to initialize a pointer to a message object
    */
  private def initializeMessageOutput(messageName: String, messageOutputParam: String): String = {
    s"${MessageInitFunction.name(messageName)}( $messageOutputParam );"
  }

  /**
    * Gets the string to free an output parameter of the given message type. The output
    * parameter should be a pointer to the message type.
    * @param messageName Name of the message
    * @param messageOutputParam Name of the message output parameter
    * @return String containing the function call to free a pointer to a message object
    */
  private def freeMessageOutput(messageName: String, messageOutputParam: String): String = {
    s"${MessageFreeFunction.name(messageName)}( $messageOutputParam );"
  }

  /**
    * Gets the internal static function to parse message objects from cJSON objects
    * @param message Message to parse
    * @return Definition of function to parse messages from cJSON objects
    */
  private def objectParser(message: Message): FunctionDefinition = {
    FunctionDefinition(
      name = objectParserName(message.name),
      documentation = objectParserDocumentation(message),
      prototype = objectParserPrototype(message),
      body = objectParserBody(message)
    )
  }

  /**
    * @param message Message to parse
    * @return Documentation of the function to parse messages from cJSON objects
    */
  private def objectParserDocumentation(message: Message): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Parse ${message.name} JSON object",
      description = s"Parses the given JSON object as a ${message.name}."
    )
  }

  /**
    * @param message Message to parse
    * @return Prototype of the function to parse messages from cJSON objects
    */
  private def objectParserPrototype(message: Message): FunctionPrototype = {
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = "cJSON*", paramName = jsonObjectParam),
        FunctionParameter(paramType = message.name + "*", messageOutputParam)
      )
    )
  }

  /**
    * @param message Message to parse
    * @return Body of the function to parse messages from cJSON objects
    */
  private def objectParserBody(message: Message): String = {
    val parseFieldSnippets = message.fields.map(field => parseFieldSnippet(message.name, field)).mkString("\n\n")

    s"""${Constants.defaultBooleanCType} $successVar;
       |cJSON* $jsonObjectItemVar;
       |
       |$successVar = 1;
       |${initializeMessageOutput(message.name, messageOutputParam)}
       |
       |$parseFieldSnippets
       |
       |// Reset the output on error
       |if( !$successVar )
       |{
       |    ${freeMessageOutput(message.name, messageOutputParam)}
       |}
       |
       |return $successVar;
     """.stripMargin
  }

  /**
    * Gets the snippet of code necessary to parse the provided field of the specified
    * message.
    * @param messageName Name of the message containing the field to parse
    * @param field Field to parse
    * @return Code snippet to parse the message field
    */
  private def parseFieldSnippet(messageName: String, field: Field): String = {
    val parseFunctionCall = fieldParseCall(messageName, field.name, field.fieldType)
    s"""if( $successVar )
       |{
       |    $jsonObjectItemVar = cJSON_GetObjectItem( $jsonObjectParam, "${field.jsonKey}" );
       |    $successVar = ( NULL != json_item ) && ( $parseFunctionCall );
       |}""".stripMargin
  }

  /**
    * Gets the function call to parse the given field of the specified message
    * @param messageName Name of the message containing the field
    * @param fieldName Name of the field to be parsed
    * @param fieldType Type of the field to be parsed
    * @return Function call to parse the field
    */
  private def fieldParseCall(messageName: String, fieldName: String, fieldType: FieldType): String = {
    fieldType match {
      case ArrayType(_) =>  arrayFieldParseCall(messageName, fieldName)
      case AliasedType(_, underlyingType) => fieldParseCall(messageName, fieldName, underlyingType)
      case ObjectType(objectName) => defaultFieldParseCall(fieldName, objectParserName(objectName))
      case BooleanType => defaultFieldParseCall(fieldName, BooleanJSONParser.name)
      case DynamicStringType => defaultFieldParseCall(fieldName, DynamicStringJSONParser.name)
      case FixedStringType(_) => fixedStringFieldParseCall(fieldName)
      case NumberType => defaultFieldParseCall(fieldName, NumberJSONParser.name)
    }
  }

  /**
    * Gets the function call to parse an array field of a message
    * @param messageName Name of the message containing the array field
    * @param arrayFieldName Name of the array field within the message
    * @return Function call to parse the array field from JSON
    */
  private def arrayFieldParseCall(messageName: String, arrayFieldName: String): String = {
    val parseFunction = ArrayJSONParser.name(messageName, arrayFieldName)
    val countFieldName = MessageStruct.arrayCountFieldName(arrayFieldName)

    s"$parseFunction( $jsonObjectItemVar, &$messageOutputParam->$arrayFieldName, &$messageOutputParam->$countFieldName )"
  }

  /**
    * Gets the default function call to parse a message field. This is for field types
    * that do not require additional parameters in their parse functions
    * @param fieldName Name of the field to parse
    * @param parseFunctionName Name of the function to parse field's type
    * @return Function call to parse the field
    */
  private def defaultFieldParseCall(fieldName: String, parseFunctionName: String): String = {
    s"$parseFunctionName( $jsonObjectItemVar, &$messageOutputParam->$fieldName )"
  }

  /**
    * Gets the function call to parse a fixed-length string field
    * @param fieldName Name of the field to parse
    * @return Function call to parse the fixed-length string field
    */
  private def fixedStringFieldParseCall(fieldName: String): String = {
    val parseFunction = FixedStringJSONParser.name
    s"$parseFunction( $jsonObjectItemVar, $messageOutputParam->$fieldName, sizeof( $messageOutputParam->$fieldName ) )"
  }

  /**
    * If the provided field is an array, this gets the definition of the function
    * to parse the provided array field of a message
    * @param messageName Name of the message containing the array field
    * @param field Field to parse
    * @return None if the field is not an array, the definition of the function to
    *         parse the array field otherwise
    */
  private def arrayFieldParseFunction(messageName: String, field: Field): Option[FunctionDefinition] = {
    field.fieldType match {
      case array @ ArrayType(_) => Some(ArrayJSONParser(messageName, field.name, array))
      case _ => None
    }
  }
}
