package codegen.types

import codegen.Constants
import codegen.functions._
import datamodel._


object ArrayFieldFreeFunction {

  private val arrayParamName = "array"
  private val countParamName = "array_cnt"
  private val indexVariableName = "i"

  /**
    * Gets the definition for the function necessary to free an array that is a field
    * of a message struct. The array free function will be a static function.
    * @param messageName Name of message containing the array
    * @param fieldName Name of the array field within the struct
    * @param array Array field of the struct
    * @return Function definition of the array's free function
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
    * Gets the name of the function used to free an array that is a field within
    * a message struct. The function is named so that it does not conflict with
    * any other functions,
    * @param messageName Name of the message which contains the array
    * @param fieldName Name of the array field within the struct
    * @return Name of the array's free function
    */
  def name(messageName: String, fieldName: String): String = {
    s"${messageName}_${fieldName}_free"
  }

  /**
    * Gets the documentation for an array free function
    * @return Array free function documentation
    */
  private def documentation: FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = "Free array",
      description = "Cleans up all resources owned by the array and its elements."
    )
  }

  /**
    * Gets the prototype for an array free function with the given element
    * type
    * @param elementType Type of elements contained within the array
    * @return Array free function prototype
    */
  private def prototype(elementType: SimpleFieldType): FunctionPrototype = {
    FunctionPrototype(
      isStatic = true,
      returnType = Constants.voidCType,
      parameters = List(
        FunctionParameter(paramType = StructDefinition.arrayFieldType(elementType), paramName = arrayParamName),
        FunctionParameter(paramType = Constants.defaultIntCType, paramName = countParamName)
      )
    )
  }

  /**
    * Gets the body of the array free function
    * @param elementType Type of elements contained within the array
    * @return String containing the body of the array free function
    */
  private def body(elementType: SimpleFieldType): String = {
    elementFreeFunctionCall(elementType) match {
      case None => nonDynamicElementsFreeBody
      case Some(freeFunction) => dynamicElementsFreeBody(freeFunction)
    }
  }

  /**
    * Gets the string containing the function call that will free an
    * array's elements if the array contains dynamically-allocated
    * elements. If the array's elements are non-dynamic and do not
    * need to be freed, then this will return None
    * @param elementType Type of elements contained within the array
    * @return None if the array's elements are nt dynamically allocated,
    *         a string to free the element otherwise
    */
  private def elementFreeFunctionCall(elementType: SimpleFieldType): Option[String] = {
    elementType match {
      case AliasedType(_, underlyingType) => elementFreeFunctionCall(underlyingType)
      case ObjectType(objectName) => Some(objectElementFreeCall(objectName))
      case BooleanType => None
      case DynamicStringType => Some(stringElementFreeCall)
      case FixedStringType(_) => Some(stringElementFreeCall)
      case NumberType => None
    }
  }

  /**
    * Gets the function call to free an element of a string array
    * @return String to free a string element of an array
    */
  private def stringElementFreeCall: String = {
    s"${Constants.defaultFreeFunction}( $arrayParamName[$indexVariableName] );"
  }

  /**
    * Gets the function call to free an element of an array of message
    * objects
    * @param objectName Name of the cDTO message object
    * @return String to free a message object that is an element of an array
    */
  private def objectElementFreeCall(objectName: String): String = {
    val functionName = MessageFreeFunction.name(objectName)

    // Always pass object elements as pointers to their free functions
    s"$functionName( &$arrayParamName[$indexVariableName] );"
  }

  /**
    * Gets the body of a function to free an array of non-dynamic elements
    * that do not need to be freed themselves. In this case only the array
    * itself needs to be freed.
    * @return Body of array free function with non-dynamic elements
    */
  private def nonDynamicElementsFreeBody: String = {
    s"${Constants.defaultFreeFunction}( $arrayParamName );"
  }

  /**
    * Gets the body of an array free function for arrays of dynamically-allocated
    * elements where each element needs to be freed individually
    * @param elementFreeCall String to free an element of the array
    * @return String containing body of function to free an array of dynamic elements
    */
  private def dynamicElementsFreeBody(elementFreeCall: String): String = {
    // Need to free each item in the array before freeing the array itself
    s"""${Constants.defaultIntCType} $indexVariableName;
      |
      |for( $indexVariableName = 0; $indexVariableName < $countParamName; $indexVariableName++ )
      |{
      |${Constants.indentation}$elementFreeCall
      |}
      |
      |${Constants.defaultFreeFunction}( $arrayParamName );""".stripMargin
  }
}
