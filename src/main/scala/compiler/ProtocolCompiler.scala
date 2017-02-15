package compiler

import datamodel._

/**
  * Top-level entry-point for compiling and analyzing a protocol definition
  */
object ProtocolCompiler {

  /**
    * Compiles the given protocol definition string
    * @param protocolDefinition - String containing the protocol definition
    * @param protocolName - Name of the protocol
    * @return Either an error if the protocol definition string is invalid or
    *         the parsed protocol.
    */
  def apply(protocolDefinition: String, protocolName: String): Either[CompilerError, Protocol] = {
    for {
      ast <- ProtocolParser(protocolDefinition).right
      protocol <- ProtocolDefinitionAnalyzer(ast, protocolName).right
    } yield protocol
  }
}
