package cli.controllers

import cli.models._
import cli.views._

import scala.util.parsing.combinator.JavaTokenParsers

class GrammarParser extends JavaTokenParsers {

  val cl = new CommandLauncher
  val view = new View
  cl.attach(view)

  // base arguments types
  def types : Parser[String] = """Integer|Double|String|Binary""".r
  def value : Parser[String] = """".*"""".r
  def string : Parser[String] = """.*""".r
  def list : Parser[String] = """\S+,\s*\S+""".r
  def key : Parser[String] = """\S*""".r

  // chained commands
  def insertCommand : Parser[String] = "insert" ~ key ~ types ~ value ~ "to" ~ string ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 ~ args_3 ~ cmd_part_2 ~ args_4 => cl.storeAndExecute(new InsertCommand(new Operations(
      Map[Any, Any]("key" -> args_1, "type" -> args_2, "value" -> args_3, cmd_part_2 -> args_4))))
  }

  def exportCommand : Parser[String] = "export" ~ (list | key) ~ "to" ~ string ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new ExportCommand(new Operations(Map[Any, Any]("p_list" -> args_1, "f_path" -> args_2)))
      cl.storeAndExecute(exp)
    }
  }

  def loginCommand : Parser[String] = "login" ~ (key) ~ key ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 => {
      val exp = new LoginCommand(new Operations(Map[Any, Any]("username" -> args_1, "password" -> args_2)))
      cl.storeAndExecute(exp)
    }
  }

  def commandList = rep(insertCommand | exportCommand)
}
