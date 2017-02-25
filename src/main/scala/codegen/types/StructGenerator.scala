package codegen.types

import codegen.Constants

object StructGenerator {

  private val tabWidth = Constants.indentation.length

  /**
    * Generates a string to declare a C struct
    * @param definition Definition of the struct to generate
    * @return String to declare the C struct
    */
  def apply(definition: StructDefinition): String = {
    s"""typedef struct
       |${Constants.indentation}{
       |${fieldDeclarations(definition.fields)}
       |${Constants.indentation}} ${definition.name};""".stripMargin
  }

  /**
    * Gets a formatted string to declare a fields of a struct. The names
    * of all fields in the struct will be aligned to the same column for
    * improved readability
    * @param fields List of fields contained in the struct
    * @return Formatted string to declare fields of a struct
    */
  private def fieldDeclarations(fields: Seq[StructField]): String = {
    val fieldNameColumn = fieldNameColumnNumber(fields)

    val fieldDeclarations = fields.map(field => {
      val declaration = fieldDeclaration(field, fieldNameColumn)
      s"${Constants.indentation}$declaration;"
    })

    fieldDeclarations.mkString("\n")
  }

  /**
    * In order to format the struct field declarations so that all field names are aligned,
    * this function determines the column number at which to declare the name of the struct
    * fields.
    * @param fields List of fields contained in the struct
    * @return Column number at which to declare the struct field names
    */
  private def fieldNameColumnNumber(fields: Seq[StructField]): Int = {
    // Determine the maximum width of the longest type declaration of all fields
    // in the struct
    val typeDeclarations = fields.map({
      case FixedArrayStructField(_, elementTypeDeclaration, _) => elementTypeDeclaration
      case SimpleStructField(_, typeDeclaration) => typeDeclaration
    })

    // Simple add an amount of spacing equal to the width of the indentation
    // to declare the field name
    val maxFieldTypeWidth = typeDeclarations.map(_.length).max
    maxFieldTypeWidth + tabWidth
  }

  /**
    * Gets the formatted string to declare the given struct field
    * @param field Field to declare
    * @param fieldNameColumn Column number at which to declare the struct field names
    * @return Formatted string to declare the given struct field within the struct
    */
  private def fieldDeclaration(field: StructField, fieldNameColumn: Int): String = {
    field match {
      case array @ FixedArrayStructField(_, _, _) => fixedArrayFieldDeclaration(array, fieldNameColumn)
      case simpleField @ SimpleStructField(_, _) => simpleStructFieldDeclaration(simpleField, fieldNameColumn)
    }
  }

  /**
    * Gets the string to declare a field of a struct that is a fixed-length array of some
    * type, e.g. 'char identifier[ 33 ];'
    * @param field - Fixed array field to declare
    * @param fieldNameColumn Column number at which to declare the struct field names
    * @return String to declare a fixed-length array field
    */
  private def fixedArrayFieldDeclaration(field: FixedArrayStructField, fieldNameColumn: Int): String = {
    val spacing = fieldTypeNameSpacing(field.elementTypeDeclaration, fieldNameColumn)

    s"${field.elementTypeDeclaration}$spacing${field.name}[ ${field.maxSize} ]"
  }

  /**
    * Gets the spacing to place between a field's type declaration and a fields name
    * to ensure that the names of all struct fields are aligned.
    * @param fieldType Type declaration of the field
    * @param fieldNameColumn Column number at which to declare the struct field names
    * @return
    */
  private def fieldTypeNameSpacing(fieldType: String, fieldNameColumn: Int): String = {
    require(fieldNameColumn > fieldType.length)
    " " * (fieldNameColumn - fieldType.length)
  }

  /**
    * Gets the string to declare a simple field that is not a fixed length array,
    * e.g. 'char* url;'
    * @param field Simple struct field to declare
    * @param fieldNameColumn Column number at which to declare the struct field names
    * @return String to declare a simple struct field
    */
  private def simpleStructFieldDeclaration(field: SimpleStructField, fieldNameColumn: Int): String = {
    val spacing = fieldTypeNameSpacing(field.typeDeclaration, fieldNameColumn)

    s"${field.typeDeclaration}$spacing${field.name}"
  }
}
