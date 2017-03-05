package codegen.json.serialization

import codegen.Constants
import codegen.functions._
import datamodel._

object MessageJSONPrettyStringSerializer {

  private val messageParam = "obj"
  private val jsonOutputParam = "json_out"

  /**
    * Creates a function to serialize a message object to a formatted
    * JSON string. This function is non-static and exported.
    * @param message Message to serialize
    * @return Definition of function to serialize a message to a formatted
    *         JSON string
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
    * Gets the name of the function to serialize messages to a formatted
    * JSON string
    * @param messageName Name of message to serialize
    * @return Name of function to serialize messages to a formatted JSON string
    */
  def name(messageName: String): String = {
    s"${messageName}_json_serialize_pretty"
  }

  /**
    * @param message Message to serialize
    * @return Documentation of function to serialize messages to a formatted JSON string
    */
  private def documentation(message: Message): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Pretty print a ${message.name} to JSON",
      description = s"Serializes a ${message.name} to a formatted JSON string. The caller must free $jsonOutputParam."
    )
  }

  /**
    * @param message Message to serialize
    * @return Prototype of function to serialize messages to a formatted JSON string
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
    * @return Body of function to serialize messages to a formatted JSON string
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
       |    *$jsonOutputParam = cJSON_Print( json_root );
       |    success = ( NULL != *$jsonOutputParam );
       |    }
       |
       |cJSON_Delete( json_root );
       |
       |return success;""".stripMargin
  }
}