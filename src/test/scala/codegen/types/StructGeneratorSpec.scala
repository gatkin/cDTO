package codegen.types

import dto.UnitSpec

class StructGeneratorSpec extends UnitSpec {

  "Struct generator" should "generate a struct definition string with many fields" in {
    val definition = StructDefinition("issue_type", List(
      SimpleStructField("reporter", "user_type"),
      SimpleStructField("id", "uint32_t"),
      SimpleStructField("title", "char*"),
      SimpleStructField("assignees", "user_type*"),
      SimpleStructField("assignees_cnt", "int")
    ))

    val struct =
      """typedef struct
        |    {
        |    user_type     reporter;
        |    uint32_t      id;
        |    char*         title;
        |    user_type*    assignees;
        |    int           assignees_cnt;
        |    } issue_type;""".stripMargin

    StructGenerator(definition) shouldBe struct
  }

  it should "generate a struct definition string with fields of fixed-length arrays" in {
    val definition = StructDefinition("simple_message_type", List(
      FixedArrayStructField("color", "char", 7)
    ))

    val struct =
      """typedef struct
        |    {
        |    char    color[ 7 ];
        |    } simple_message_type;""".stripMargin

    StructGenerator(definition) shouldBe struct
  }
}
