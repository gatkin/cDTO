package compiler

import dto.UnitSpec

class ProtocolParserSpec extends UnitSpec {

  "Protocol parser" should "successfully parse a valid protocol definition" in {
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

    val successfulAST = ProtocolAST(List(
      MessageDefinition("issue", List(
        FieldDefinition("number", NumberTypeDefinition(), List(JSONKeyAttribute("issueNumber"), CTypeAttribute("uint32_t"))),
        FieldDefinition("url", DynamicStringTypeDefinition(), List()),
        FieldDefinition("title", DynamicStringTypeDefinition(), List()),
        FieldDefinition("creator", ObjectTypeDefinition("user"), List(JSONKeyAttribute("user"))),
        FieldDefinition("assignees", ArrayTypeDefinition(ObjectTypeDefinition("user")), List()),
        FieldDefinition("labels", ArrayTypeDefinition(ObjectTypeDefinition("label")), List()),
        FieldDefinition("is_open", BooleanTypeDefinition(), List())
      )),
      MessageDefinition("user", List(
        FieldDefinition("name", DynamicStringTypeDefinition(), List(JSONKeyAttribute("login"))),
        FieldDefinition("url", DynamicStringTypeDefinition(), List())
      )),
      MessageDefinition("label", List(
        FieldDefinition("name", DynamicStringTypeDefinition(), List()),
        FieldDefinition("color", FixedStringTypeDefinition(6), List())
      ))
    ))

    ProtocolParser(validInput) shouldBe Right(successfulAST)
  }

  it should "successfully parse arrays of fixed-length strings" in {
    val arrayOfFixedStrings =
      """
        | user {
        |   ids Array[String[32]];
        | }
      """.stripMargin

    val arrayOfFixedStringsAST = ProtocolAST(List(
      MessageDefinition("user", List(
        FieldDefinition("ids", ArrayTypeDefinition(FixedStringTypeDefinition(32)), List())
      ))
    ))

    ProtocolParser(arrayOfFixedStrings) shouldBe Right(arrayOfFixedStringsAST)
  }

  it should "fail to parse when a message id is missing" in {
    val noMessageId =
      """
        |{
        | name String;
        | id Number;
        |}
      """.stripMargin

    val noMessageIdError = ParserError(Location(2,1), """string matching regex `[_a-zA-Z][_a-zA-Z0-9]*' expected but `{' found""")

    ProtocolParser(noMessageId) shouldBe Left(noMessageIdError)
  }

  it should "fail to parse when no message fields are provided" in {
    val noMessageFields =
      """
        |user {}
      """.stripMargin

    val noMessageFieldsError = ParserError(Location(2,7), """string matching regex `[_a-zA-Z][_a-zA-Z0-9]*' expected but `}' found""")

    ProtocolParser(noMessageFields) shouldBe Left(noMessageFieldsError)
  }

  it should "fail to parse when an invalid attribute is specified" in {
    val invalidAttribute =
      """
        |user {
        | name String maxLength=32;
        |}
      """.stripMargin

    val invalidAttributeError = ParserError(Location(3, 14), """`;' expected but `m' found""")

    ProtocolParser(invalidAttribute) shouldBe Left(invalidAttributeError)
  }

  it should "fail to parse when a field type is not specified" in {
    val missingFieldType =
      """
        |user {
        | name jsonKey=login;
        |}
      """.stripMargin

    val missingFieldTypeError = ParserError(Location(3,14), """`;' expected but `=' found""")

    ProtocolParser(missingFieldType) shouldBe Left(missingFieldTypeError)
  }

  it should "fail to parse when a message definition is incomplete" in {
    val incompleteMessage =
      """
        |user {
        | name String;
        | id Number;
        | friends Array[user];
      """.stripMargin

    val incompleteMessageError = ParserError(Location(6, 7), """`}' expected but end of source found""")

    ProtocolParser(incompleteMessage) shouldBe Left(incompleteMessageError)
  }

  it should "fail to parse a nested array" in {
    val nestedArray =
      """
        |message {
        | convolutedField Array[Array[String]];
        |}
      """.stripMargin

    val nestedArrayError = ParserError(Location(3,29),"`]' expected but `[' found")

    ProtocolParser(nestedArray) shouldBe Left(nestedArrayError)
  }
}
