package codegen.messagetypes

import codegen.Constants
import codegen.functions._
import codegen.sourcefile._
import codegen.types.StructDefinition
import datamodel._

object MessageTypeFiles {

  /**
    * Creates the header file to define all structs and functions corresponding the given
    * set of protocol messages as well as the C source file containing the function
    * definitions.
    * @param protocol cDTO message protocol
    * @param aliasedTypeHeaders List of header files that contain type definitions for
    *                           any aliased types referenced in the protocol definition.
    *                           For instance, if a Number field is aliased to uint32_t,
    *                           then this list should include '<stdint.h>'
    * @return Header and C source files containing type definitions and functions for
    *         working with the protocol message objects.
    */
  def apply(protocol: Protocol, aliasedTypeHeaders: Seq[String]): SourceFilePair = {
    // Get all of the structs to define
    val structs = protocol.messages.map(message => MessageStruct(message))

    // Get all of the functions to declare and define
    val initFunctions = protocol.messages.map(message => MessageInitFunction(message))
    val freeFunctions = protocol.messages.map(message => MessageFreeFunction(message))

    val allFunctions = initFunctions ++ freeFunctions ++ arrayFreeFunctions(protocol)

    // Create the header and source files
    val header = headerFile(protocol.name, aliasedTypeHeaders, structs, allFunctions)
    val sourceFile = cFile(protocol.name, allFunctions)

    SourceFilePair(header, sourceFile)
  }

  /**
    * Gets the name of the C source file containing message free and init
    * functions
    * @param protocolName Name of the message protocol
    * @return Name of the protocol types C source file
    */
  def cFileName(protocolName: String): String = {
    s"$protocolName.c"
  }

  /**
    * Gets the name of the header file containing the message type definitions
    * and message free and init function declarations
    * @param protocolName Name of the message protocol
    * @return Name of the protocol type header file
    */
  def headerFileName(protocolName: String): String = {
    s"$protocolName.h"
  }

  /**
    * Gets the string necessary to include the header file containing the
    * protocol message struct definitions
    * @param protocolName Name of the message protocol
    * @return Include string for the message type header file
    */
  def headerFileInclude(protocolName: String): String = {
    // Include with quotes around the file name
    s""""${headerFileName(protocolName)}""""
  }

  /**
    * Gets the list of all array free functions necessary for all array types
    * used in the protocol
    * @param protocol Message protocol
    * @return List of functions to free all array types used in the protocol
    */
  private def arrayFreeFunctions(protocol: Protocol): Seq[FunctionDefinition] = {
    val fieldTypes = for {
      message <- protocol.messages
      field <- message.fields
    } yield field.fieldType

    val arrayFields = fieldTypes.collect({ case array @ ArrayType(_) => array }).toSet

    arrayFields.map(array => ArrayFieldFreeFunction(array.elementType)).toSeq
  }

  /**
    * Gets the header file for the message protocol
    * @param protocolName Name of the message protocol
    * @param aliasedTypeHeaders Set of header files that contain type definitions for all aliased types
    *                           specified in the protocol
    * @param structs List of protocol message struct definitions to declare
    * @param functions List of protocol message functions to declare
    * @return Message protocol header file
    */
  private def headerFile(protocolName: String, aliasedTypeHeaders: Seq[String], structs: Seq[StructDefinition], functions: Seq[FunctionDefinition]): FileDefinition = {
    val headerContents = HeaderFile(
      name = headerFileName(protocolName),
      description =  s"Contains definitions for $protocolName types.",
      includes = aliasedTypeHeaders,
      types = structs,
      functions = functions
    )

    FileDefinition(name = headerFileName(protocolName), contents = headerContents)
  }

  /**
    * Gets the C source file for the message protocol
    * @param protocolName Name of the message protocol
    * @param functions List of protocol functions to define in the C source file
    * @return Message protocol C source file
    */
  private def cFile(protocolName: String, functions: Seq[FunctionDefinition]): FileDefinition = {
    // Need <stdlib.h> for free() and <string.h> for memset()
    val cFileContents = CFile(
      name = cFileName(protocolName),
      description =  s"Contains functions for working with $protocolName types.",
      includes = List(Constants.stdlibHeader, Constants.stringHeader, headerFileInclude(protocolName)),
      functions = functions
    )

    FileDefinition(name = cFileName(protocolName), contents = cFileContents)
  }
}
