package codegen.sourcefile

import codegen.types._
import dto.UnitSpec

class StructDeclarationOrderSpec extends UnitSpec {

  "Struct declaration order" should "order struct definitions that have dependencies" in {
    val definitions = List(
      StructDefinition("struct_c", List(
        SimpleStructField("d", "struct_d"),
        SimpleStructField("e", "struct_e"),
        SimpleStructField("f", "struct_f"),
        SimpleStructField("an_int", "int")
      )),
      StructDefinition("struct_f", Nil),
      StructDefinition("struct_d", List(
        SimpleStructField("f", "struct_f"),
        SimpleStructField("a_string", "char*")
      )),
      StructDefinition("struct_a", List(
        SimpleStructField("b", "struct_b")
      )),
      StructDefinition("struct_e", Nil),
      StructDefinition("struct_b", List(
        SimpleStructField("c", "struct_c"),
        SimpleStructField("d", "struct_d")
      ))
    )

    val orderedDefinitions = List(
      StructDefinition("struct_f", Nil),
      StructDefinition("struct_e", Nil),
      StructDefinition("struct_d", List(
        SimpleStructField("f", "struct_f"),
        SimpleStructField("a_string", "char*")
      )),
      StructDefinition("struct_c", List(
        SimpleStructField("d", "struct_d"),
        SimpleStructField("e", "struct_e"),
        SimpleStructField("f", "struct_f"),
        SimpleStructField("an_int", "int")
      )),
      StructDefinition("struct_b", List(
        SimpleStructField("c", "struct_c"),
        SimpleStructField("d", "struct_d")
      )),
      StructDefinition("struct_a", List(
        SimpleStructField("b", "struct_b")
      ))
    )

    StructDefinitionOrder(definitions) shouldBe Some(orderedDefinitions)
  }

  it should "order struct definitions that have pointer dependencies" in {
    val definitions = List(
      StructDefinition("struct_b", List(
        FixedArrayStructField("a_fixed_string", "char", 32),
        SimpleStructField("an_int", "int")
      )),
      StructDefinition("struct_a", List(
        SimpleStructField("b", "struct_b*"),
        SimpleStructField("b_cnt", "int"),
        SimpleStructField("c", "struct_c*"),
        SimpleStructField("c_cnt", "int")
      )),
      StructDefinition("struct_c", List(
        SimpleStructField("a_number", "double")
      ))
    )

    val orderedDefinitions = List(
      StructDefinition("struct_b", List(
        FixedArrayStructField("a_fixed_string", "char", 32),
        SimpleStructField("an_int", "int")
      )),
      StructDefinition("struct_c", List(
        SimpleStructField("a_number", "double")
      )),
      StructDefinition("struct_a", List(
        SimpleStructField("b", "struct_b*"),
        SimpleStructField("b_cnt", "int"),
        SimpleStructField("c", "struct_c*"),
        SimpleStructField("c_cnt", "int")
      ))
    )

    StructDefinitionOrder(definitions) shouldBe Some(orderedDefinitions)
  }

  it should "order struct definitions that have fixed array dependencies" in {
    val definitions = List(
      StructDefinition("struct_b", List(
        SimpleStructField("a_string", "char*"),
        FixedArrayStructField("c", "struct_c", 5)
      )),
      StructDefinition("struct_a", List(
        FixedArrayStructField("c", "struct_c", 10),
        SimpleStructField("a_boolean", "boolean")
      )),
      StructDefinition("struct_c", List(
        SimpleStructField("ids", "uint32_t*"),
        SimpleStructField("ids_cnt", "int")
      ))
    )

    val orderedDefinitions = List(
      StructDefinition("struct_c", List(
        SimpleStructField("ids", "uint32_t*"),
        SimpleStructField("ids_cnt", "int")
      )),
      StructDefinition("struct_b", List(
        SimpleStructField("a_string", "char*"),
        FixedArrayStructField("c", "struct_c", 5)
      )),
      StructDefinition("struct_a", List(
        FixedArrayStructField("c", "struct_c", 10),
        SimpleStructField("a_boolean", "boolean")
      ))
    )

    StructDefinitionOrder(definitions) shouldBe Some(orderedDefinitions)
  }

  it should "not change the order struct definitions that do not have any dependencies" in {
    val definitions = List(
      StructDefinition("struct_b", List(
        SimpleStructField("urls", "char**"),
        SimpleStructField("urls_cnt", "int")
      )),
      StructDefinition("struct_a", List(
        SimpleStructField("a_boolean", "boolean")
      )),
      StructDefinition("struct_c", List(
        SimpleStructField("ids", "uint32_t*"),
        SimpleStructField("ids_cnt", "int")
      ))
    )

    StructDefinitionOrder(definitions) shouldBe Some(definitions)
  }

  it should "report an error for struct definitions that contain circular dependencies" in {
    val definitions = List(
      StructDefinition("struct_b", List(
        SimpleStructField("c", "struct_c")
      )),
      StructDefinition("struct_a", List(
        FixedArrayStructField("b", "struct_b", 10)
      )),
      StructDefinition("struct_c", List(
        SimpleStructField("a", "struct_a")
      ))
    )

    StructDefinitionOrder(definitions) shouldBe None
  }
}
