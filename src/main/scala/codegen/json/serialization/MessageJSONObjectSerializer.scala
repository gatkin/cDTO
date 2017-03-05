package codegen.json.serialization

import codegen.Constants
import codegen.functions._
import codegen.messagetypes._
import datamodel._

object MessageJSONObjectSerializer {

  private val messageParam = "obj"
  private val jsonOutputParam = "json_out"
  private val successVar = "success"
  private val jsonRootVar = "json_root"
  private val jsonItemVar = "json_item"

  /**
    * Generates a function to serialize a message to a cJSON object.
    * This function is static and is intended to be used internally
    * by other functions to serialize messages contained as fields
    * within other messages and by the functions to serialize a
    * message to a JSON string.
    * @param message Message to serialize
    * @return Definition of the function to serialize a message to a cJSON object
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
    * Gets the name of the function that serializes a message to
    * a cJSON object
    * @param messageName Name of message to serialize
    * @return Name of function to serialize message to a cJSON object
    */
  def name(messageName: String): String = {
    s"${messageName}_json_obj_serialize"
  }

  /**
    * @param message Message to serialize
    * @return Documentation for the function to serialize a message
    */
  private def documentation(message: Message): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Serialize a ${message.name} to JSON",
      description = s"Serializes the provided ${message.name} to a cJSON object. The caller must clean up $jsonOutputParam."
    )
  }

  /**
    * @param message Message to serialize
    * @return Gets the prototype for the static function to serialize a message object
    *         to a cJSON object
    */
  private def prototype(message: Message): FunctionPrototype = {
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.defaultBooleanCType,
      parameters = List(
        FunctionParameter(paramType = message.name + " const*", paramName = messageParam),
        FunctionParameter(paramType = "cJSON**", paramName = jsonOutputParam)
      )
    )
  }

  /**
    * @param message Message to serialize
    * @return Body of the function to serialize message to a cJSON object
    */
  private def body(message: Message): String = {
    val fieldSnippets = message.fields.map(field => fieldSerializeSnippet(field.fieldType, field.name, field.jsonKey))
    val allFieldSnippets = fieldSnippets.mkString("\n\n")

    s"""${Constants.defaultBooleanCType} $successVar;
       |cJSON* $jsonRootVar;
       |cJSON* $jsonItemVar;
       |
       |*$jsonOutputParam = NULL;
       |
       |$jsonRootVar = cJSON_CreateObject();
       |$successVar = ( NULL != $jsonRootVar );
       |
       |$allFieldSnippets
       |
       |// Set the output or clean up on error
       |if( $successVar )
       |    {
       |   *$jsonOutputParam = $jsonRootVar;
       |    }
       |else
       |    {
       |    cJSON_Delete( $jsonRootVar );
       |    }
       |
       |return $successVar;""".stripMargin
  }

  /**
    * Generates the code snippet to serialize the specified object field to a
    * cJSON object and add it to the message's root JSON object
    * @param fieldType Type of field
    * @param fieldName Name of field
    * @param jsonKey Field's JSON key
    * @return Code snippet to serialize the specified field
    */
  private def fieldSerializeSnippet(fieldType: FieldType, fieldName: String, jsonKey: String): String = {
    fieldType match {
      case ArrayType(elementType) => arraySerializeSnippet(elementType, fieldName, jsonKey)
      case AliasedType(_, underlyingType) =>fieldSerializeSnippet(underlyingType, fieldName, jsonKey)
      case ObjectType(objectName) => objectSerializeSnippet(objectName, fieldName, jsonKey)
      case baseFieldType:BaseFieldType => baseTypeFieldSerializeSnippet(baseFieldType, fieldName, jsonKey)
    }
  }

  /**
    * Gets the code snippet to serialize an array of the specified element type as
    * a field of a message.
    * @param elementType Type of elements contained in the array
    * @param fieldName Name of the array field
    * @param jsonKey JSON key of the array field
    * @return Code snippet to serialize the specified array field
    */
  private def arraySerializeSnippet(elementType: SimpleFieldType, fieldName: String, jsonKey: String): String = {
    elementType match {
      case AliasedType(_, underlyingType) => arraySerializeSnippet(underlyingType, fieldName, jsonKey)
      case ObjectType(objectName) => objectArraySerializeSnippet(objectName, fieldName, jsonKey)
      case BooleanType => booleanArraySerializeSnippet(fieldName, jsonKey)
      case DynamicStringType => stringArraySerializeSnippet(fieldName, jsonKey)
      case FixedStringType(_) => stringArraySerializeSnippet(fieldName, jsonKey)
      case NumberType => numberArraySerializeSnippet(fieldName, jsonKey)
    }
  }

  /**
    * Gets the code snippet to serialize an array of objects
    * @param objectName Name of object type contained in the array
    * @param fieldName Name of the object array field
    * @param jsonKey JSON key of the object array field
    * @return Code snippet to serialize the specified object array field
    */
  private def objectArraySerializeSnippet(objectName: String, fieldName: String, jsonKey: String): String = {
    val serializeFunction = MessageArrayJSONSerializer.name(objectName)
    val countField = MessageStruct.arrayCountFieldName(fieldName)

    s"""if( $successVar )
       |    {
       |   $successVar = $serializeFunction( $messageParam->$fieldName, $messageParam->$countField, &$jsonItemVar );
       |    }
       |
       |${addToJSONRootSnippet(jsonKey)}""".stripMargin
  }

  /**
    * Gets the code snippet to serialize an array of boolean values
    * @param fieldName Name of the boolean array field
    * @param jsonKey JSON key of the boolean array field
    * @return Code snippet to serialize the specified boolean array field
    */
  private def booleanArraySerializeSnippet(fieldName: String, jsonKey: String): String = {
    val serializeFunction = BooleanArrayJSONSerializer.name
    val countField = MessageStruct.arrayCountFieldName(fieldName)

    s"""if( $successVar )
       |    {
       |    $successVar = $serializeFunction( $messageParam->$fieldName, $messageParam->$countField, &$jsonItemVar );
       |    }
       |
       |${addToJSONRootSnippet(jsonKey)}""".stripMargin
  }

  /**
    * Gets the code snippet to serialize an array of strings
    * @param fieldName Name of the string array field
    * @param jsonKey JSON key of the string array field
    * @return Code snippet to serialize the specified string array field
    */
  private def stringArraySerializeSnippet(fieldName: String, jsonKey: String): String = {
    val countField = MessageStruct.arrayCountFieldName(fieldName)

    s"""if( $successVar )
       |    {
       |    $jsonItemVar = cJSON_CreateStringArray( $messageParam->$fieldName, $messageParam->$countField );
       |    $successVar = ( NULL != $jsonItemVar );
       |    }
       |
       |${addToJSONRootSnippet(jsonKey)}""".stripMargin
  }

  /**
    * Gets the code snippet to serialize an array of numbers
    * @param fieldName Name of string array field
    * @param jsonKey JSON key of string array field
    * @return Code snippet to serialize an array of strings into the specified field
    */
  private def numberArraySerializeSnippet(fieldName: String, jsonKey: String): String = {
    val countField = MessageStruct.arrayCountFieldName(fieldName)

    s"""if( $successVar )
       |    {
       |    $jsonItemVar = cJSON_CreateDoubleArray( $messageParam->$fieldName, $messageParam->$countField );
       |    $successVar = ( NULL != $jsonItemVar );
       |    }
       |
       |${addToJSONRootSnippet(jsonKey)}""".stripMargin
  }

  /**
    * Gets the code snippet to serialize an object field
    * @param objectName Name of the object type
    * @param fieldName Name of the object field
    * @param jsonKey JSON key of the object field
    * @return Code snippet to serialize the sepcified object field
    */
  private def objectSerializeSnippet(objectName: String, fieldName: String, jsonKey: String): String = {
    val serializeFunction = MessageJSONObjectSerializer.name(objectName)

    s"""if( $successVar )
       |    {
       |    $successVar = $serializeFunction( &$messageParam->$fieldName, &$jsonItemVar );
       |    }
       |
       |${addToJSONRootSnippet(jsonKey)}""".stripMargin
  }

  /**
    * Gets the code snippet to serialize a base-type field
    * @param baseFieldType Type of field
    * @param fieldName Name of field
    * @param jsonKey Field's JSON key
    * @return Code snippet to serialize the specified field
    */
  private def baseTypeFieldSerializeSnippet(baseFieldType: BaseFieldType, fieldName: String, jsonKey: String): String = {
    val serializeFunction = baseFieldType match {
      case BooleanType => "cJSON_CreateBool"
      case DynamicStringType => "cJSON_CreateString"
      case FixedStringType(_) => "cJSON_CreateString"
      case NumberType => "cJSON_CreateNumber"
    }

    s"""if( $successVar )
       |    {
       |    $jsonItemVar = $serializeFunction( $messageParam->$fieldName );
       |    $successVar = ( NULL != $jsonItemVar );
       |    }
       |
       |${addToJSONRootSnippet(jsonKey)}""".stripMargin
  }

  /**
    * Gets the code snippet to add a serialized JSON object to the
    * root JSON object using the specified JSON key
    * @param jsonKey JSON key under which to add the item to the root
    * @return Code snippet to add the JSON item to the JSON root object
    */
  private def addToJSONRootSnippet(jsonKey: String): String = {
    s"""if( $successVar )
       |    {
       |    cJSON_AddItemToObject( $jsonRootVar, "$jsonKey", $jsonItemVar );
       |    }""".stripMargin
  }
}
