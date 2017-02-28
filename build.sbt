name := "cdto"

version := "1.0"

scalaVersion := "2.12.1"

mainClass in run := Some("cdto.Main")

libraryDependencies ++= Seq(
  "org.scala-lang.modules"  %% "scala-parser-combinators" % "1.0.5",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.rogach" %% "scallop" % "2.1.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
    