package codegen.messagetypes

import codegen.functions._
import datamodel._
import dto.UnitSpec

class MessageFreeFunctionSpec extends UnitSpec {

    "Message free function" should "generate a free function for messages with dynamic members" in {
      val message = Message("my_message_t", List(
        Field("boolean_field", BooleanType, "booleanField"),
        Field("number_field", NumberType, "numberField"),
        Field("dynamic_string_field", DynamicStringType, "dynamicStringField"),
        Field("fixed_string_field", FixedStringType(32), "fixedStringField"),
        Field("issue_field", ObjectType("issue"), "issue"),
        Field("array_field", ArrayType(ObjectType("user")), "arrayField")
      ))

      val freeFunction = FunctionDefinition(
        name = "my_message_t_free",
        documentation = FunctionDocumentation(
          shortSummary = "Free my_message_t",
          description = "Cleans up all resources owned by the provided my_message_t."
        ),
        prototype = FunctionPrototype(
          isStatic = false,
          returnType = "void",
          parameters = List(
            FunctionParameter(paramType = "my_message_t*", paramName = "obj")
          )
        ),
        body =
          """free( obj->dynamic_string_field );
            |issue_free( &obj->issue_field );
            |user_array_free( obj->array_field, obj->array_field_cnt );
            |
            |my_message_t_init( obj );""".stripMargin
      )

      MessageFreeFunction(message) shouldBe freeFunction
    }

  it should "generate a free function for messages with no dynamic members" in {
    val message = Message("my_message_t", List(
      Field("boolean_field", BooleanType, "booleanField"),
      Field("number_field", NumberType, "numberField"),
      Field("fixed_string_field", FixedStringType(32), "fixedStringField")
    ))

    val freeFunction = FunctionDefinition(
      name = "my_message_t_free",
      documentation = FunctionDocumentation(
        shortSummary = "Free my_message_t",
        description = "Cleans up all resources owned by the provided my_message_t."
      ),
      prototype = FunctionPrototype(
        isStatic = false,
        returnType = "void",
        parameters = List(
          FunctionParameter(paramType = "my_message_t*", paramName = "obj")
        )
      ),
      body = "my_message_t_init( obj );"
    )

    MessageFreeFunction(message) shouldBe freeFunction
  }
}
