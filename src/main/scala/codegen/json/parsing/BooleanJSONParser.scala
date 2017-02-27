package codegen.json.parsing

import codegen.Constants
import codegen.functions._

/**
  * Contains the definition of a function to parse Boolean values from JSOn
  */
object BooleanJSONParser {

  private val jsonParam = "json"
  private val outputParam = "value_out"

  private def parseFunctionBody =
    s"""${Constants.defaultBooleanCType} success;
       |
       |if( cJSON_True == $jsonParam->type )
       |{
       |    *$outputParam = 1;
       |    success = 1;
       |}
       |else if( cJSON_False == $jsonParam->type )
       |{
       |    *$outputParam = 0;
       |    success = 1;
       |}
       |else
       |{
       |    success = 0;
       |}
       |
       |return success;""".stripMargin

  /**
    * Name of the function to parse Boolean values from JSON
    */
  val name: String = "boolean_json_parse"

  /**
    * Definition of the static function that takes a cJSON pointer input parameter
    * and a boolean pointer output parameter and parses the given JSON object as a
    * boolean value
    */
  val parseFunction: FunctionDefinition = FunctionDefinition(
    name = name,
    documentation = FunctionDocumentation(
      description = "Parse a JSON boolean value",
      shortSummary = "Parses the given JSON object as a boolean value. Returns 1 if the parse was successful, 0 otherwise."
    ),
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = "cJSON*", paramName = jsonParam),
        FunctionParameter(paramType = {Constants.defaultBooleanCType} + "*", paramName = outputParam)
      )
    ),
    body = parseFunctionBody
  )
}
