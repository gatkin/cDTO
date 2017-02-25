package codegen.types

import datamodel._
import dto.UnitSpec

class MessageStructSpec extends UnitSpec {

  "Message struct" should "generate a struct definition containing only simple, un-aliased types" in {
    val simpleTypesMessage = Message("my_message_t", List(
      Field("boolean_field", BooleanType, "booleanField"),
      Field("number_field", NumberType, "numberField"),
      Field("dynamic_string_field", DynamicStringType, "dynamicStringField"),
      Field("fixed_string_field", FixedStringType(32), "fixedStringField"),
      Field("issue_field", ObjectType("issue"), "issue")
    ))

    val simpleTypesMessageStruct =
      """typedef struct {
        |    int boolean_field;
        |    double number_field;
        |    char* dynamic_string_field;
        |    char fixed_string_field[32 + 1];
        |    issue issue_field;
        |} my_message_t;""".stripMargin

    MessageStruct(simpleTypesMessage) shouldBe simpleTypesMessageStruct
  }

  it should "generate a struct definition containing arrays of simple un-aliased types" in {
    val simpleTypeArraysMessage = Message("my_arrays_message_t", List(
      Field("boolean_field", ArrayType(BooleanType), "booleanField"),
      Field("number_field", ArrayType(NumberType), "numberField"),
      Field("dynamic_string_field", ArrayType(DynamicStringType), "dynamicStringField"),
      Field("fixed_string_field", ArrayType(FixedStringType(32)), "fixedStringField"),
      Field("issue_field", ArrayType(ObjectType("issue")), "issue")
    ))

    val simpleTypeArraysStruct =
      """typedef struct {
        |    int* boolean_field;
        |    int boolean_field_cnt;
        |    double* number_field;
        |    int number_field_cnt;
        |    char** dynamic_string_field;
        |    int dynamic_string_field_cnt;
        |    char** fixed_string_field;
        |    int fixed_string_field_cnt;
        |    issue* issue_field;
        |    int issue_field_cnt;
        |} my_arrays_message_t;""".stripMargin

    MessageStruct(simpleTypeArraysMessage) shouldBe simpleTypeArraysStruct
  }

  it should "generate a struct definition with aliased types" in {
    val aliasedTypesMessage = Message("my_message_t", List(
      Field("boolean_field", AliasedType("boolean", BooleanType), "booleanField"),
      Field("number_field", AliasedType("uint32", NumberType), "numberField"),
      Field("dynamic_string_field", AliasedType("string", DynamicStringType), "dynamicStringField"),
      Field("fixed_string_field", AliasedType("id", FixedStringType(32)), "fixedStringField")
    ))

    val aliasedTypesStruct =
      """typedef struct {
        |    boolean boolean_field;
        |    uint32 number_field;
        |    string dynamic_string_field;
        |    id fixed_string_field;
        |} my_message_t;""".stripMargin

    MessageStruct(aliasedTypesMessage) shouldBe aliasedTypesStruct
  }

  it should "generate a struct definition with aliased array element types" in {
    val aliasedArraysMessage = Message("my_arrays_message_t", List(
      Field("boolean_field", ArrayType(AliasedType("boolean", BooleanType)), "booleanField"),
      Field("number_field", ArrayType(AliasedType("uint32", NumberType)), "numberField"),
      Field("dynamic_string_field", ArrayType(AliasedType("string", DynamicStringType)), "dynamicStringField"),
      Field("fixed_string_field", ArrayType(AliasedType("id", FixedStringType(32))), "fixedStringField")
    ))

    val aliasedArraysStruct =
      """typedef struct {
        |    boolean* boolean_field;
        |    int boolean_field_cnt;
        |    uint32* number_field;
        |    int number_field_cnt;
        |    string* dynamic_string_field;
        |    int dynamic_string_field_cnt;
        |    id* fixed_string_field;
        |    int fixed_string_field_cnt;
        |} my_arrays_message_t;""".stripMargin

    MessageStruct(aliasedArraysMessage) shouldBe aliasedArraysStruct
  }
}
