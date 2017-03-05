package codegen.messagetypes

import codegen.functions._
import datamodel._
import dto.UnitSpec

class ArrayFieldFreeFunctionSpec extends UnitSpec {

  "Array field free function" should "generate a free function for arrays with dynamic elements" in {
    val messageName = "issue"
    val fieldName = "urls"
    val arrayField = ArrayType(DynamicStringType)

    val arrayFreeFunction = FunctionDefinition(
      name = "issue_urls_free",
      documentation = FunctionDocumentation(shortSummary = "Free array",
        description = "Cleans up all resources owned by the array and its elements."),
      prototype = FunctionPrototype(
        isStatic = true,
        returnType = "void",
        parameters = List(
          FunctionParameter(paramType = "char**", paramName = "array"),
          FunctionParameter(paramType = "int", paramName = "array_cnt")
        )
      ),
      body =
        """int i;
          |
          |for( i = 0; i < array_cnt; i++ )
          |    {
          |    free( array[i] );
          |    }
          |
          |free( array );""".stripMargin
    )

    ArrayFieldFreeFunction(messageName, fieldName, arrayField) shouldBe arrayFreeFunction
  }

  it should "generate a free function for arrays with non-dynamic elements" in {
    val messageName = "issue"
    val fieldName = "ids"
    val arrayField = ArrayType(AliasedType("uint32_t", NumberType))

    val arrayFreeFunction = FunctionDefinition(
      name = "issue_ids_free",
      documentation = FunctionDocumentation(shortSummary = "Free array",
        description = "Cleans up all resources owned by the array and its elements."),
      prototype = FunctionPrototype(
        isStatic = true,
        returnType = "void",
        parameters = List(
          FunctionParameter(paramType = "uint32_t*", paramName = "array"),
          FunctionParameter(paramType = "int", paramName = "array_cnt")
        )
      ),
      body = "free( array );"
    )

    ArrayFieldFreeFunction(messageName, fieldName, arrayField) shouldBe arrayFreeFunction
  }

  it should "generate a free function for arrays with object-type elements" in {
    val messageName = "issue"
    val fieldName = "assignees"
    val arrayField = ArrayType(ObjectType("user"))

    val arrayFreeFunction = FunctionDefinition(
      name = "issue_assignees_free",
      documentation = FunctionDocumentation(shortSummary = "Free array",
        description = "Cleans up all resources owned by the array and its elements."),
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
          |    {
          |    user_free( &array[i] );
          |    }
          |
          |free( array );""".stripMargin
    )

    ArrayFieldFreeFunction(messageName, fieldName, arrayField) shouldBe arrayFreeFunction
  }

  it should "generate a free function for arrays of fixed-length strings" in {
    // Fixed-length strings will need to be dynamically allocated when they
    // are elements of an array so we need to call free on each element when
    // they are fixed-length strings.
    val messageName = "issue"
    val fieldName = "label_colors"
    val arrayField = ArrayType(FixedStringType(6))

    val arrayFreeFunction = FunctionDefinition(
      name = "issue_label_colors_free",
      documentation = FunctionDocumentation(shortSummary = "Free array",
        description = "Cleans up all resources owned by the array and its elements."),
      prototype = FunctionPrototype(
        isStatic = true,
        returnType = "void",
        parameters = List(
          FunctionParameter(paramType = "char**", paramName = "array"),
          FunctionParameter(paramType = "int", paramName = "array_cnt")
        )
      ),
      body =
        """int i;
          |
          |for( i = 0; i < array_cnt; i++ )
          |    {
          |    free( array[i] );
          |    }
          |
          |free( array );""".stripMargin
    )

    ArrayFieldFreeFunction(messageName, fieldName, arrayField) shouldBe arrayFreeFunction
  }
}
