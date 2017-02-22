package codegen.functions

import dto.UnitSpec

class FunctionGeneratorSpec extends UnitSpec {

  "Function generator" should "generate a function with many arguments" in {

    val functionDefinition = FunctionDefinition(name = "issue_set_assignees",
      documentation = FunctionDocumentation("Set issue's assignees", "Sets the provided list of users as the issue's assignees"),
      prototype = FunctionPrototype(isStatic = false, returnType = "void", parameters = List(
        FunctionParameter(paramType = "issue*", paramName = "issue"),
        FunctionParameter(paramType = "user const*", paramName = "assignees"),
        FunctionParameter(paramType = "int", paramName = "assignees_cnt")
      )),
      body =
        """issue->assignees = assignees;
          |issue->assignees_cnt = assignees_cnt;
        """.stripMargin
    )

    val expectedFunction = CFunction(
      declaration =
        """void issue_set_assignees
          |    (
          |    issue* issue,
          |    user const* assignees,
          |    int assignees_cnt
          |    );""".stripMargin,
      implementation =
        """/**************************************************
          |*
          |*    issue_set_assignees - Set issue's assignees
          |*
          |*    Sets the provided list of users as the issue's assignees
          |*
          |**************************************************/
          |void issue_set_assignees
          |    (
          |    issue* issue,
          |    user const* assignees,
          |    int assignees_cnt
          |    )
          |{
          |issue->assignees = assignees;
          |issue->assignees_cnt = assignees_cnt;
          |}    /* issue_set_assignees()    */""".stripMargin
    )

    FunctionGenerator(functionDefinition) shouldBe expectedFunction
  }

  it should "generate a function with a single argument" in {

    val functionDefinition = FunctionDefinition(name = "add_one",
      documentation = FunctionDocumentation("Add one", "Adds one to the given number"),
      prototype = FunctionPrototype(isStatic = false, returnType = "int", parameters = List(
        FunctionParameter(paramType = "int", paramName = "value")
      )),
      body = "return value + 1;"
    )

    val expectedFunction = CFunction(
      declaration =
        """int add_one
          |    (
          |    int value
          |    );""".stripMargin,
      implementation =
        """/**************************************************
          |*
          |*    add_one - Add one
          |*
          |*    Adds one to the given number
          |*
          |**************************************************/
          |int add_one
          |    (
          |    int value
          |    )
          |{
          |return value + 1;
          |}    /* add_one()    */""".stripMargin
    )

    FunctionGenerator(functionDefinition) shouldBe expectedFunction
  }

  it should "generate a function with no arguments" in {

    val functionDefinition = FunctionDefinition(name = "say_hello",
      documentation = FunctionDocumentation("Say hello", "Prints a greeting"),
      prototype = FunctionPrototype(isStatic = false, returnType = "void", parameters = List()),
      body = """printf( "Hello, world!\n" );"""
    )

    val expectedFunction = CFunction(
      declaration =
        """void say_hello
          |    (
          |    void
          |    );""".stripMargin,
      implementation =
        """/**************************************************
          |*
          |*    say_hello - Say hello
          |*
          |*    Prints a greeting
          |*
          |**************************************************/
          |void say_hello
          |    (
          |    void
          |    )
          |{
          |printf( "Hello, world!\n" );
          |}    /* say_hello()    */""".stripMargin
    )

    FunctionGenerator(functionDefinition) shouldBe expectedFunction
  }

  it should "generate a static function" in {

    val functionDefinition = FunctionDefinition(name = "add_values",
      documentation = FunctionDocumentation("Add values", "Adds two values together"),
      prototype = FunctionPrototype(isStatic = true, returnType = "int", parameters = List(
        FunctionParameter(paramType = "int", paramName = "value_a"),
        FunctionParameter(paramType = "int", paramName = "value_b")
      )),
      body = "return value_a + value_b;"
    )

    val expectedFunction = CFunction(
      declaration =
        """static int add_values
          |    (
          |    int value_a,
          |    int value_b
          |    );""".stripMargin,
      implementation =
        """/**************************************************
          |*
          |*    add_values - Add values
          |*
          |*    Adds two values together
          |*
          |**************************************************/
          |static int add_values
          |    (
          |    int value_a,
          |    int value_b
          |    )
          |{
          |return value_a + value_b;
          |}    /* add_values()    */""".stripMargin
    )

    FunctionGenerator(functionDefinition) shouldBe expectedFunction
  }
}
