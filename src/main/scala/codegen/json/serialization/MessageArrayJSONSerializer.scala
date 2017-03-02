package codegen.json.serialization

import codegen.Constants
import codegen.functions._

object MessageArrayJSONSerializer {

  private val arrayParam = "obj_array"
  private val countParam = "obj_array_cnt"
  private val jsonOutputParam = "json_out"

  /** Generates the definition of the function to serialize an array of
    * messages to a cJSON array object. The returned function is static
    * and is intended to be only used internally to serialize messages.
    * @param messageName Name of the message
    * @return Definition of function to serialize an array of messages
    */
  def apply(messageName: String): FunctionDefinition = {
    FunctionDefinition(
      name = name(messageName),
      documentation = documentation(messageName),
      prototype = prototype(messageName),
      body = body(messageName)
    )
  }

  /**
    * Gets the name of the function to parse arrays of the given type
    * @param messageName Name of the message
    * @return Name of the function to parse arrays of the given message type
    */
  def name(messageName: String): String = {
    s"${messageName}_array_json_serialize"
  }

  /**
    * @param messageName Name of message
    * @return Documentation for function to serialize arrays of messages
    */
  private def documentation(messageName: String): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Serialize an array of $messageName objects",
      description = s"Serializes an array of $messageName objects to a cJSON array object. The caller must clean up $jsonOutputParam."
    )
  }

  /**
    * @param messageName Name of message
    * @return Prototype for function to serialize arrays of message objects
    */
  private def prototype(messageName: String): FunctionPrototype = {
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = messageName + " const*", paramName = arrayParam),
        FunctionParameter(paramType = "int", paramName = countParam),
        FunctionParameter(paramType = "cJSON**", paramName = jsonOutputParam)
      )
    )
  }

  /**
    * @param messageName Name of message
    * @return Body of function to serialize arrays of message objects
    */
  private def body(messageName: String): String = {
    val objectSerializer = MessageJSONObjectSerializer.name(messageName)

    s"""${Constants.defaultBooleanCType} success;
       |cJSON* json_array;
       |cJSON* array_item;
       |int i;
       |
       |*$jsonOutputParam = NULL;
       |
       |json_array = cJSON_CreateArray();
       |success = ( NULL != json_array );
       |
       |for( i = 0; ( success ) && ( i < $countParam ); i++ )
       |{
       |    success = $objectSerializer( &$arrayParam[i], &array_item );
       |
       |    if( success )
       |    {
       |        cJSON_AddItemToArray( json_array, array_item );
       |    }
       |}
       |
       |// Set the output or clean up on error
       |if( success )
       |{
       |    *$jsonOutputParam = json_array;
       |}
       |else
       |{
       |    cJSON_Delete( json_array );
       |}
       |
       |return success;""".stripMargin
  }
}
