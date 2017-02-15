package compiler

import datamodel._
import dto.UnitSpec

class ProtocolCompilerSpec extends UnitSpec {
  val protocolName = "TestProtocol"

  val validInput =
    """
      |issue {
      |    number Number jsonKey=issueNumber cType=uint32_t;
      |    url String;
      |    title String;
      |    creator user jsonKey=user;
      |    assignees Array[user];
      |    labels Array[label];
      |    is_open Boolean;
      |}
      |
      |user {
      |    name String jsonKey=login;
      |    url String;
      |}
      |
      |label {
      |    name String;
      |    color String[6];
      |}
    """.stripMargin
  val validProtocol = Protocol(protocolName, List(
    Message("issue", List(
      Field("number", AliasedType("uint32_t", NumberType), "issueNumber"),
      Field("url", DynamicStringType, "url"),
      Field("title", DynamicStringType, "title"),
      Field("creator", ObjectType("user"), "user"),
      Field("assignees", ArrayType(ObjectType("user")), "assignees"),
      Field("labels", ArrayType(ObjectType("label")), "labels"),
      Field("is_open", BooleanType, "is_open")
    )),
    Message("user", List(
      Field("name", DynamicStringType, "login"),
      Field("url", DynamicStringType, "url")
    )),
    Message("label", List(
      Field("name", DynamicStringType, "name"),
      Field("color", FixedStringType(6), "color")
    ))
  ))

  val invalidSyntaxInput =
    """
      |{
      |    number Number;
      |    url String;
      |}
      |
      |user {
      |    name String jsonKey=login;
      |    url String;
      |}
    """.stripMargin
  val invalidSyntaxError = ParserError(Location(2,1),"""string matching regex `[_a-zA-Z][_a-zA-Z0-9]*' expected but `{' found""")

  val invalidSemanticsInput =
    """
      |user {
      |    name String jsonKey=login;
      |    url String;
      |}
      |
      |user {
      |    name String;
      |    color String[6];
      |}
    """.stripMargin
  val invalidSemanticsError = DuplicateMessagesError(List("user"))


  "Protocol compiler" should "compile valid input" in {
    ProtocolCompiler(validInput, protocolName) shouldBe Right(validProtocol)
  }

  it should "report syntax errors" in {
    ProtocolCompiler(invalidSyntaxInput, protocolName) shouldBe Left(invalidSyntaxError)
  }

  it should "report semantic errors" in {
    ProtocolCompiler(invalidSemanticsInput, protocolName) shouldBe Left(invalidSemanticsError)
  }
}
