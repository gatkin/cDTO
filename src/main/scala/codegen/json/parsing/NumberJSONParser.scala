package codegen.json.parsing

import codegen.Constants
import codegen.functions._

/**
  * Creates a function to parse numeric JSON values
  */
object NumberJSONParser {

  private val jsonParamName = "json"
  private val outputParamName = "value_out"

  private def parseFunctionBody =
    s"""${Constants.defaultBooleanCType} success;
       |
       |if( cJSON_Number == $jsonParamName->type )
       |    {
       |    *$outputParamName = $jsonParamName->valuedouble;
       |    success = 1;
       |    }
       |else
       |    {
       |    success = 0;
       |    }
       |
       |return success;""".stripMargin

  /**
    * Name of the function to parse numeric JSON values
    */
  val name: String = "number_json_parse"

  /**
    * Definition of the static function to parse numeric values from JSON.
    */
  val parseFunction: FunctionDefinition = FunctionDefinition(
    name = name,
    documentation = FunctionDocumentation(
      description = "Parse JSON number",
      shortSummary = "Parses the given JSON object as a number. Returns 1 if the parse was successful, 0 otherwise."
    ),
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = "cJSON*", paramName = jsonParamName),
        FunctionParameter(paramType = {Constants.defaultNumberCType} + "*", paramName = outputParamName)
      )
    ),
    body = parseFunctionBody
  )
}
