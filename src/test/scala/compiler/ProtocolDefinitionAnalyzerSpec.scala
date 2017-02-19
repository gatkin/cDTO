package compiler

import datamodel._
import dto.UnitSpec

class ProtocolDefinitionAnalyzerSpec extends UnitSpec{
  val protocolName = "TestProtocol"

  val validAST = ProtocolAST(List(
    MessageDefinition("issue", List(
      FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
      FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user")))
    )),
    MessageDefinition("user", List(
      FieldDefinition("name", DynamicStringTypeDefinition(), List(JSONKeyAttribute("login"))),
      FieldDefinition("url", DynamicStringTypeDefinition(), List())
    ))
  ))
  val validProtocol = Protocol(protocolName, List(
    Message("issue", List(
      Field("number", AliasedType("uint32_t", NumberType), "issueNumber"),
      Field("creator", ObjectType("user"), "user")
    )),
    Message("user", List(
      Field("name", DynamicStringType, "login"),
      Field("url", DynamicStringType, "url")
    ))
  ))

  val duplicateMessagesAST = ProtocolAST(List(
    MessageDefinition("issue", List(
      FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
      FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user")))
    )),
    MessageDefinition("user", List(
      FieldDefinition("name", DynamicStringTypeDefinition(), List(JSONKeyAttribute("login"))),
      FieldDefinition("url", DynamicStringTypeDefinition(), List())
    )),
    MessageDefinition("issue", List(
      FieldDefinition("title", DynamicStringTypeDefinition(), List()),
      FieldDefinition("linked_issues", ArrayTypeDefinition(DynamicStringTypeDefinition()), List(JSONKeyAttribute("links")))
    ))
  ))
  val duplicateMessageError = DuplicateMessagesError(List("issue"))

  val undefinedObjectFieldAST = ProtocolAST(List(
    MessageDefinition("issue", List(
      FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
      FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user")))
    ))
  ))
  val undefinedObjectFieldError = ObjectTypesNotDefinedError(List("user"))

  val invalidMessagesAST = ProtocolAST(List(
    MessageDefinition("issue", List(
      FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
      FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user"), JSONKeyAttribute("creator")))
    )),
    MessageDefinition("user", List(
      FieldDefinition("name", DynamicStringTypeDefinition(), List(JSONKeyAttribute("login"))),
      FieldDefinition("name", DynamicStringTypeDefinition(), List(JSONKeyAttribute("userName")))
    ))
  ))
  val invalidMessagesError = InvalidMessagesError(List(
    InvalidFieldsError(List(InvalidFieldError("creator", DuplicateAttributeError(Constants.JSON_KEY_ATTRIBUTE))),"issue"),
    DuplicateFieldsError(List("name"), "user")
  ))

  "Protocol definition analyzer" should "accept a valid AST" in {
    ProtocolDefinitionAnalyzer(validAST, protocolName) shouldBe Right(validProtocol)
  }

  it should "not accept an AST with duplicated message names" in {
    ProtocolDefinitionAnalyzer(duplicateMessagesAST, protocolName) shouldBe Left(duplicateMessageError)
  }

  it should "not accept an AST with undefined object type fields" in {
    ProtocolDefinitionAnalyzer(undefinedObjectFieldAST, protocolName) shouldBe Left(undefinedObjectFieldError)
  }

  it should "not accept an AST with invalid messages" in {
    ProtocolDefinitionAnalyzer(invalidMessagesAST, protocolName) shouldBe Left(invalidMessagesError)
  }
}
