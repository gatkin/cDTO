package compiler

import datamodel._
import dto.UnitSpec

class MessageDefinitionAnalyzerSpec extends UnitSpec {

  "Message definition analyzer" should "accept a valid message definition" in {
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

    MessageDefinitionAnalyzer(validMessageDef) shouldBe Right(validMessage)
  }

  it should "not accept a definition with duplicate fields" in {
    val duplicateFieldsDef = MessageDefinition("issue", List(
      FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
      FieldDefinition("number", DynamicStringTypeDefinition(), List()),
      FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user")))
    ))

    val duplicateFieldsError = InvalidMessageError("issue", DuplicateFieldsError(List("number")))

    MessageDefinitionAnalyzer(duplicateFieldsDef) shouldBe Left(duplicateFieldsError)
  }

  it should "not accept a definition with multiple duplicate fields" in {
    val multipleDuplicateFieldsDef = MessageDefinition("issue", List(
      FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
      FieldDefinition("creator", NumberTypeDefinition(), List(JSONKeyAttribute("creatorId"), CTypeAttribute("uint32-t"))),
      FieldDefinition("number", DynamicStringTypeDefinition(), List()),
      FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user")))
    ))

    val multipleDuplicateFieldsError = InvalidMessageError("issue", DuplicateFieldsError(List("number", "creator")))

    MessageDefinitionAnalyzer(multipleDuplicateFieldsDef) shouldBe Left(multipleDuplicateFieldsError)
  }

  it should "not accept a definition with invalid fields" in {
    val fieldErrorsDef = MessageDefinition("issue", List(
      FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"), JSONKeyAttribute("issueId"))),
      FieldDefinition("url", DynamicStringTypeDefinition(), List()),
      FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user"), CTypeAttribute("user_type")))
    ))

    val fieldErrorsError = InvalidMessageError("issue", FieldErrors(List(
      InvalidFieldError("number", DuplicateAttributeError(Constants.JSON_KEY_ATTRIBUTE)),
      InvalidFieldError("creator", TypeAliasNotAllowedError(ObjectType("user").toString))
    )))

    MessageDefinitionAnalyzer(fieldErrorsDef) shouldBe Left(fieldErrorsError)
  }
}
