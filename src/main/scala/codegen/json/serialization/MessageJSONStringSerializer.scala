package codegen.json.serialization

import codegen.Constants
import codegen.functions._
import datamodel._

object MessageJSONStringSerializer {

  private val messageParam = "obj"
  private val jsonOutputParam = "json_out"

  /**
    * Generates a function to serialize messages to unformatted
    * JSON strings. This function is non-static as intended to
    * be exported
    * @param message Message to serialize
    * @return Definition of function to serialize a message to an unformatted JSON string
    */
  def apply(message: Message): FunctionDefinition = {
    FunctionDefinition(
      name = name(message.name),
      documentation(message),
      prototype(message),
      body(message)
    )
  }

  /**
    * Gets the name of the function to serialize messages to unformatted
    * JSON strings.
    * @param messageName Name of message to serialize
    * @return Name of function to serialize a message to an unformatted JSON string
    */
  def name(messageName: String): String = {
    s"${messageName}_json_serialize"
  }

  /**
    * @param message Message to serialize
    * @return Documentation of function to serialize a message to an unformatted JSON string
    */
  private def documentation(message: Message): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Serialize a ${message.name} to JSON",
      description = s"Serializes a ${message.name} to an unformatted JSON string. The caller must free $jsonOutputParam."
    )
  }

  /**
    * @param message Message to serialize
    * @return Prototype of function to serialize a message to an unformatted JSON string
    */
  private def prototype(message: Message): FunctionPrototype = {
    FunctionPrototype(
      isStatic = false,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = message.name + " const*", paramName = messageParam),
        FunctionParameter(paramType = Constants.defaultCharacterCType + "**", paramName = jsonOutputParam)
      )
    )
  }

  /**
    * @param message Message to serialize
    * @return body of function to serialize a message to an unformatted JSON string
    */
  private def body(message: Message): String = {
    val objectSerializer = MessageJSONObjectSerializer.name(message.name)

    s"""${Constants.defaultBooleanCType} success;
       |cJSON* json_root;
       |
       |json_root = NULL;
       |*$jsonOutputParam = NULL;
       |
       |success = $objectSerializer( $messageParam, &json_root );
       |
       |if( success )
       |    {
       |    *$jsonOutputParam = cJSON_PrintUnformatted( json_root );
       |    success = ( NULL != *$jsonOutputParam );
       |    }
       |
       |cJSON_Delete( json_root );
       |
       |return success;""".stripMargin
  }
}
