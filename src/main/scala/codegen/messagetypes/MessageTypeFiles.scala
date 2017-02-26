package codegen.messagetypes

import codegen.functions._
import codegen.sourcefile._
import codegen.types.StructDefinition
import datamodel._

/**
  * Contains the contents the source files to define message types
  * and the functions to manage them
  * @param header Header file
  * @param cFile C source file
  */
case class MessageFiles(header: MessageHeaderFile, cFile: MessageCFile)
case class MessageHeaderFile(name: String, contents: String)
case class MessageCFile(name: String, contents: String)

object MessageTypeFiles {

  private val stdlibHeader = "<stdlib.h>"
  private val stringHeader = "<string.h>"

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
  def apply(protocol: Protocol, aliasedTypeHeaders: Seq[String]): MessageFiles = {
    // Get all of the structs to define
    val structs = protocol.messages.map(message => MessageStruct(message))

    // Get all of the functions to declare and define
    val initFunctions = protocol.messages.map(message => MessageInitFunction(message))
    val freeFunctions = protocol.messages.map(message => MessageFreeFunction(message))
    val fieldArrayFreeFunctions = protocol.messages.flatMap(message => MessageFreeFunction.arrayFieldFreeFunctions(message))

    val allFunctions = initFunctions ++ freeFunctions ++ fieldArrayFreeFunctions

    // Create the header and source files
    val header = headerFile(protocol.name, aliasedTypeHeaders, structs, allFunctions)
    val sourceFile = cFile(protocol.name, allFunctions)

    MessageFiles(header, sourceFile)
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
    * Gets the header file for the message protocol
    * @param protocolName Name of the message protocol
    * @param aliasedTypeHeaders Set of header files that contain type definitions for all aliased types
    *                           specified in the protocol
    * @param structs List of protocol message struct definitions to declare
    * @param functions List of protocol message functions to declare
    * @return Message protocol header file
    */
  private def headerFile(protocolName: String, aliasedTypeHeaders: Seq[String], structs: Seq[StructDefinition], functions: Seq[FunctionDefinition]): MessageHeaderFile = {
    val headerContents = HeaderFile(
      name = headerFileName(protocolName),
      description =  s"Contains definitions for $protocolName types.",
      includes = aliasedTypeHeaders,
      types = structs,
      functions = functions
    )

    MessageHeaderFile(name = headerFileName(protocolName), contents = headerContents)
  }

  /**
    * Gets the C source file for the message protocol
    * @param protocolName Name of the message protocol
    * @param functions List of protocol functions to define in the C source file
    * @return Message protocol C source file
    */
  private def cFile(protocolName: String, functions: Seq[FunctionDefinition]): MessageCFile = {
    // Need <stdlib.h> for free() and <string.h> for memset()
    val cFileContents = CFile(
      name = cFileName(protocolName),
      description =  s"Contains functions for working with $protocolName types.",
      includes = List(stdlibHeader, stringHeader, headerFileInclude(protocolName)),
      functions = functions
    )

    MessageCFile(name = cFileName(protocolName), contents = cFileContents)
  }
}
