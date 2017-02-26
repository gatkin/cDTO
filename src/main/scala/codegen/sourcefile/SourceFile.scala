package codegen.sourcefile

import codegen.Constants
import codegen.functions._
import codegen.types._

object SourceFile {

  /**
    * Gets the string to define all function bodies
    * @param functions List of functions
    * @return String defining the bodies of all provided functions
    */
  def functionBodies(functions: Seq[FunctionDefinition]): String = {
    val bodies = functions
      .map(FunctionGenerator(_))
      .map(_.implementation)

    bodies.mkString("\n\n")
  }

  /**
    * Gets the string to declare all function prototypes.
    * @param functions List of functions to declare
    * @return String to declare the prototypes of all functions
    */
  def functionDeclarations(functions: Seq[FunctionDefinition]): String = {
    val declarations = functions
      .map(FunctionGenerator(_))
      .map(_.declaration)

    s"""/************************************************************************
       |                              PROCEDURES
       |************************************************************************/
       |
       |${declarations.mkString("\n\n")}
     """.stripMargin
  }

  /**
    * Gets the string to include all necessary header files. Only pass in the name of the
    * header files, e.g. "my_header.h" or <stdio.h>. Do not add the #include
    * @param includes - List of header files to include
    * @return String that will import all header files
    */
  def includeStatements(includes: Seq[String]): String = {
    val allIncludes = includes.map(include => "#include " + include).mkString("\n")

    s"""/************************************************************************
       |                              INCLUDES
       |************************************************************************/
       |
       |$allIncludes
     """.stripMargin
  }

  /**
    * Creates the prelude string to be placed at the top of all generated source
    * files. The prelude will include a warning message that the file is auto-generated
    * @param fileName Name of file
    * @param description Short description of the contents of the file
    * @return String containing the file prelude
    */
  def prelude(fileName: String, description: String): String = {
    s"""/************************************************************************
       |
       |THIS FILE IS AUTO-GENERATED. DO NOT EDIT DIRECTLY. ALL CHANGES WILL BE LOST.
       |
       |${Constants.indentation}$fileName - $description
       |
       |************************************************************************/
     """.stripMargin
  }

  /**
    * Gets the string to declare all user-defined struct types. This will ensure that
    * all structs are declared in the correct order so that if one struct contains
    * another struct as one of its fields, the contained struct is declared first.
    * @param types Definitions of structs to declare.
    * @return
    */
  def typeDeclarations(types: Seq[StructDefinition]): String = {
    // Ensure the structs are declared in the correct order so that structs
    // that depend on the definition of other structs are declared after
    // the structs they depend on
    val orderedDeclarations = StructDefinitionOrder(types)

    // We should not have circular dependencies in our struct definition at
    // this point.
    require(orderedDeclarations.isDefined)

    val declarations = orderedDeclarations.get.map(structDefinition => StructGenerator(structDefinition))
    s"""/************************************************************************
       |                              TYPES
       |************************************************************************/
       |
       |${declarations.mkString("\n\n")}
     """.stripMargin
  }
}
