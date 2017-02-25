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
            |my_message_t_array_field_free( obj->array_field, obj->array_field_cnt );
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

  it should "generate a list of array field free functions for messages with array fields" in {
    val message = Message("issue", List(
      Field("assignees", ArrayType(ObjectType("user")), "assignees"),
      Field("ids", ArrayType(AliasedType("uint32_t", NumberType)), "ids")
    ))

    val arrayFreeFunctions = List(
      FunctionDefinition(
        name = "issue_assignees_free",
        documentation = FunctionDocumentation(
          shortSummary = "Free array",
          description = "Cleans up all resources owned by the array and its elements."
        ),
        prototype = FunctionPrototype(
          isStatic = true,
          returnType = "void",
          parameters = List(
            FunctionParameter(paramType = "user*", paramName = "array"),
            FunctionParameter(paramType = "int", paramName = "array_cnt")
          )
        ),
        body =
          """int i;
            |
            |for( i = 0; i < array_cnt; i++ )
            |{
            |    user_free( &array[i] );
            |}
            |
            |free( array );""".stripMargin
      ),
      FunctionDefinition(
        name = "issue_ids_free",
        documentation = FunctionDocumentation(
          shortSummary = "Free array",
          description = "Cleans up all resources owned by the array and its elements."
        ),
        prototype = FunctionPrototype(
          isStatic = true,
          returnType = "void",
          parameters = List(
            FunctionParameter(paramType = "uint32_t*", paramName = "array"),
            FunctionParameter(paramType = "int", paramName = "array_cnt")
          )
        ),
        body ="free( array );"
      )
    )

    MessageFreeFunction.arrayFieldFreeFunctions(message) shouldBe arrayFreeFunctions
  }

  it should "generate a list of empty array free functions for messages with no array fields" in {
    val message = Message("issue", List(
      Field("title", DynamicStringType, "title"),
      Field("id", NumberType, "number")
    ))

    MessageFreeFunction.arrayFieldFreeFunctions(message) shouldBe Nil
  }
}
