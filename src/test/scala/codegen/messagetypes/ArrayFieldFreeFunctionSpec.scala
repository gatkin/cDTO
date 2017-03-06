package codegen.messagetypes

import codegen.functions._
import datamodel._
import dto.UnitSpec

class ArrayFieldFreeFunctionSpec extends UnitSpec {

  "Array field free function" should "generate a free function for arrays with dynamic elements" in {
    val arrayFreeFunction = FunctionDefinition(
      name = "string_array_free",
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

    ArrayFieldFreeFunction(DynamicStringType) shouldBe arrayFreeFunction
  }

  it should "generate a free function for arrays with non-dynamic elements" in {
    val arrayFreeFunction = FunctionDefinition(
      name = "number_array_free",
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

    ArrayFieldFreeFunction(AliasedType("uint32_t", NumberType)) shouldBe arrayFreeFunction
  }

  it should "generate a free function for arrays with object-type elements" in {
    val arrayFreeFunction = FunctionDefinition(
      name = "user_array_free",
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

    ArrayFieldFreeFunction(ObjectType("user")) shouldBe arrayFreeFunction
  }

  it should "generate a free function for arrays of fixed-length strings" in {
    // Fixed-length strings will need to be dynamically allocated when they
    // are elements of an array so we need to call free on each element when
    // they are fixed-length strings.
    val arrayFreeFunction = FunctionDefinition(
      name = "string_array_free",
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

    ArrayFieldFreeFunction(FixedStringType(6)) shouldBe arrayFreeFunction
  }
}
