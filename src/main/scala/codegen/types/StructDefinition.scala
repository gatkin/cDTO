package codegen.types

/**
  * Contains information necessary to define a C struct
  * @param name Name of struct
  * @param fields List of fileds contained in the struct
  */
case class StructDefinition(name: String, fields: Seq[StructField])

/**
  * Contains the information necessary to declare a field within a struct
  */
sealed trait StructField
case class FixedArrayStructField(name: String, elementTypeDeclaration: String, maxSize: Int) extends StructField
case class SimpleStructField(name: String, typeDeclaration: String) extends StructField


