package codegen.messagetypes

import codegen.Constants
import codegen.functions._
import datamodel._


object ArrayFieldFreeFunction {

  private val nameSuffix = "_array_free"
  private val arrayParamName = "array"
  private val countParamName = "array_cnt"
  private val indexVariableName = "i"

  /**
    * Gets the definition for the static function to free arrays containing the specified
    * types of elements
    * @param elementType Type of elements contained in the array
    * @return Function definition of the array's free function
    */
  def apply(elementType: SimpleFieldType): FunctionDefinition = {
    FunctionDefinition(
      name = name(elementType),
      documentation = documentation,
      prototype = prototype(elementType),
      body = body(elementType)
    )
  }

  /**
    * Gets the name of the function to free arrays containing the specified type of
    * elements
    * @param elementType Type of elements contained in the array
    * @return Name of the array's free function
    */
  def name(elementType: SimpleFieldType): String = {
    elementType match {
      case AliasedType(_, underlyingType) => name(underlyingType)
      case ObjectType(objectName) => objectName + nameSuffix
      case BooleanType => "boolean" + nameSuffix
      case DynamicStringType => "string" + nameSuffix
      case FixedStringType(_) => "string" + nameSuffix
      case NumberType => "number" + nameSuffix
    }
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
        FunctionParameter(paramType = MessageStruct.arrayFieldType(elementType), paramName = arrayParamName),
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
      |    {
      |    $elementFreeCall
      |    }
      |
      |${Constants.defaultFreeFunction}( $arrayParamName );""".stripMargin
  }
}
