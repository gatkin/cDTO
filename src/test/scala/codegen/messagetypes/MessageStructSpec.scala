package codegen.messagetypes

import codegen.types._
import datamodel._
import dto.UnitSpec

class MessageStructSpec extends UnitSpec {

  "Message struct" should "generate a struct definition for cDTO messages" in {
    val message = Message("my_message_t", List(
      Field("dynamic_string_field", DynamicStringType, "dynamicStringField"),
      Field("issue_field", ObjectType("issue"), "issue"),
      Field("fixed_string_field", FixedStringType(32), "fixedStringField"),
      Field("fixed_string_array", ArrayType(FixedStringType(6)), "fixedStringArray"),
      Field("number_array", ArrayType(NumberType), "numberArray"),
      Field("aliased_field", AliasedType("bool", BooleanType), "aliasedField"),
      Field("aliased_array", ArrayType(AliasedType("uint32_t", NumberType)), "aliasedArray")
    ))

    val struct = StructDefinition(
      name = "my_message_t",
      fields = List(
        SimpleStructField("dynamic_string_field", "char*"),
        SimpleStructField("issue_field", "issue"),
        FixedArrayStructField("fixed_string_field", "char", 33),
        SimpleStructField("fixed_string_array", "char**"),
        SimpleStructField("fixed_string_array_cnt", "int"),
        SimpleStructField("number_array", "double*"),
        SimpleStructField("number_array_cnt", "int"),
        SimpleStructField("aliased_field", "bool"),
        SimpleStructField("aliased_array", "uint32_t*"),
        SimpleStructField("aliased_array_cnt", "int")
      )
    )

    MessageStruct(message) shouldBe struct
  }
}
