package compiler

import datamodel._
import dto.UnitSpec

class FieldDefinitionAnalyzerSpec extends UnitSpec {
  val validFieldDef = FieldDefinition("user_name", DynamicStringTypeDefinition(), List(JSONKeyAttribute("userName")))
  val validField = Field("user_name", DynamicStringType, "userName")

  val aliasedTypeFieldDef = FieldDefinition("user_id", NumberTypeDefinition(), List(JSONKeyAttribute("userId"), CTypeAttribute("uint32_t")))
  val aliasedTypeField = Field("user_id", AliasedType("uint32_t", NumberType), "userId")

  val aliasedArrayFieldDef = FieldDefinition("user_ids", ArrayTypeDefinition(NumberTypeDefinition()), List(CTypeAttribute("uint32_t"), JSONKeyAttribute("userIds")))
  val aliasedArrayField = Field("user_ids", ArrayType(AliasedType("uint32_t", NumberType)), "userIds")

  val noJsonKeyFieldDef = FieldDefinition("user", ObjectTypeDefinition("user"), List())
  val noJsonKeyField = Field("user", ObjectType("user"), "user")

  val duplicateJsonKeyDef = FieldDefinition("user_id", NumberTypeDefinition(), List(JSONKeyAttribute("userId"), CTypeAttribute("uint32_t"), JSONKeyAttribute("id")))
  val duplicateJsonKeyError = InvalidFieldError("user_id", DuplicateAttributeError(Constants.JSON_KEY_ATTRIBUTE))

  val duplicateCTypeDef = FieldDefinition("is_logged_in", BooleanTypeDefinition(), List(JSONKeyAttribute("isLoggedIn"), CTypeAttribute("boolean"), CTypeAttribute("int")))
  val duplicateCTypeError = InvalidFieldError("is_logged_in", DuplicateAttributeError(Constants.C_TYPE_ATTRIBUTE))

  val badAliasDef = FieldDefinition("user", ObjectTypeDefinition("user"), List(CTypeAttribute("user_type")))
  val badAliasError = InvalidFieldError("user", TypeAliasNotAllowedError(ObjectType("user").toString))

  val badArrayAliasDef = FieldDefinition("users", ArrayTypeDefinition(ObjectTypeDefinition("user")), List(CTypeAttribute("user_t"), JSONKeyAttribute("users")))
  val badArrayAliasError = InvalidFieldError("users", TypeAliasNotAllowedError(ObjectType("user").toString))


  "Field definition analyzer" should "accept a valid field definition" in {
    FieldDefinitionAnalyzer(validFieldDef) shouldBe Right(validField)
  }

  it should "accept valid C-type aliases" in {
    FieldDefinitionAnalyzer(aliasedTypeFieldDef) shouldBe Right(aliasedTypeField)
  }

  it should "accept valid C-type aliases of array elements" in {
    FieldDefinitionAnalyzer(aliasedArrayFieldDef) shouldBe Right(aliasedArrayField)
  }

  it should "use a default value for the JSON key if none was provided" in {
    FieldDefinitionAnalyzer(noJsonKeyFieldDef) shouldBe Right(noJsonKeyField)
  }

  it should "not accept a definition with duplicate JSON key attributes" in {
    FieldDefinitionAnalyzer(duplicateJsonKeyDef) shouldBe Left(duplicateJsonKeyError)
  }

  it should "not accept a definition with duplicate C-type attributes" in {
    FieldDefinitionAnalyzer(duplicateCTypeDef) shouldBe Left(duplicateCTypeError)
  }

  it should "not accept a definition with an invalid C-type alias" in {
    FieldDefinitionAnalyzer(badAliasDef) shouldBe Left(badAliasError)
  }

  it should "not accept a definition with an invalid C-type array alias" in {
    FieldDefinitionAnalyzer(badArrayAliasDef) shouldBe Left(badArrayAliasError)
  }
}
