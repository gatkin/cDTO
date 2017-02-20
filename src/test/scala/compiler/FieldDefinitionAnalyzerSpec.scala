package compiler

import datamodel._
import dto.UnitSpec

class FieldDefinitionAnalyzerSpec extends UnitSpec {

  "Field definition analyzer" should "accept a valid field definition" in {
    val validFieldDef = FieldDefinition("user_name", DynamicStringTypeDefinition(), List(JSONKeyAttribute("userName")))
    val validField = Field("user_name", DynamicStringType, "userName")

    FieldDefinitionAnalyzer(validFieldDef) shouldBe Right(validField)
  }

  it should "accept valid C-type aliases" in {
    val aliasedTypeFieldDef = FieldDefinition("user_id", NumberTypeDefinition(), List(JSONKeyAttribute("userId"), CTypeAttribute("uint32_t")))
    val aliasedTypeField = Field("user_id", AliasedType("uint32_t", NumberType), "userId")

    FieldDefinitionAnalyzer(aliasedTypeFieldDef) shouldBe Right(aliasedTypeField)
  }

  it should "accept valid C-type aliases of array elements" in {
    val aliasedArrayFieldDef = FieldDefinition("user_ids", ArrayTypeDefinition(NumberTypeDefinition()), List(CTypeAttribute("uint32_t"), JSONKeyAttribute("userIds")))
    val aliasedArrayField = Field("user_ids", ArrayType(AliasedType("uint32_t", NumberType)), "userIds")

    FieldDefinitionAnalyzer(aliasedArrayFieldDef) shouldBe Right(aliasedArrayField)
  }

  it should "use a default value for the JSON key if none was provided" in {
    val noJsonKeyFieldDef = FieldDefinition("user", ObjectTypeDefinition("user"), List())
    val noJsonKeyField = Field("user", ObjectType("user"), "user")

    FieldDefinitionAnalyzer(noJsonKeyFieldDef) shouldBe Right(noJsonKeyField)
  }

  it should "not accept a definition with duplicate JSON key attributes" in {
    val duplicateJsonKeyDef = FieldDefinition("user_id", NumberTypeDefinition(), List(JSONKeyAttribute("userId"), CTypeAttribute("uint32_t"), JSONKeyAttribute("id")))
    val duplicateJsonKeyError = InvalidFieldError("user_id", DuplicateAttributeError(Constants.JSON_KEY_ATTRIBUTE))

    FieldDefinitionAnalyzer(duplicateJsonKeyDef) shouldBe Left(duplicateJsonKeyError)
  }

  it should "not accept a definition with duplicate C-type attributes" in {
    val duplicateCTypeDef = FieldDefinition("is_logged_in", BooleanTypeDefinition(), List(JSONKeyAttribute("isLoggedIn"), CTypeAttribute("boolean"), CTypeAttribute("int")))
    val duplicateCTypeError = InvalidFieldError("is_logged_in", DuplicateAttributeError(Constants.C_TYPE_ATTRIBUTE))

    FieldDefinitionAnalyzer(duplicateCTypeDef) shouldBe Left(duplicateCTypeError)
  }

  it should "not accept a definition with an invalid C-type alias" in {
    val badAliasDef = FieldDefinition("user", ObjectTypeDefinition("user"), List(CTypeAttribute("user_type")))
    val badAliasError = InvalidFieldError("user", TypeAliasNotAllowedError(ObjectType("user").toString))

    FieldDefinitionAnalyzer(badAliasDef) shouldBe Left(badAliasError)
  }

  it should "not accept a definition with an invalid C-type array alias" in {
    val badArrayAliasDef = FieldDefinition("users", ArrayTypeDefinition(ObjectTypeDefinition("user")), List(CTypeAttribute("user_t"), JSONKeyAttribute("users")))
    val badArrayAliasError = InvalidFieldError("users", TypeAliasNotAllowedError(ObjectType("user").toString))

    FieldDefinitionAnalyzer(badArrayAliasDef) shouldBe Left(badArrayAliasError)
  }
}
