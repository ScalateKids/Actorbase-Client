package com.actorbase.cli.controllers

import com.actorbase.cli.models._
import com.actorbase.cli.views.ResultView

import scala.util.parsing.combinator._

class GrammarParser(commandInvoker: CommandInvoker, view: ResultView) extends JavaTokenParsers with Observable {

  // base arguments types
  def types : Parser[String] = """Integer|Double|String|Binary""".r
  def value : Parser[String] = """".*"""".r
  def string : Parser[String] = """.*""".r
  def list : Parser[String] = """\S+,\s*\S+""".r
  // def list : Parser[Any] = repsep(stringLiteral, ",")
  def key : Parser[String] = """\S*""".r

  // chained commands

  def insertItemCommand : Parser[String] = "insert " ~ key ~ types ~ value ~ "to " ~ string ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 ~ args_3 ~ cmd_part_2 ~ args_4 => commandInvoker.storeAndExecute(new InsertItemCommand(
      new CommandReceiver(Map[Any, Any]("key " -> args_1, "type" -> args_2, "value" -> args_3, cmd_part_2 -> args_4))))
  }

  def exportCommand : Parser[String] = "export " ~ (list | key) ~ "to " ~ string ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new ExportCommand(new CommandReceiver(Map[Any, Any]("p_list" -> args_1, "f_path" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def loginCommand : Parser[String] = "login " ~ key ~ string ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 => {
      val exp = new LoginCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1, "password" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def addContributorCommand : Parser[String] = "addContributor " ~ key ~ "to " ~ key ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new AddContributorCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1, "collection" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  /**
    * TODO: needs improvement
    * probably splitted into sub commands
    */

  def findCommand : Parser[String] = "find " ~ key.? ~ "from ".? ~ (list | key).? ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => { //searches the key in the listed collections
      val exp = new FindCommand(new CommandReceiver(Map[Any, Any]("key" -> args_1, "collection" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
    case cmd_part_1 ~ args_1 => { //search key in whole database
      val exp = new FindCommand(new CommandReceiver(Map[Any, Any]("key" -> args_1)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def commandList = rep(insertItemCommand | exportCommand | loginCommand | addContributorCommand | findCommand)

  /**
    * Parse CommandLoop input line, sets state on observable view
    * and notify them
    */
  def parseInput(line: String) : Boolean = {
    val os = System.getProperty("os.name")
    var status : Boolean = true
    if(line == "quit" || line == "exit")
      status = false
    parseAll(commandList, line) match {
      case Success(matched, _) => {
        setState("")
      }
      case Failure(msg, _) => {
        os match {
          case linux if linux.contains("Linux") => setState(s"\u001B[33mFAILURE:\u001B[0m $msg") // handle with exceptions etc..
          case windows if windows.contains("Windows") => setState(s"\u001B[33mFAILURE:\u001B[1m $msg") // handle with exceptions etc..
          case mac if mac.contains("Darwin") => setState(s"\u001B[33mFAILURE:\u001B[1m $msg") // handle with exceptions etc..
          case _ => setState(s"FAILURE: $msg")
        }
      }
      case Error(msg, _) => setState(s"ERROR: $msg") // handle with exceptions etc..
    }
    notifyAllObservers()
    return status
  }
}
