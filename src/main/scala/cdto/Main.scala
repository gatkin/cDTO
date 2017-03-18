package cdto

import codegen.json._
import codegen.messagetypes._
import compiler._
import datamodel.Protocol

import io.Source
import java.nio.file.Paths
import java.io.{File, PrintWriter}

import codegen.sourcefile.FileDefinition
import org.rogach.scallop._


object Main  {

  private class ArgConfig(arguments: Seq[String]) extends ScallopConf(arguments) {
    val protocol = opt[String](required = true, descr = "Path to the protocol definition file")
    val outputDir = opt[String](required = true, descr = "Path to the directory to write the generated files")
    val typeHeaders = opt[List[String]](descr = "List of headers containing C-type definitions used in message fields")

    verify()
  }

  def main(args: Array[String]): Unit = {
    val parsedArgs = new ArgConfig(args)

    val protocolFile = parsedArgs.protocol()
    val outputDir = parsedArgs.outputDir()
    val typeHeaders = parsedArgs.typeHeaders.getOrElse(Nil)

    val protocolName = protocolNameFromPath(protocolFile)
    val definition = readProtocolDefinition(parsedArgs.protocol())
    val compilerResult = ProtocolCompiler(definition, protocolName)

    compilerResult match {
      case Left(error) => println(error)
      case Right(protocol) => {
        writeProtocolTypeFiles(protocol, typeHeaders, outputDir)
        writeProtocolJSONFiles(protocol, outputDir)
      }
    }
  }

  /**
    * Gets the name of the protocol from the provided path to the protocol
    * definition file
    */
  private def protocolNameFromPath(protocolFilePath: String): String = {
    Paths.get(protocolFilePath).getFileName.toString
  }

  /**
    * Reads the protocol definition file. Note: This ignores all errors reading
    * the file.
    * @param definitionPath Absolute path to the protocol definition file
    * @return Contents of the protocol definition file
    */
  private def readProtocolDefinition(definitionPath: String): String = {
    // TODO: Handle errors
    Source.fromFile(definitionPath).getLines.mkString
  }

  /**
    * Writes the protocol type definition files to the specified output directory
    * @param protocol Protocol
    * @param typeHeaders List of headers containing definitions for custom C-types
    * @param outputDir Absolute path to the directory which to write the protocol type
    *                  files
    */
  private def writeProtocolTypeFiles(protocol: Protocol, typeHeaders: Seq[String], outputDir: String): Unit = {
    val messageTypeFiles = MessageTypeFiles(protocol, typeHeaders)

    writeFile(outputDir, messageTypeFiles.headerFile)
    writeFile(outputDir, messageTypeFiles.cFile)
  }

  /**
    * Writes the protocol JSON parsing/serialization files to the specified directory
    * @param protocol Protocol
    * @param outputDir Directory to which the files are to be written
    */
  private def writeProtocolJSONFiles(protocol: Protocol, outputDir: String): Unit = {
    val jsonFiles = MessageJSONFiles(protocol)

    writeFile(outputDir, jsonFiles.headerFile)
    writeFile(outputDir, jsonFiles.cFile)
  }

  /**
    * Writes the given file to the specified output directory. Note: This ignores
    * all errors writing the file
    * @param outputDir Absolute path to the directory which the file is to be written
    * @param file File to write
    */
  private def writeFile(outputDir: String, file: FileDefinition): Unit = {
    val path = Paths.get(outputDir, file.name)

    // TODO: Handle errors
    val writer = new PrintWriter(new File(path.toAbsolutePath.toString))
    writer.write(file.contents)
    writer.close()
  }
}
