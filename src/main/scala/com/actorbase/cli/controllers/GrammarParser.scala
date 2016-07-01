/**
  * The MIT License (MIT)
  * <p/>
  * Copyright (c) 2016 ScalateKids
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * <p/>
  *
  * @author Scalatekids
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.cli.controllers

import com.actorbase.cli.models._
import com.actorbase.cli.views.ResultView
import com.actorbase.driver.ActorbaseDriver

import scala.tools.jline.console.ConsoleReader
import scala.util.parsing.combinator._

/**
  * GrammarParser class, process input strings sent by the user command line
  * using a CommandInvoker object to send the requests to the models package
  * This class translate the user input strings into model Commands.
  *
  * @param commandInvoker an instance of command invoker for execute all request command on the models package
  * @param view where the result of request will stored after invocation of commandInvoker
  */
class GrammarParser(commandInvoker: CommandInvoker, view: ResultView, driverConnection: ActorbaseDriver) extends JavaTokenParsers with Observable {

  // base arguments types
  val permissions: Parser[String] = """ReadOnly|ReadWrite""".r
  val quotedString: Parser[String] = """".*"""".r
  val literalString: Parser[String] = """.+""".r
  val listString: Parser[String] = """["\S+",\s*"\S+"]+""".r
  val keyString: Parser[String] = """"\S+"""".r

  def strip(s: Any): String = {
    if(s.asInstanceOf[String].takeRight(1) == "\"" && s.asInstanceOf[String].take(1) == "\"") {
      return s.asInstanceOf[String].drop(1).dropRight(1)
    }
    s.asInstanceOf[String]
  }

  // chained commands

  /**
    * Method to parse the login and logout commands.
    *
    * @return a Parser[Command] representing the login or logout command based on the user input.
    */
  def authManagementCommand : Parser[Command] = "login" ~ keyString ~ quotedString ^^ {
    //    case "logout" => new LogoutCommand(new CommandReceiver(Map[String, Any]("logout" -> None), driverConnection))
    case "login" ~ args_1 ~ args_2 => new LoginCommand(new CommandReceiver(Map[String, Any]("username" -> args_1, "password" -> args_2), driverConnection))
  }

  /**
    * Method to parse the changePasswordCommand.
    *
    * @return a Parser[Command] representing the ChangePasswordCommand with the right parameters.
    */
  def changePasswordCommand : Parser[Command] = "changePassword" ~ keyString ~ keyString ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 ~ args_3 =>
      new ChangePasswordCommand(new CommandReceiver(Map[String, Any]("oldPsw" -> strip(args_1), "newPsw" -> strip(args_2), "repeatedPsw" -> strip(args_3)), driverConnection))
  }

  /**
    * Method to parse the help command.
    *
    * @return a Parser[Command] representing the HelpCommand with the right parameters.
    */
  // ugly as hell, needs improvements
  def helpCommand : Parser[Command] = ("help" ~ literalString | "help") ^^ {
    case "help" => new HelpCommand(new CommandReceiver(Map[String, Any](), driverConnection))
    case "help" ~ arg => new HelpCommand(new CommandReceiver(Map[String, Any]("command" -> arg), driverConnection))
  }

  /********************************************************************************************************************/
  /**                                          COLLECTION OPERATIONS                                                 **/
  /********************************************************************************************************************/

  /**
    * Method to parse the collection operations commands.
    * Commands that can be parsed are:
    *  _createCollection
    *  _deleteCollection
    *  _listCollections
    *
    * @return a Parser[Command] representing the right command based on the user input.
    */
  def collectionManagementCommand : Parser[Command] =
    (("createCollection" | "deleteCollection") ~ quotedString |
      "listCollections") ^^ {

      case "createCollection" ~ args_1 => new CreateCollectionCommand(new CommandReceiver(Map[String, Any]("name" -> strip(args_1)), driverConnection))
      case "deleteCollection" ~ args_1 => new DeleteCollectionCommand(new CommandReceiver(Map[String, Any]("collection" -> strip(args_1)), driverConnection))
      case "listCollections" => new ListCollectionsCommand(new CommandReceiver(Map[String, Any]("list" -> None), driverConnection))
    }

  /**
    * Method to parse the addCollaboratorCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the AddCollaboratorCommand with the right parameters.
    */
  def addCollaboratorCommand : Parser[Command] = "addContributor " ~ keyString ~ "to " ~ keyString ~ permissions ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 ~ args_3 =>
      new AddCollaboratorCommand(new CommandReceiver(Map[String, Any]("username" -> strip(args_1), "collection" -> strip(args_2), "permissions" -> strip(args_3)), driverConnection))
  }

  /**
    * Method to parse the removeCollaboratorCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the RemoveCollaboratorCommand with the right parameters.
    */
  def removeCollaboratorCommand : Parser[Command] = "removeContributor" ~ keyString ~ "from " ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 =>
      new RemoveCollaboratorCommand(new CommandReceiver(Map[String, Any]("username" -> strip(args_1), "collection" -> strip(args_2)), driverConnection))
  }

  /**
    * Method to parse the exportCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the ExportCommand with the right parameters.
    */
  // only works without spaces for now
  def exportCommand : Parser[Command] = "export " ~ (keyString | listString) ~ "to" ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 =>
      new ExportCommand(new CommandReceiver(Map[String, Any]("p_list" -> args_1.split(",").toList, "f_path" -> strip(args_2)), driverConnection))
  }

  def importCommand : Parser[Command] = "import " ~ (keyString) ^^ {
    case cmd_part_1 ~ args_1 =>
      new ImportCommand(new CommandReceiver( Map[String, Any]("path" -> strip(args_1)), driverConnection))
  }

  /********************************************************************************************************************/
  /**                                               ITEM OPERATIONS                                                  **/
  /********************************************************************************************************************/

  /**
    * Method to parse the insertItemCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the InsertItemCommand with the right parameters.
    */
  def insertItemCommand : Parser[Command] = {
    ("insert" | "update") ~ "(" ~ keyString ~ "->" ~ keyString ~ ")" ~ "to" ~ keyString ^^ {
      case "insert" ~ "(" ~ args_1 ~ "->" ~ args_2 ~ ")" ~ "to" ~ args_3 =>
        new InsertItemCommand(
          new CommandReceiver(Map[String, Any]("key" -> strip(args_1), "value" -> strip(args_2), "collection" -> strip(args_3), "update" -> false), driverConnection))
      case "update" ~ "(" ~ args_1 ~ "->" ~ args_2 ~ ")" ~ "to" ~ args_3 =>
        new InsertItemCommand(
          new CommandReceiver(Map[String, Any]("key" -> strip(args_1), "value" -> strip(args_2), "collection" -> strip(args_3), "update" -> true), driverConnection))
    }
  }

  /**
    * Method to parse the removeItemCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the RemoveItemCommand with the right parameters.
    */
  def removeItemCommand : Parser[Command] = "remove " ~ keyString ~ "from " ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 =>
      new RemoveItemCommand(new CommandReceiver(Map[String, Any]("key" -> strip(args_1), "collection" -> strip(args_2)), driverConnection))
  }

  /**
    * Method to parse the findCommand with the parameters passed by the user.
    *
    * @return a Parser[Command] representing the FindCommand with the right parameters.
    */
  def findCommand : Parser[Command] = ("find" ~ keyString ~ "from" ~ (listString | keyString) | "find from" ~ (listString | keyString) | "find" ~ keyString | "find" ) ^^ {
    case "find" => new FindCommand(new CommandReceiver(Map[String, Any](), driverConnection))
    case "find" ~ args_1 => new FindCommand(new CommandReceiver(Map[String, Any]("key" -> strip(args_1)), driverConnection))
    case "find from" ~ args_1 => new FindCommand(new CommandReceiver(Map[String, Any]("collection" -> args_1.asInstanceOf[String].split(",").map(x => strip(x)).toList), driverConnection))
    case "find" ~ args_1 ~ "from"  ~ args_2 =>
      new FindCommand(new CommandReceiver(Map[String, Any]("key" -> strip(args_1), "collection" -> args_2.asInstanceOf[String].split(",").map(x => strip(x)).toList), driverConnection))
  }

  /********************************************************************************************************************/
  /**                                              USERS MANAGEMENT                                                  **/
  /********************************************************************************************************************/

  /**
    * Method to parse the user management commands.
    * Commands that can be parsed are:
    *  _addUser
    *  _removeUser
    *  _resetPassword
    *
    * @return a Parser[Command] representing the right command based on the user input.
    */
  def userManagementCommand : Parser[Command] = (("addUser" | "removeUser" | "resetPassword") ~ keyString | "listUsers") ^^ {
    case "addUser" ~ args_1 => new AddUserCommand(new CommandReceiver(Map[String, Any]("username" -> strip(args_1)), driverConnection))
    case "removeUser" ~ args_1 => new RemoveUserCommand(new CommandReceiver(Map[String, Any]("username" -> strip(args_1)), driverConnection))
    case "resetPassword" ~ args_1 => new ResetPasswordCommand(new CommandReceiver(Map[String, Any]("username" -> strip(args_1)), driverConnection))
    case "listUsers" => new ListUsersCommand(new CommandReceiver(Map[String, Any]("" -> ""), driverConnection))
  }

  /**
    * Method that contains all the possible Commands parsable by the GrammarParser.
    * This method is used by parseInput to check if the user input received from
    * CommandLoop correspond to a method listed here.
    *
    * @return a Parser[Command] containing all the parsable commands of the GrammarParser
    */
  def commandList : Parser[Command] = {
    insertItemCommand | exportCommand | authManagementCommand | addCollaboratorCommand | findCommand |
    helpCommand | collectionManagementCommand | removeCollaboratorCommand | removeItemCommand |
    changePasswordCommand | userManagementCommand | importCommand
  }

  /**
    * Parse CommandLoop input line, sets state on observable view and notify them
    * It also check for special cases input such as login or renamePassword, in
    * these cases it prompt for additional input required by these operations;
    * in case of quit or exit it call for an eventual logout and close the loop
    *
    * @param input a String representing the user input on the CLI
    * @return a Boolean representing the loop condition, all commands return
    * true except for keywords 'quit' or 'exit'
    */
  def parseInput(input: String) : Boolean = {
    val os = System.getProperty("os.name")
    var status : Boolean = true
    val reader : ConsoleReader = new ConsoleReader()
    if(input matches("(q|quit|exit|logout)\\s*")) {
      status = false
    }
    else {
      val pattern = """login(\s*)(\w*)""".r
      var line : String = input
      line match {
        case login if login matches("login\\s*.*") => {
          pattern.findAllIn(login).matchData foreach { m =>
            m match {
              case nousername if m.group(2).isEmpty => {
                val user = reader.readLine(">> username: ")
                line += " " + """\w*""".r.findFirstIn(user).get
              }
              case username if !m.group(2).isEmpty => line = "login " + m.group(2)
            }
          }
          line += " " + reader.readLine(">> password: ", '*')
        }
        case change if change matches("changePassword\\s*") => {
          val oldPassword = reader.readLine(">> password: ", '*')
          """\w*""".r.findFirstIn(oldPassword) map (x => line += " " + "\"" + x + "\"")
          val newPassword = reader.readLine(">> new password: ", '*')
          """\w*""".r.findFirstIn(newPassword) map (x => line += " " + "\"" + x + "\"")
          val repeatPassword = reader.readLine(">> repeat password: ", '*')
          """\w*""".r.findFirstIn(repeatPassword) map (x => line += " " + "\"" + x + "\"")
        }
        case quit if quit matches("(q|quit|exit|logout)\\s*") => status = false
        case _ => line
      }
      setState("") // reset controller state
      if(!line.isEmpty) {
        parseAll(commandList, line) match {
          case Success(matched, _) => commandInvoker.storeAndExecute(matched)
          case Failure(msg, _) => {
            os match {
              case linux if linux.contains("Linux") => setState(s"\u001B[33mFAILURE:\u001B[0m $msg")       // DEBUG
              case windows if windows.contains("Windows") => setState(s"\u001B[33mFAILURE:\u001B[1m $msg") // DEBUG
              case mac if mac.contains("Darwin") => setState(s"\u001B[33mFAILURE:\u001B[1m $msg")          // DEBUG
              case _ => setState(s"FAILURE: $msg")
            }
          }
          case Error(msg, _) => setState(s"ERROR: $msg") // DEBUG
        }
      }
      notifyAllObservers()
    }
    return status
  }
}
