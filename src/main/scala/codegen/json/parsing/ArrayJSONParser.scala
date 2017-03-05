package codegen.json.parsing

import codegen.Constants
import codegen.functions._
import codegen.messagetypes.MessageStruct
import datamodel._


object ArrayJSONParser {

  private val jsonParam = "json_array"
  private val arrayOutputParam = "array_out"
  private val countOutputParam = "array_cnt_out"

  /**
    * Creates a function to parse a JSON array into a message field. The returned
    * function is a static function that takes a cJSON pointer argument.
    * @param messageName Name of the message containing the array
    * @param fieldName Name of the array field
    * @param array Array type information
    * @return Definition of function to parse a JSON array into a message field
    */
  def apply(messageName: String, fieldName: String, array: ArrayType): FunctionDefinition = {
    FunctionDefinition(
      name = name(messageName, fieldName),
      documentation = documentation,
      prototype = prototype(array.elementType),
      body = body(array.elementType)
    )
  }

  /**
    * Gets the name of the function to parse an array field of a message. The name of this
    * function should be unique among all JSON parsing functions used in a protocol.
    * @param messageName Name of the message containing the array
    * @param fieldName Name of the array field
    * @return Name of the function to parse an array for the given message field
    *         from a cJSON object
    */
  def name(messageName: String, fieldName: String): String = {
    s"${messageName}_${fieldName}_json_parse"
  }

  /**
    * Documentation for the array JSON parsing function
    */
  private val documentation: FunctionDocumentation = FunctionDocumentation(
    shortSummary = "Parse JSON array",
    description = "Parses the given JSON object as an array. Returns 1 if the parse was successful, 0 otherwise. The caller must free the parsed array"
  )

  /**
    * Generates the prototype for the static array parsing function that takes a cJSON pointer
    * as an input parameter and an array pointer and count pointer as output parameters
    * @param elementType Type of elements contained in the array
    * @return Prototype for a function to parse an array of the specified type
    */
  private def prototype(elementType: SimpleFieldType): FunctionPrototype = {
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = "cJSON*", paramName = jsonParam),

        // Create output parameters as pointers to their respective types
        FunctionParameter(paramType = MessageStruct.arrayFieldType(elementType) + "*", paramName = arrayOutputParam),
        FunctionParameter(paramType = Constants.defaultIntCType + "*", paramName = countOutputParam)
      )
    )
  }

  /**
    * Generates the body of the function to parse a JSON array of the specified element
    * type.
    * @param elementType Type of elements contained within the array
    * @return Body of function to parse a JSON array with the given types of elements.
    */
  private def body(elementType: SimpleFieldType): String = {
    val arrayTypeDeclaration = MessageStruct.arrayFieldType(elementType)
    val parseElement = elementParseFunction(elementType)

    s"""${Constants.defaultBooleanCType} success;
       |$arrayTypeDeclaration array;
       |${Constants.defaultIntCType} array_cnt;
       |${Constants.defaultIntCType} i;
       |cJSON* array_item;
       |
       |array = NULL;
       |array_cnt = 0;
       |
       |success = ( cJSON_Array == $jsonParam->type );
       |
       |// Allocate room to hold all items. Initialize the array's memory to
       |// all zeros so it is safe to free the array if an error occurs in the
       |// middle of parsing.
       |if( success )
       |    {
       |    array_cnt = cJSON_GetArraySize( $jsonParam );
       |    if( array_cnt > 0 )
       |        {
       |       array = calloc( array_cnt, sizeof( *array ) );
       |       success = ( NULL != array );
       |
       |       // Reset the array count if we failed to allocate the array
       |       array_cnt = success ? array_cnt : 0;
       |        }
       |    }
       |
       |for( i = 0; success && ( i < array_cnt ); i++ )
       |    {
       |    array_item = cJSON_GetArrayItem( $jsonParam, i );
       |    success = $parseElement( array_item, &array[i] );
       |    }
       |
       |*$arrayOutputParam = array;
       |*$countOutputParam = array_cnt;
       |
       |return success;""".stripMargin
  }

  /**
    * Gets the name of the function to parse an element of an array from JSON.
    * The returned element parse function will take as input a cJSON pointer
    * parameter
    * @param elementType Type of element contained in the array
    * @return Name of the function to parse an element of an array from JSON
    */
  private def elementParseFunction(elementType: SimpleFieldType): String = {
    elementType match {
      case AliasedType(_, underlyingType) => elementParseFunction(underlyingType)
      case ObjectType(objectName) => MessageJSONParser.objectParserName(objectName)
      case BooleanType => BooleanJSONParser.name
      case DynamicStringType => DynamicStringJSONParser.name
      case FixedStringType(_) => DynamicStringJSONParser.name // In arrays, fixed-length strings are dynamically-allocated
      case NumberType => NumberJSONParser.name
    }
  }
}
