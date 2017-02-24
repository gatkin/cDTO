package codegen.types

import codegen.functions._
import datamodel._


object MessageInitFunction {

  private val parameterName = "obj"

  /**
    * Creates a definition for a function that can be
    * used to initialize a message struct. The initialization
    * function will simply zero out the message struct
    * @param message cDTO message
    * @return Definition for the message's initialization function
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
    * Gets the name of the function used to initialize a message struct
    * @param messageName Name of cDTO message
    * @return Name of the message's initialization function
    */
  def name(messageName: String): String = {
    s"${messageName}_init"
  }

  /**
    * Gets the documentation for the message's initialization function
    * @param message cDTO message
    * @return Documentation for the initialization function
    */
  private def documentation(message: Message): FunctionDocumentation = {
    FunctionDocumentation(
      shortSummary = s"Initialize a ${message.name} object",
      description = s"Zeros out the provided ${message.name}."
    )
  }

  /**
    * Gets the body of the message's initialization function
    * @param message cDTO message
    * @return String containing the initialization function's body
    */
  private def body(message: Message): String = {
    s"memset( $parameterName, 0, sizeof( *$parameterName ) );"
  }

  /**
    * Gets the prototype for the message's initialization function.
    * Message initialization functions are never static and are
    * declared in the same header that defines the message struct.
    * @param message cDTO message
    * @return Prototype of the initialization function
    */
  private def prototype(message: Message): FunctionPrototype = {
    FunctionPrototype(
      isStatic = false,
      returnType = "void",
      parameters = List(
        FunctionParameter(paramType = s"${message.name}*", paramName = parameterName)
      )
    )
  }
}
