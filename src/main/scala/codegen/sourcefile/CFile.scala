package codegen.sourcefile

import codegen.functions._

object CFile {

  /**
    * Creates the contents of a C source file
    * @param name Name of the C source file
    * @param description Short description of the contents of the C source file
    * @param includes List of header file names to include, e.g. "my_header.h", <string.h>
    * @param functions List of functions to define in the C source file. All static functions
    *                  will be both declared and defined in the file. Non-static functions are
    *                  expected to be declared in a separate header file
    * @return String containing the contents of a C source file
    */
  def apply(name: String,
            description: String,
            includes: Seq[String],
            functions: Seq[FunctionDefinition]): String = {

    // Declare and define the functions in alphabetical order
    val orderedFunctions = functions.sortBy(_.name)

    val (staticFunctions, nonStaticFunctions) = orderedFunctions.partition(_.prototype.isStatic)

    s"""${SourceFile.prelude(name, description)}
       |${SourceFile.includeStatements(includes)}
       |${SourceFile.functionDeclarations(staticFunctions)}
       |
       |${SourceFile.functionBodies(nonStaticFunctions)}
       |
       |${SourceFile.functionBodies(staticFunctions)}
     """.stripMargin

  }
}
