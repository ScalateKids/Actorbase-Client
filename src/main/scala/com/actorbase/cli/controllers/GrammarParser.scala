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
  * @author Scalatekids 
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.cli.controllers

import com.actorbase.cli.models._
import com.actorbase.cli.views.ResultView

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
class GrammarParser(commandInvoker: CommandInvoker,
  view: ResultView,
  hostname: String,
  port: Int,
  private var username: String = "admin",
  private var password: String = "Actorb4se") extends JavaTokenParsers with Observable {

  // base arguments types
  val types : Parser[String] = """Integer|Double|String|Binary""".r
  val permissions : Parser[String] = """ReadOnly|ReadWrite""".r
  val quotedString : Parser[String] = """['"].*['"]""".r
  val literalString : Parser[String] = """.*""".r
  val listString : Parser[String] = """[\S+,\s*\S+]+""".r
  val keyString : Parser[String] = """\S+""".r

  // chained commands

  /**
    * Method to parse the login and logout commands.
    *
    * @return a Parser[Command] representing the login or logout command based on the user input.
    */
  def authManagementCommand : Parser[Command] = ("login" ~ keyString ~ literalString | "logout") ^^ {
    case "logout" => new LogoutCommand(new CommandReceiver(hostname, port, Map[Any, Any]("logout" -> None), username, password))
    case "login" ~ args_1 ~ args_2 => new LoginCommand(new CommandReceiver(hostname, port, Map[Any, Any]("username" -> args_1, "password" -> args_2), username, password))
  }

  /**
    * Method to parse the changePasswordCommand.
    *
    * @return a Parser[Command] representing the ChangePasswordCommand with the right parameters.
    */
  def changePasswordCommand : Parser[Command] = "changePassword" ~ keyString ~ keyString ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 ~ args_3 =>
      new ChangePasswordCommand(new CommandReceiver(hostname, port,Map[Any, Any]("oldPsw" -> args_1, "newPsw" -> args_2, "repeatedPsw" -> args_3), username, password))
  }

  /**
    * Method to parse the help command.
    *
    * @return a Parser[Command] representing the HelpCommand with the right parameters.
    */
  // ugly as hell, needs improvements
  def helpCommand : Parser[Command] = "help" ~> keyString.? ^^ {
    case arg => new HelpCommand(new CommandReceiver(hostname, port, Map[Any, Any]("command" -> arg), username, password))
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
    (("createCollection" | "deleteCollection") ~ literalString |
      "listCollections") ^^ {

      case "createCollection" ~ args_1 => new CreateCollectionCommand(new CommandReceiver(hostname, port,Map[Any, Any]("name" -> args_1), username, password))
      case "deleteCollection" ~ args_1 => new DeleteCollectionCommand(new CommandReceiver(hostname, port,Map[Any, Any]("Collection" -> args_1), username, password))
      case "listCollections" => new ListCollectionsCommand(new CommandReceiver(hostname, port,Map[Any, Any]("list" -> None), username, password))
    }

  /**
    * Method to parse the addCollaboratorCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the AddCollaboratorCommand with the right parameters.
    */
  def addCollaboratorCommand : Parser[Command] = "addCollaborator " ~ keyString ~ "to " ~ keyString ~ permissions ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 ~ args_3 =>
      new AddCollaboratorCommand(new CommandReceiver(hostname, port,Map[Any, Any]("username" -> args_1, "collection" -> args_2, "permissions" -> args_3), username, password))
  }

  /**
    * Method to parse the removeCollaboratorCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the RemoveCollaboratorCommand with the right parameters.
    */
  def removeCollaboratorCommand : Parser[Command] = "removeCollaborator" ~ keyString ~ "from " ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 =>
      new RemoveCollaboratorCommand(new CommandReceiver(hostname, port,Map[Any, Any]("username" -> args_1, "collection" -> args_2), username, password))
  }

  /**
    * Method to parse the exportCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the ExportCommand with the right parameters.
    */
  // only works without spaces for now
  def exportCommand : Parser[Command] = "export " ~ (keyString | listString) ~ "to" ~ literalString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 =>
      new ExportCommand(new CommandReceiver(hostname, port,Map[Any, Any]("p_list" -> args_1.split(",").toList, "f_path" -> args_2), username, password))
  }

  /********************************************************************************************************************/
  /**                                               ITEM OPERATIONS                                                  **/
  /********************************************************************************************************************/

  /**
    * Method to parse the insertItemCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the InsertItemCommand with the right parameters.
    */
  // TODO flag sovrascrittura
  def insertItemCommand : Parser[Command] = "insert" ~ "(" ~ keyString ~ "->" ~ keyString ~ ")" ~ "to" ~ literalString ^^ {
    case "insert" ~ "(" ~ args_1 ~ "->" ~ args_2 ~ ")" ~ "to" ~  args_3 =>
      val value = args_2 match {
        case integer if integer matches("""^\d+$""") => integer.toInt
        case double if double matches("""^\d+\.\d+""") => double.toDouble
        case _ => args_2
      }
      new InsertItemCommand(new CommandReceiver(hostname, port,Map[Any, Any]("key" -> args_1, "value" -> value, "collection" -> args_3), username, password))
  }

  // TODO insert item da file?

  /**
    * Method to parse the removeItemCommand with the parameters passed by the user
    *
    * @return a Parser[Command] representing the RemoveItemCommand with the right parameters.
    */
  def removeItemCommand : Parser[Command] = "remove " ~ keyString ~ "from " ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 =>
      new RemoveItemCommand(new CommandReceiver(hostname, port,Map[Any, Any]("key" -> args_1, "collection" -> args_2), username, password))
  }

  /**
    * Method to parse the findCommand with the parameters passed by the user.
    *
    * @return a Parser[Command] representing the FindCommand with the right parameters.
    */
  // meh
  def findCommand : Parser[Command] = ("find" ~ keyString ~ "from" ~ (listString | keyString) | "find from" ~ (listString | keyString) | "find" ~ keyString | "find" ) ^^ {
    case "find" => new FindCommand(new CommandReceiver(hostname, port,Map[Any, Any](/*"key" -> None, "collection" -> None*/), username, password))
    case "find" ~ args_1 => new FindCommand(new CommandReceiver(hostname, port,Map[Any, Any]("key" -> args_1), username, password))
    case "find from" ~ args_1 => new FindCommand(new CommandReceiver(hostname, port,Map[Any, Any](/*"key" -> None, */"collection" -> args_1.asInstanceOf[String].split(",").toList), username, password))
    case "find" ~ args_1 ~ "from"  ~ args_2 =>
      new FindCommand(new CommandReceiver(hostname, port,Map[Any, Any]("key" -> args_1, "collection" -> args_2.asInstanceOf[String].split(",").toList), username, password))
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
  def userManagementCommand : Parser[Command] = ("addUser" | "removeUser" | "resetPassword") ~ keyString ^^ {
    case "addUser" ~ args_1 => new AddUserCommand(new CommandReceiver(hostname, port,Map[Any, Any]("username" -> args_1), username, password))
    case "removeUser" ~ args_1 => new RemoveUserCommand(new CommandReceiver(hostname, port,Map[Any, Any]("username" -> args_1), username, password))
    case "resetPassword" ~ args_1 => new ResetPasswordCommand(new CommandReceiver(hostname, port,Map[Any, Any]("username" -> args_1), username, password))
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
    changePasswordCommand | userManagementCommand
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
    if(input matches("(quit|exit)\\s*")) {
      commandInvoker.storeAndExecute(new LogoutCommand(new CommandReceiver(hostname, port,Map[Any, Any]("logout" -> None), username, password)))
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
          line += " " + """.*""".r.findFirstIn(oldPassword).get
          val newPassword = reader.readLine(">> new password: ", '*')
          line += " " + """\w*""".r.findFirstIn(newPassword).get
          val repeatPassword = reader.readLine(">> repeat password: ", '*')
          line += " " + """\w*""".r.findFirstIn(repeatPassword).get
        }
        case quit if quit matches("(quit|exit)\\s*") => status = false
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
