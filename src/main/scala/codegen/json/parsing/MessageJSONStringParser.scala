package codegen.json.parsing

import codegen.Constants
import codegen.functions._
import codegen.messagetypes._
import datamodel._


object MessageJSONStringParser {

  private val jsonStringParam = "json_str"
  private val messageOutputParam = "obj_out"

  /**
    * Returns the definition for the function that parses an input string into
    * objects of the given message type. These parse functions are intended to
    * be exposed as part of the public API.
    * @param message Message to parse
    * @return Function to parse the message from JSON strings
    */
  def apply(message: Message): FunctionDefinition = {
    FunctionDefinition(
      name = name(message.name),
      documentation = documentation(message),
      prototype = prototype(message),
      body = body(message)
    )
  }

  /**
    * Gets the name of the public API function to parse a message object from a
    * JSON input string.
    * @param messageName Name of the message to parse
    * @return Function to parse a message object from a JSON string
    */
  def name(messageName: String): String = {
    s"${messageName}_json_parse"
  }

  /**
    * @param message Message to parse
    * @return Documentation of the function to parse a message from a JSON string
    */
  private def documentation(message: Message): FunctionDocumentation = {
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
  private def prototype(message: Message): FunctionPrototype = {
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
  private def body(message: Message): String = {
    val jsonRootVar = "json_root"
    val initializeOutput = s"${MessageInitFunction.name(message.name)}( $messageOutputParam );"
    val freeOutput = s"${MessageFreeFunction.name(message.name)}( $messageOutputParam );"

    // Delegate to the object parser to perform the actual parsing work
    val parseJSONObject = s"${MessageJSONObjectParser.name(message.name)}( $jsonRootVar, $messageOutputParam );"

    s"""${Constants.defaultBooleanCType} success;
       |cJSON* $jsonRootVar;
       |
       |$initializeOutput
       |
       |$jsonRootVar = cJSON_Parse( $jsonStringParam );
       |success = ( NULL != $jsonRootVar );
       |
       |if( success )
       |    {
       |    success = $parseJSONObject
       |    }
       |
       |// Reset the output on error
       |if( !success )
       |    {
       |    $freeOutput
       |    }
       |
       |cJSON_Delete( $jsonRootVar );
       |
       |return success;""".stripMargin
  }
}
