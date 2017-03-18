package codegen.json.parsing

import codegen.Constants
import codegen.functions._
import codegen.messagetypes._
import datamodel._


object MessageJSONObjectParser {

  private val jsonObjectParam = "json_obj"
  private val messageOutputParam = "obj_out"
  private val successVar = "success"
  private val jsonObjectItemVar = "json_item"

  /**
    * Creates a static function that takes as input a pointer to a cJSON object
    * and parses that into a message object. The object parse function for a message
    * is intended to be used internally and not exposed in the API.
    * @param message Message to parse
    * @return Definition of function to parse messages from cJSON objects
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
    * Gets the name of the internal static function to parse a message object from a
    * cJSON object
    * @param messageName Name of the message to parse
    * @return Name of the function to parse messages from a cJSON object
    */
  def name(messageName: String): String = {
    s"${messageName}_json_obj_parse"
  }

  /**
    * @param message Message to parse
    * @return Documentation of the function to parse messages from cJSON objects
    */
  private def documentation(message: Message): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Parse ${message.name} JSON object",
      description = s"Parses the given JSON object as a ${message.name}."
    )
  }

  /**
    * @param message Message to parse
    * @return Prototype of the function to parse messages from cJSON objects
    */
  private def prototype(message: Message): FunctionPrototype = {
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = "cJSON*", paramName = jsonObjectParam),
        FunctionParameter(paramType = message.name + "*", messageOutputParam)
      )
    )
  }

  /**
    * @param message Message to parse
    * @return Body of the function to parse messages from cJSON objects
    */
  private def body(message: Message): String = {
    val initializeOutput = s"${MessageInitFunction.name(message.name)}( $messageOutputParam );"
    val freeOutput = s"${MessageFreeFunction.name(message.name)}( $messageOutputParam );"
    val parseFieldSnippets = message.fields.map(parseFieldSnippet).mkString("\n\n")

    s"""${Constants.defaultBooleanCType} $successVar;
       |cJSON* $jsonObjectItemVar;
       |
       |$successVar = 1;
       |$initializeOutput
       |
       |$parseFieldSnippets
       |
       |// Reset the output on error
       |if( !$successVar )
       |    {
       |    $freeOutput
       |    }
       |
       |return $successVar;
     """.stripMargin
  }

  /**
    * Gets the snippet of code necessary to parse the provided field of the specified
    * message.
    * @param field Field to parse
    * @return Code snippet to parse the message field
    */
  private def parseFieldSnippet(field: Field): String = {
    val parseFunctionCall = fieldParseCall(field.name, field.fieldType)
    s"""if( $successVar )
       |    {
       |    $jsonObjectItemVar = cJSON_GetObjectItem( $jsonObjectParam, "${field.jsonKey}" );
       |    $successVar = ( NULL != json_item ) && ( $parseFunctionCall );
       |    }""".stripMargin
  }

  /**
    * Gets the function call to parse the given field of the specified message
    * @param fieldName Name of the field to be parsed
    * @param fieldType Type of the field to be parsed
    * @return Function call to parse the field
    */
  private def fieldParseCall(fieldName: String, fieldType: FieldType): String = {
    fieldType match {
      case ArrayType(elementType) =>  arrayFieldParseCall(fieldName, elementType)
      case AliasedType(_, underlyingType) => aliasedFieldParseCall(fieldName, underlyingType)
      case ObjectType(objectName) => defaultFieldParseCall(fieldName, MessageJSONObjectParser.name(objectName))
      case BooleanType => defaultFieldParseCall(fieldName, BooleanJSONParser.name)
      case DynamicStringType => defaultFieldParseCall(fieldName, DynamicStringJSONParser.name)
      case FixedStringType(_) => fixedStringFieldParseCall(fieldName)
      case NumberType => defaultFieldParseCall(fieldName, NumberJSONParser.name)
    }
  }

  /**
    * Gets the function call to parse an array field of a message
    * @param arrayFieldName Name of the array field within the message
    * @param elementType Type of elements contained in the array
    * @return Function call to parse the array field from JSON
    */
  private def arrayFieldParseCall(arrayFieldName: String, elementType: SimpleFieldType): String = {
    val parseFunction = ArrayJSONParser.name(elementType)
    val countFieldName = MessageStruct.arrayCountFieldName(arrayFieldName)

    s"$parseFunction( $jsonObjectItemVar, &$messageOutputParam->$arrayFieldName, &$messageOutputParam->$countFieldName )"
  }

  /**
    * Gets the function call to parse an aliased message field
    * @param fieldName Name of aliased-type field
    * @param underlyingType Underlying field type
    * @return Function call to parse the aliased field
    */
  private def aliasedFieldParseCall(fieldName: String, underlyingType: BaseFieldType): String = {
    underlyingType match {
      case BooleanType => defaultAliasedFieldParseCall(fieldName, BooleanJSONParser.name, Constants.defaultBooleanCType)
      case DynamicStringType => defaultAliasedFieldParseCall(fieldName, DynamicStringJSONParser.name, Constants.defaultCharacterCType)
      case FixedStringType(_) => aliasedFixedStringParseCall(fieldName)
      case NumberType => defaultAliasedFieldParseCall(fieldName, NumberJSONParser.name, Constants.defaultNumberCType)
    }
  }

  /**
    * Gets the default function call to parse an aliased-type field. This is for
    * parsing underlying types that do not require additional parameters to their
    * parse functions
    * @param fieldName Name of field
    * @param parseFunctionName Name of function to parse the field's underlying type
    * @param underlyingType Field's underlying C-type
    * @return Function call to parse the aliased field
    */
  private def defaultAliasedFieldParseCall(fieldName: String, parseFunctionName: String, underlyingType: String): String = {
    // Need to cast the parameter to the type expected by the parse function
    s"$parseFunctionName( $jsonObjectItemVar, ($underlyingType*)&$messageOutputParam->$fieldName )"
  }

  /**
    * Gets the function call to parse an aliased fixed-string field
    * @param fieldName Name of aliased field
    * @return Function call to parse an aliased fixed-string field
    */
  private def aliasedFixedStringParseCall(fieldName: String): String = {
    val parseFunction = FixedStringJSONParser.name
    val cast = s"(${Constants.defaultCharacterCType}*)"
    s"$parseFunction( $jsonObjectItemVar, $cast$messageOutputParam->$fieldName, sizeof( $messageOutputParam->$fieldName ) )"
  }
  
  /**
    * Gets the default function call to parse a message field. This is for field types
    * that do not require additional parameters in their parse functions
    * @param fieldName Name of the field to parse
    * @param parseFunctionName Name of the function to parse field's type
    * @return Function call to parse the field
    */
  private def defaultFieldParseCall(fieldName: String, parseFunctionName: String): String = {
    s"$parseFunctionName( $jsonObjectItemVar, &$messageOutputParam->$fieldName )"
  }

  /**
    * Gets the function call to parse a fixed-length string field
    * @param fieldName Name of the field to parse
    * @return Function call to parse the fixed-length string field
    */
  private def fixedStringFieldParseCall(fieldName: String): String = {
    val parseFunction = FixedStringJSONParser.name
    s"$parseFunction( $jsonObjectItemVar, $messageOutputParam->$fieldName, sizeof( $messageOutputParam->$fieldName ) )"
  }
}
