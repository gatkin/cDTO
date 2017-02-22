package codegen

/**
  * Created by gatkin on 2/20/17.
  */
object Constants {
  /*
  * Default C types used for simple cDTO types. In the future, this
  * can be made configurable.
  */
  val defaultBooleanCType = "int"
  val defaultNumberCType = "double"
  val defaultCharacterCType = "char"

  val voidCType = "void"

  /*
    * Using spaces instead of tabs for indentation, can make this configurable
    * in the future.
    */
  val indentation = "    "
}
