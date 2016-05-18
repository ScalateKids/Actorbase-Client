name := "Actorbase-CLI"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
  "org.scala-lang" % "jline" % "2.11.0-M3",
  "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "io.spray" %%  "spray-json" % "1.3.2")

initialCommands in console += """
import com.actorbase.driver.ActorbaseDriver
import com.actorbase.driver.client.api._
import com.actorbase.driver.data._
"""
