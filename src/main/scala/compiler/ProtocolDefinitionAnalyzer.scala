package compiler

import datamodel._

/**
  * Contains functionality for analyzing and validating ASTs from parsed
  * cDTO protocol definitions.
  */
object ProtocolDefinitionAnalyzer {

  /**
    * Analyzes the provided AST to ensure it represents a valid cDTO protocol. If the
    * AST is valid, then this will produce the corresponding protocol.
    * @param ast - Abstract syntax tree from a parsed protocol definition
    * @param protocolName - Name of the protocol
    * @return Either an error if the protocol is invalid, otherwise the protocol that corresponds
    *         to the AST is returned.
    */
  def apply(ast: ProtocolAST, protocolName: String): Either[SemanticError, Protocol] = {
    for {
      messages <- messagesGetAll(ast).right
      messages <- messagesCheckDuplicates(messages).right
      messages <- messagesCheckUndefinedFields(messages).right
    } yield Protocol(protocolName, messages)
  }

  /**
    * Gets all messages from the protocol definition AST.
    * @param ast - Abstract syntax tree from the parsed protocol definition
    * @return An error if any of the message definitions were invalid, otherwise the input
    *         sequence of messages is returned unmodified.
    */
  private def messagesGetAll(ast: ProtocolAST): Either[SemanticError, Seq[Message]] = {
    val messages = ast.messages.map(message => MessageDefinitionAnalyzer(message))

    val errors = messages.flatMap(message => message.left.toOption)
    val validMessages = messages.flatMap(message => message.right.toOption)

    errors match {
      case Nil => Right(validMessages)
      case _ => Left(MessageErrors(errors))
    }
  }

  /**
    * Checks whether any duplicate message definitions with the same message name were
    * provided in the protocol definition.
    * @param messages - List of messages in the protocol
    * @return Ann error if multiple messages have the same name, otherwise the input
    *         sequence of messages is returned unmodified.
    */
  private def messagesCheckDuplicates(messages: Seq[Message]): Either[SemanticError, Seq[Message]] = {
    val messagesByNames = messages.groupBy(_.name)

    val duplicateMessages = messagesByNames.keys.filter(messageName => messagesByNames(messageName).size > 1).toSeq

    duplicateMessages match {
      case Nil => Right(messages)
      case _ => Left(DuplicateMessagesError(duplicateMessages))
    }
  }

  /**
    * Checks whether any messages contain undefined fields. Messages may contain fields that are
    * an object type. These object types must refer to another message defined in the protocol, otherwise
    * it is an invalid protocol definition.
    * @param messages - List of messages in the protocol
    * @return Ann error if there are any undefined object type fields, otherwise the input
    *         sequence of messages is returned unmodified.
    */
  private def messagesCheckUndefinedFields(messages: Seq[Message]): Either[SemanticError, Seq[Message]] = {
    val messageNames = messages.map(_.name).toSet

    val fieldTypes = for {
      message <- messages
      field <- message.fields
    } yield field.fieldType

    val objectFields = fieldTypes.collect({ case ObjectType(objectName) => objectName }).toSet

    // For every field that has an object type, the name of the object must correspond to
    // one of the messages in the protocol definition
    val undefinedObjects = objectFields.diff(messageNames)

    if (undefinedObjects.isEmpty) {
      Right(messages)
    } else {
      Left(ObjectTypesNotDefinedError(undefinedObjects.toSeq))
    }
  }
}
