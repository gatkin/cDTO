package codegen.json.parsing

import codegen.Constants
import codegen.functions._

/**
  * Defines a function to parse dynamically-allocated string values
  * from JSON
  */
object DynamicStringJSONParser {

  private val jsonParam = "json"
  private val outputParam = "value_out"

  private def parseFunctionBody =
    s"""${Constants.defaultBooleanCType} success;
       |
       |*$outputParam = NULL;
       |
       |success = ( cJSON_String == $jsonParam->type );
       |
       |if( success )
       |    {
       |    *$outputParam = strdup( $jsonParam->valuestring );
       |    success = ( NULL != *$outputParam );
       |    }
       |
       |return success;""".stripMargin

  /**
    * Name of the function to parse dynamically-allocated string values from JSON.
    */
  val name: String = "dynamic_string_json_parse"

  /**
    * Definition of the static function to parse dynamically-allocated string values from JSON.
    * The function takes a cJSON pointer input parameter and a char pointer output parameter.
    */
  val parseFunction: FunctionDefinition = FunctionDefinition(
    name = name,
    documentation = FunctionDocumentation(
      description = "Parse dynamic JSON string",
      shortSummary = s"Parses the given JSON object as a dynamic string. Returns 1 if the parse was successful, 0 otherwise. The caller must free $outputParam."
    ),
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = "cJSON*", paramName = jsonParam),
        FunctionParameter(paramType = {Constants.defaultCharacterCType} + "**", paramName = outputParam)
      )
    ),
    body = parseFunctionBody
  )
}
