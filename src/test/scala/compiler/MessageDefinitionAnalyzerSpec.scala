package compiler

import datamodel._
import dto.UnitSpec

class MessageDefinitionAnalyzerSpec extends UnitSpec{

  val validMessageDef = MessageDefinition("issue", List(
    FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
    FieldDefinition("url", DynamicStringTypeDefinition(), List()),
    FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user")))
  ))
  val validMessage = Message("issue", List(
    Field("number", AliasedType("uint32_t", NumberType), "issueNumber"),
    Field("url", DynamicStringType, "url"),
    Field("creator", ObjectType("user"), "user")
  ))

  val duplicateFieldsDef = MessageDefinition("issue", List(
    FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
    FieldDefinition("number", DynamicStringTypeDefinition(), List()),
    FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user")))
  ))
  val duplicateFieldsError = DuplicateFieldsError(List("number"), "issue")

  val multipleDuplicateFieldsDef = MessageDefinition("issue", List(
    FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
    FieldDefinition("creator", NumberTypeDefinition(), List(JSONKeyAttribute("creatorId"), CTypeAttribute("uint32-t"))),
    FieldDefinition("number", DynamicStringTypeDefinition(), List()),
    FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user")))
  ))
  val multipleDuplicateFieldsError = DuplicateFieldsError(List("number", "creator"), "issue")

  val fieldErrorsDef = MessageDefinition("issue", List(
    FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"), JSONKeyAttribute("issueId"))),
    FieldDefinition("url", DynamicStringTypeDefinition(), List()),
    FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user"), CTypeAttribute("user_type")))
  ))
  val fieldErrorsError = InvalidFieldsError(List(
    DuplicateAttributeError(Constants.JSON_KEY_ATTRIBUTE, "number"),
    TypeAliasNotAllowedError(ObjectType("user").toString, "creator")
  ), "issue")


  "Message definition analyzer" should "accept a valid message definition" in {
    MessageDefinitionAnalyzer(validMessageDef) shouldBe Right(validMessage)
  }

  it should "not accept a definition with duplicate fields" in {
    MessageDefinitionAnalyzer(duplicateFieldsDef) shouldBe Left(duplicateFieldsError)
  }

  it should "not accept a definition with multiple duplicate fields" in {
    MessageDefinitionAnalyzer(multipleDuplicateFieldsDef) shouldBe Left(multipleDuplicateFieldsError)
  }

  it should "not accept a definition with invalid fields" in {
    MessageDefinitionAnalyzer(fieldErrorsDef) shouldBe Left(fieldErrorsError)
  }
}
