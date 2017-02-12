package compiler

import datamodel._
import dto.UnitSpec

class MessageFieldDefinitionAnalyzerSpec extends UnitSpec{
  val validFieldDef = FieldDefinition("user_name", DynamicStringTypeDefinition(), List(JSONKeyAttribute("userName")))
  val validField = Field("user_name", DynamicStringType, "userName")

  val aliasedTypeFieldDef = FieldDefinition("user_id", NumberTypeDefinition(), List(JSONKeyAttribute("userId"), CTypeAttribute("uint32_t")))
  val aliasedTypeField = Field("user_id", AliasedType("uint32_t", NumberType), "userId")

  val aliasedArrayFieldDef = FieldDefinition("user_ids", ArrayTypeDefinition(NumberTypeDefinition()), List(CTypeAttribute("uint32_t"), JSONKeyAttribute("userIds")))
  val aliasedArrayField = Field("user_ids", AliasedType("uint32_t", ArrayType(NumberType)), "userIds")

  val noJsonKeyFieldDef = FieldDefinition("user", ObjectTypeDefinition("user"), List())
  val noJsonKeyField = Field("user", ObjectType("user"), "user")

  val duplicateJsonKeyDef = FieldDefinition("user_id", NumberTypeDefinition(), List(JSONKeyAttribute("userId"), CTypeAttribute("uint32_t"), JSONKeyAttribute("id")))
  val duplicateJsonKeyError = DuplicateAttributeError(Constants.JSON_KEY_ATTRIBUTE, "user_id")

  val duplicateCTypeDef = FieldDefinition("is_logged_in", BooleanTypeDefinition(), List(JSONKeyAttribute("isLoggedIn"), CTypeAttribute("boolean"), CTypeAttribute("int")))
  val duplicateCTypeError = DuplicateAttributeError(Constants.C_TYPE_ATTRIBUTE, "is_logged_in")

  val badAliasDef = FieldDefinition("user", ObjectTypeDefinition("user"), List(CTypeAttribute("user_type")))
  val badAliasError = TypeAliasNotAllowedError(ObjectType("user").toString, "user")

  val badArrayAliasDef = FieldDefinition("users", ArrayTypeDefinition(ObjectTypeDefinition("user")), List(CTypeAttribute("user_t"), JSONKeyAttribute("users")))
  val badArrayAliasError = TypeAliasNotAllowedError(ArrayType(ObjectType("user")).toString, "users")


  "Message field definition analyzer" should "accept a valid field definition" in {
    MessageFieldDefinitionAnalyzer(validFieldDef) shouldBe Right(validField)
  }

  it should "accept valid C-type aliases" in {
    MessageFieldDefinitionAnalyzer(aliasedTypeFieldDef) shouldBe Right(aliasedTypeField)
  }

  it should "accept valid C-type aliases of array elements" in {
    MessageFieldDefinitionAnalyzer(aliasedArrayFieldDef) shouldBe Right(aliasedArrayField)
  }

  it should "use a default value for the JSON key if none was provided" in {
    MessageFieldDefinitionAnalyzer(noJsonKeyFieldDef) shouldBe Right(noJsonKeyField)
  }

  it should "not accept a definition with duplicate JSON key attributes" in {
    MessageFieldDefinitionAnalyzer(duplicateJsonKeyDef) shouldBe Left(duplicateJsonKeyError)
  }

  it should "not accept a definition with duplicate C-type attributes" in {
    MessageFieldDefinitionAnalyzer(duplicateCTypeDef) shouldBe Left(duplicateCTypeError)
  }

  it should "not accept a definition with an invalid C-type alias" in {
    MessageFieldDefinitionAnalyzer(badAliasDef) shouldBe Left(badAliasError)
  }

  it should "not accept a definition with an invalid C-type array alias" in {
    MessageFieldDefinitionAnalyzer(badArrayAliasDef) shouldBe Left(badArrayAliasError)
  }
}
