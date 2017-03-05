package codegen.json.parsing

import codegen.Constants
import codegen.functions._

/**
  * Defines a function to parse fixed-length string values
  * from JSON
  */
object FixedStringJSONParser {

  private val jsonParam = "json"
  private val outputParam = "value_out"
  private val maxLengthParam = "max_length"

  private def parseFunctionBody =
    s"""${Constants.defaultBooleanCType} success;
       |int chars_printed;
       |
       |success = ( cJSON_String == $jsonParam->type );
       |
       |if( success )
       |    {
       |    chars_printed = snprintf( $outputParam, $maxLengthParam, "%s", $jsonParam->valuestring );
       |    success = ( chars_printed < $maxLengthParam );
       |    }
       |
       |return success;""".stripMargin

  /**
    * Name of the function to parse fixed-length string values from JSON
    */
  val name: String = "fixed_string_json_parse"

  /**
    * Definition of the static function to parse fixed-length values from JSON. The
    * function takes a cJSON pointer input parameter, a string buffer output parameter
    * that must be pre-allocated by the caller, and a parameter indicating the size of
    * the string output buffer. This will fail to parse if the JSON string value exceeds
    * the maximum length.
    */
  val parseFunction: FunctionDefinition = FunctionDefinition(
    name = name,
    documentation = FunctionDocumentation(
      description = "Parse fixed-length JSON string",
      shortSummary = s"Parses the given JSON object as a fixed-length string. Returns 1 if the parse was successful, 0 otherwise"
    ),
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = "cJSON*", paramName = jsonParam),
        FunctionParameter(paramType = {Constants.defaultCharacterCType} + "*", paramName = outputParam),
        FunctionParameter(paramType = Constants.defaultIntCType, paramName = maxLengthParam)
      )
    ),
    body = parseFunctionBody
  )
}
