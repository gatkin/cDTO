package codegen.types

import datamodel._
import dto.UnitSpec
import codegen.functions._

class MessageInitFunctionSpec extends UnitSpec {

  "Init function definition" should "create an initialization function for a cDTO message" in {

    val message = Message("issue", List(
      Field("number", AliasedType("uint32_t", NumberType), "issueNumber"),
      Field("url", DynamicStringType, "url"),
      Field("creator", ObjectType("user"), "user")
    ))

    val expectedDefinition = FunctionDefinition(
      name = "issue_init",
      documentation = FunctionDocumentation(shortSummary = "Initialize a issue object", description = "Zeros out the provided issue."),
      prototype = FunctionPrototype(isStatic = false, returnType = "void", parameters = List(
        FunctionParameter(paramType = "issue*", paramName = "obj")
      )),
      body = "memset( obj, 0, sizeof( *obj ) );"
    )

    MessageInitFunction(message) shouldBe expectedDefinition
  }

}
