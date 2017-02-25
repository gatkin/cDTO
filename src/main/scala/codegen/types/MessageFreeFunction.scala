package codegen.types

import codegen.Constants
import codegen.functions._
import datamodel._

object MessageFreeFunction {

  private val paramName = "obj"

  /**
    * Creates the definition of the function to free all memory owned
    * by a cDTO message struct
    * @param message cDTO message
    * @return Definition of the message free function
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
    * Gets the name of the function to free a message struct. All message
    * free functions take pointer parameters
    * @param messageName Name of message
    * @return Name of the message struct's free function
    */
  def name(messageName: String): String = {
    s"${messageName}_free"
  }

  /**
    * Gets a list of definitions for functions to free any array fields contained
    * in the message.
    * @param message cDTO message
    * @return List of definitions for array field free functions
    */
  def arrayFieldFreeFunctions(message: Message): Seq[FunctionDefinition] = {
    message.fields.flatMap(field => arrayFieldFreeFunctionDef(message.name, field))
  }

  /**
    * Gets the documentation for a message free function
    * @param message cDTO message
    * @return Message free function documentation
    */
  private def documentation(message: Message): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Free ${message.name}",
      description = s"Cleans up all resources owned by the provided ${message.name}."
    )
  }

  /**
    * Gets the prototype for the message free function
    * @param message cDTO message
    * @return Message free function prototype
    */
  private def prototype(message: Message): FunctionPrototype = {
    FunctionPrototype(
      isStatic = false,
      returnType = Constants.voidCType,
      parameters = List(
        FunctionParameter(paramType = s"${StructDefinition.structName(message)}*", paramName = paramName)
      )
    )
  }

  /**
    * Gets the body of a message free function
    * @param message cDTO message
    * @return String containing the body of a message free function
    */
  private def body(message: Message): String = {
    val fieldFreeCalls = for {
      field <- message.fields
      freeCall <- fieldFreeFunctionCall(message.name, field.name, field.fieldType)
    } yield freeCall

    val allFieldFreeCalls = fieldFreeCalls.mkString("\n")

    // After freeing all of the message's fields, we want to call the message's
    // init function so its memory is zeroed out. This will make it safe to
    // call the free function multiple times on the same object and ensure none
    // of the fields contain dangling pointers
    val initCall = s"${MessageInitFunction.name(message.name)}( $paramName );"

    // Add a line blank between the field free calls and the message initialization
    // call for better readability
    val spacing = if(fieldFreeCalls.isEmpty) "" else "\n\n"

    s"$allFieldFreeCalls$spacing$initCall"
  }

  /**
    * If a message field contains dynamically-allocated memory, then this will return
    * the string necessary to free all dynamically-allocated memory owned by the field.
    * If the field does not own any dynamically-allocated memory, then this will return None
    * @param messageName Name of message containing the field
    * @param fieldName Name of field
    * @param fieldType Type of field
    * @return String to free the field if it owns dynamically-allocated memory, None otherwise
    */
  private def fieldFreeFunctionCall(messageName: String, fieldName: String, fieldType: FieldType): Option[String] = {
    fieldType match {
      case ArrayType(_) => Some(arrayFreeFunctionCall(messageName, fieldName))
      case AliasedType(_, underlyingType) => fieldFreeFunctionCall(messageName, fieldName, underlyingType)
      case ObjectType(objectName) => Some(objectFreeFunctionName(objectName, fieldName))
      case BooleanType => None
      case DynamicStringType => Some(dynamicStringFreeFunctionCall(fieldName))
      case FixedStringType(_) => None
      case NumberType => None
    }
  }

  /**
    * Gets the string to free an array field
    * @param messageName Name of message containing the array
    * @param arrayFieldName Name of the array field
    * @return String to free the array field
    */
  private def arrayFreeFunctionCall(messageName: String, arrayFieldName: String): String = {
    val functionName = ArrayFieldFreeFunction.name(messageName, arrayFieldName)
    val countField = StructDefinition.arrayCountFieldName(arrayFieldName)

    s"$functionName( $paramName->$arrayFieldName, $paramName->$countField );"
  }

  /**
    * Gets the string to free a message object field that is contained within
    * another message object
    * @param objectTypeName Name of the message object type
    * @param objectFieldName Name the object field
    * @return String to free the object field
    */
  private def objectFreeFunctionName(objectTypeName: String, objectFieldName: String): String = {
    val functionName = MessageFreeFunction.name(objectTypeName)

    // Always pass object fields as pointers to their free functions
    s"$functionName( &$paramName->$objectFieldName );"
  }

  /**
    * Gets the string to free a field containing a dynamically-allocated string
    * @param stringFieldName Name of the field containing the string
    * @return String to free the string field
    */
  private def dynamicStringFreeFunctionCall(stringFieldName: String): String = {
    s"${Constants.defaultFreeFunction}( $paramName->$stringFieldName );"
  }

  /**
    * If the provided field is an array, this returns the definition for the
    * function to free the array, None otherwise
    * @param messageName Name of message containing the field
    * @param field Field of th Message
    * @return Either an array free function if the field is an array or None otherwise
    */
   private def arrayFieldFreeFunctionDef(messageName: String, field: Field): Option[FunctionDefinition] = {
    field.fieldType match {
      case array @ ArrayType(_) => Some(ArrayFieldFreeFunction(messageName, field.name, array))
      case _ => None
    }
  }
}
