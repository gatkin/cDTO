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
  val defaultCharacterCType = "char"
  val defaultIntCType = "int"
  val defaultNumberCType = "double"

  val voidCType = "void"

  val defaultFreeFunction = "free"

  val stdlibHeader = "<stdlib.h>"
  val stringHeader = "<string.h>"
  val cJSONHeader = """"cJSON.h""""
}
