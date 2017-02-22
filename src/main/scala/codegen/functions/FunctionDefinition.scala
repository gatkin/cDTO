package codegen.functions

/**
  * Contains strings to declare and define a C function
  * @param declaration Declaration of the function's prototype. Suitable for either a header file
  *                    for non-static functions or for the top of a .c file for static functions
  * @param implementation Implementation of the function. Includes a documentation header, the function's
  *                       signature and the function's body.
  */
case class CFunction(declaration: String, implementation: String)

/**
  * Contains the information necessary to define a C function
  * @param name Name of the function
  * @param documentation Documentation for the function
  * @param prototype Function's prototype
  * @param body Body of the function. This should contain all statements for the function separated by semicolons and
  *             newlines. Do NOT include the surrounding braces in this string.
  */
case class FunctionDefinition(name: String, documentation: FunctionDocumentation, prototype: FunctionPrototype, body: String)

/**
  * Contains documentation for a C function.
  * @param shortSummary Short, one sentence summary of the function
  * @param description More detailed explanation of the function
  */
case class FunctionDocumentation(shortSummary: String, description: String)

/**
  * Contains the information necessary to define the function's prototype
  * @param isStatic Is the function a static function?
  * @param returnType Type returned by the function
  * @param parameters List of parameters taken by the function
  */
case class FunctionPrototype(isStatic: Boolean, returnType: String, parameters: Seq[FunctionParameter])

/**
  * Represents a C function parameter
  * @param paramType Type of the parameter, e.g. int, char const*, etc.
  * @param paramName Name of the parameter
  */
case class FunctionParameter(paramType: String, paramName: String)