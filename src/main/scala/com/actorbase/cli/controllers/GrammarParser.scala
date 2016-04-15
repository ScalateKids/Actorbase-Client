package com.actorbase.cli.controllers

import com.actorbase.cli.models._
import com.actorbase.cli.views.ResultView

import scala.util.parsing.combinator.JavaTokenParsers

class GrammarParser extends JavaTokenParsers {

  val cl = new CommandInvoker
  val view = new ResultView
  cl.attach(view)

  // base arguments types
  def types : Parser[String] = """Integer|Double|String|Binary""".r
  def value : Parser[String] = """".*"""".r
  def string : Parser[String] = """.*""".r
  def list : Parser[String] = """\S+,\s*\S+""".r
  def key : Parser[String] = """\S*""".r

  // chained commands

  def insertItemCommand : Parser[String] = "insert " ~ key ~ types ~ value ~ "to " ~ string ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 ~ args_3 ~ cmd_part_2 ~ args_4 => cl.storeAndExecute(new InsertItemCommand(
      new CommandReceiver(Map[Any, Any]("key " -> args_1, "type" -> args_2, "value" -> args_3, cmd_part_2 -> args_4))))
  }

  def exportCommand : Parser[String] = "export " ~ (list | key) ~ "to " ~ string ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new ExportCommand(new CommandReceiver(Map[Any, Any]("p_list" -> args_1, "f_path" -> args_2)))
      cl.storeAndExecute(exp)
    }
  }

  def loginCommand : Parser[String] = "login " ~ key ~ string ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 => {
      val exp = new LoginCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1, "password" -> args_2)))
      cl.storeAndExecute(exp)
    }
  }

  def addContributorCommand : Parser[String] = "addContributor " ~ key ~ "to " ~ key ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new AddContributorCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1, "collection" -> args_2)))
      cl.storeAndExecute(exp)
    }
  }

  def findCommand : Parser[String] = "find " ~ key ~ "from " ~ (list | key) ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => { //searches the key in the listed collections
      val exp = new FindCommand(new CommandReceiver(Map[Any, Any]("key" -> args_1, "collection" -> args_2)))
      cl.storeAndExecute(exp)
    }
    case cmd_part_1 ~ args_1 ~ cmd_part_2  => { //search key in whole database
      val exp = new FindCommand(new CommandReceiver(Map[Any, Any]("key" -> args_1)))
      cl.storeAndExecute(exp)
    }
    case cmd_part_1 ~ cmd_part_2 ~ args_2  => {//returns all the content of the listed collections unreachable at the moment
      val exp = new FindCommand(new CommandReceiver(Map[Any, Any]("collections" -> args_2)))
      cl.storeAndExecute(exp)
    }
    case cmd_part_1 => { //returns all the content of the database, unreachable at the moment
    val exp = new FindCommand(new CommandReceiver(Map[Any, Any]("whole database" -> "lol")))
    cl.storeAndExecute(exp)
    }
  }

  def commandList = rep(insertItemCommand | exportCommand | loginCommand | addContributorCommand | findCommand)
}
