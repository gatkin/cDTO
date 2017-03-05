package codegen.functions

import codegen.Constants

object FunctionGenerator {

  /**
    * Generates the strings necessary to declare and define a C function
    * based on the given definition
    * @param definition Definition of function to generate
    * @return CFunction object containing the strings to declare and define the function
    */
  def apply(definition: FunctionDefinition): CFunction = {
    CFunction(
      declaration = functionDeclaration(definition),
      implementation = functionImplementation(definition)
    )
  }

  /**
    * Generates the declaration string for the function
    * @param definition Definition of the function
    * @return C declaration string for the function
    */
  private def functionDeclaration(definition: FunctionDefinition): String = {
    // The header is just the signature with an additional semicolon
    s"${functionSignature(definition)};"
  }

  /**
    * Generates the function signature string, e.g.
    *
    *     static void say_hello
    *         (
    *         char const * name,
    *         char const * message
    *         )
    *
    * @param definition Definition of the function
    * @return C function signature string
    */
  private def functionSignature(definition: FunctionDefinition): String = {
    val parameters = functionParameters(definition.prototype.parameters)
    val staticModifier = if(definition.prototype.isStatic) "static " else ""

    s"""$staticModifier${definition.prototype.returnType} ${definition.name}
       |    (
       |$parameters
       |    )""".stripMargin
  }

  /**
    * Generates a formatted string containing all of a function's parameters. This
    * string will consist of simply "void" if the parameters list is empty or a
    * comma-separated list of function parameters otherwise
    * @param parameters List of function parameters
    * @return String containing the C function's parameter list
    */
  private def functionParameters(parameters: Seq[FunctionParameter]): String = {
    parameters match {
      case Nil => s"    ${Constants.voidCType}"
      case _ => functionParameterList(parameters)
    }
  }

  /**
    * Generates a string containing a comma-separated list of function parameters
    * @param parameters Non-empty list of function parameters
    * @return String containing the C function's parameter list
    */
  private def functionParameterList(parameters: Seq[FunctionParameter]): String = {
    require(parameters.nonEmpty)

    val parameterDeclarations = for {
      parameter <- parameters
    } yield s"    ${parameter.paramType} ${parameter.paramName}"

    parameterDeclarations.mkString(",\n")
  }

  /**
    * Generates a function implementation string containing a documentation header,
    * the function signature, and the function body
    * @param definition Definition of the function
    * @return String containing the implementation of the C function
    */
  private def functionImplementation(definition: FunctionDefinition): String = {
    s"""${functionDocumentationHeader(definition)}
       |${functionSignature(definition)}
       |{
       |${definition.body.trim}
       |}    /* ${definition.name}()    */""".stripMargin
  }

  /**
    * Generates a function documentation header containing documentation
    * for a C function
    * @param definition Definition of the function
    * @return String containing the function's documentation header
    */
  private def functionDocumentationHeader(definition: FunctionDefinition): String = {
    s"""/**************************************************
       |*
       |*    ${definition.name} - ${definition.documentation.shortSummary}
       |*
       |*    ${definition.documentation.description}
       |*
       |**************************************************/""".stripMargin
  }
}
