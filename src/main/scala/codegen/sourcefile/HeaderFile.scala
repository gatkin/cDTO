package codegen.sourcefile

import codegen.functions._
import codegen.types._

object HeaderFile {

  /**
    * Creates a string containing the contents of a header file
    * @param name Name of header file
    * @param types Types to declare in the header file
    * @param functions Functions to declare in the header file. This will only declare non-static functions
    *                  inside of a header file.
    * @return String containing the contents of the header file
    */
  def apply(name: String,
            description: String,
            includes: Seq[String],
            types: Seq[StructDefinition],
            functions: Seq[FunctionDefinition]): String = {

    // Declare the functions in alphabetical order and only declare
    // non-static functions in the header
    val orderedFunctions = functions.filterNot(_.prototype.isStatic).sortBy(_.name)

    s"""${SourceFile.prelude(name, description)}
       |${ifNotDefinedMacro(name)}
       |${SourceFile.includeStatements(includes)}
       |${SourceFile.typeDeclarations(types)}
       |${SourceFile.functionDeclarations(orderedFunctions)}
       |${endIfNotDefinedMacro(name)}
     """.stripMargin
  }

  /**
    * Creates the string containing the header macro definition to prevent
    * errors due to multiple includes of the same header file name
    * @param headerName Name of header file
    * @return String to ensure header is defined only once
    */
  private def ifNotDefinedMacro(headerName: String): String = {
    val headerMacro = headerMacroName(headerName)

    s"""#ifndef $headerMacro
       |#define $headerMacro
     """.stripMargin
  }

  /**
    * Gets the string to use in the header's macro definition. The conversion will
    * be: my_header.cdto.h -> MY_HEADER_CDTO_H
    * @param headerName Name of header file
    * @return Header macro name
    */
  private def headerMacroName(headerName: String): String = {
    headerName.replace('.', '_').toUpperCase
  }

  /**
    * Gets the string for the end of the header macro definition
    * @param headerName Name of header file
    * @return End of the header macro definition.
    */
  private def endIfNotDefinedMacro(headerName: String): String = {
    val headerMacro = headerMacroName(headerName)
    s"#endif /* #ifndef $headerMacro */"
  }
}
