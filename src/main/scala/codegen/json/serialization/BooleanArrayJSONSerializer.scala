package codegen.json.serialization

import codegen.Constants
import codegen.functions._

/**
  * Contains the definition for the serializing arrays of boolean values
  * to cJSON array objects. Other base types have convenience functions
  * e.g. cJSON_CreateStringArray() for serializing arrays of those types
  * However, there is no such function for boolean types so a special
  * static function is needed.
  */
object BooleanArrayJSONSerializer {

  private val arrayParam = "array"
  private val countParam = "array_cnt"
  private val jsonOutputParam = "json_out"

  private val documentation = FunctionDocumentation(
    shortSummary = "Serialize array of boolean values",
    description = s"Serializes an array of boolean values. The caller must clean up $jsonOutputParam"
  )

  private val prototype = FunctionPrototype(
    isStatic = true,
    returnType = Constants.defaultBooleanCType,
    parameters = List(
      FunctionParameter(paramType = Constants.defaultBooleanCType + " const*", paramName = arrayParam),
      FunctionParameter(paramType = "int", paramName = countParam),
      FunctionParameter(paramType = "cJSON**", paramName = jsonOutputParam)
    )
  )

  private val body =
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
       |    array_item = NULL;
       |
       |    if( $arrayParam[i] )
       |    {
       |        array_item = cJSON_CreateTrue();
       |    }
       |    else
       |    {
       |        array_item = cJSON_CreateFalse();
       |    }
       |
       |    success = ( NULL != array_item );
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

  val name: String = "boolean_array_json_serialize"

  val definition = FunctionDefinition(
    name = name,
    documentation = documentation,
    prototype = prototype,
    body = body
  )
}
