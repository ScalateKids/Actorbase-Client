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
  * @author Scalatekids TODO DA CAMBIARE
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.cli.controllers

import com.actorbase.cli.models._
import com.actorbase.cli.views.ResultView

import scala.tools.jline.console.ConsoleReader
import scala.tools.jline.console.history.FileHistory
import scala.tools.jline.console.completer._

import scala.util.parsing.combinator._

class GrammarParser(commandInvoker: CommandInvoker, view: ResultView) extends JavaTokenParsers with Observable {

  // base arguments types
  val types : Parser[String] = """Integer|Double|String|Binary""".r
  val permissions : Parser[String] = """ReadOnly|ReadWrite""".r
  val quotedString : Parser[String] = """['"].*['"]""".r
  val literalString : Parser[String] = """.*""".r
  val listString : Parser[String] = """[\S+,\s*\S+]+""".r        // only works without spaces for now
  // val listString : Parser[String] = """^[-\w\s]+(?:,[-\w\s]*)*$""".r
  // val listString : Parser[Any] = repsep(stringLiteral, ",")
  val keyString : Parser[String] = """\S*""".r
  //val nothing : Parser[String] = """""" // ???????

  // chained commands

  def loginCommand : Parser[String] = "login " ~ keyString ~ literalString ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 => {
      val exp = new LoginCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1, "password" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def logoutCommand : Parser[String] = "logout " ^^ {
    case cmd_part_1 => {
      val exp = new LogoutCommand(new CommandReceiver(Map[Any, Any]("logout" -> None)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def changePasswordCommand : Parser[String] = "changePassword " ~ keyString ~ keyString ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 ~ args_3 => {
      val exp = new ChangePasswordCommand(new CommandReceiver(Map[Any, Any]("oldPsw" -> args_1, "newPsw" -> args_2, "repeatedPsw" -> args_3)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  // ugly as hell, needs improvements
  def helpCommand : Parser[String] = "help" ~ keyString.? ^^ {
    case cmd_part_1 => {
      val exp = new HelpCommand(new CommandReceiver(Map[Any, Any]("key" -> "key")))
      commandInvoker.storeAndExecute(exp)
    }
  }

  /********************************************************************************************************************/
  /**                                          COLLECTION OPERATIONS                                                 **/
  /********************************************************************************************************************/

  def createCollectionCommand : Parser[String] = "createCollection " ~ literalString ^^ {
    case cmd_part_1 ~ args_1 => {
      val exp = new CreateCollectionCommand(new CommandReceiver(Map[Any, Any]("name" -> args_1)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def listCollectionsCommand : Parser[String] = "listCollections" ^^ {
    case cmd_part_1 => {
      val exp = new CreateCollectionCommand(new CommandReceiver(Map[Any, Any]("list" -> None)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  // key? o String?
  def renameCollectionCommand : Parser[String] = "renameCollection " ~ keyString ~ "to " ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new RenameCollectionCommand(new CommandReceiver(Map[Any, Any]("oldName" -> args_1, "newName" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def deleteCollectionCommand : Parser[String] = "deleteCollection " ~ quotedString ^^ {
    case cmd_part_1 ~ args_1 => {
      val exp = new DeleteCollectionCommand(new CommandReceiver(Map[Any, Any]("Collection" -> args_1)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def addCollaboratorCommand : Parser[String] = "addCollaborator " ~ keyString ~ "to " ~ keyString ~ permissions ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 ~ args_3 => {
      val exp = new AddCollaboratorCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1, "collection" -> args_2, "permissions" -> args_3)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def removeCollaboratorCommand : Parser[String] = "removeCollaborator" ~ keyString ~ "from " ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new RemoveCollaboratorCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1, "collection" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def exportCommand : Parser[String] = "export " ~ (keyString | listString) ~ "to " ~ literalString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new ExportCommand(new CommandReceiver(Map[Any, Any]("p_list" -> args_1.split(",").toList, "f_path" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  /********************************************************************************************************************/
  /**                                               ITEM OPERATIONS                                                  **/
  /********************************************************************************************************************/

  // TODO flag sovrascrittura
  // TODO inserimento item con creazione nuova collezione
  def insertItemCommand : Parser[String] = "insert " ~ keyString ~ types ~ quotedString ~ "to " ~ literalString ^^ {
    case cmd_part_1 ~ args_1 ~ args_2 ~ args_3 ~ cmd_part_2 ~ args_4 => commandInvoker.storeAndExecute(new InsertItemCommand(
      new CommandReceiver(Map[Any, Any]("key " -> args_1, "type" -> args_2, "quotedString" -> args_3, cmd_part_2 -> args_4))))
  }

  // TODO insert item da file?

  def removeItemCommand : Parser[String] = "remove " ~ keyString ~ "from " ~ keyString ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => {
      val exp = new RemoveItemCommand(new CommandReceiver(Map[Any, Any]("key" -> args_1, "collection" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  // TODO: needs improvement, probably splitted into sub commands
  def findCommand : Parser[String] = "find " ~ keyString.? ~ "from ".? ~ (listString | keyString).? ^^ {
    case cmd_part_1 ~ args_1 ~ cmd_part_2 ~ args_2 => { //searches the key in the listed collections
      val exp = new FindCommand(new CommandReceiver(Map[Any, Any]("key" -> args_1, "collection" -> args_2)))
      commandInvoker.storeAndExecute(exp)
    }
    case cmd_part_1 ~ args_1 => { //search key in whole database
      val exp = new FindCommand(new CommandReceiver(Map[Any, Any]("key" -> args_1)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  /********************************************************************************************************************/
  /**                                              USERS MANAGEMENT                                                  **/
  /********************************************************************************************************************/

  def addUserCommand : Parser[String] = "addUser" ~ keyString ^^ {
    case cmd_part_1 ~ args_1 => {
      val exp = new AddUserCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def removeUserCommand : Parser[String] = "removeUser" ~ keyString ^^ {
    case cmd_part_1 ~ args_1 => {
      val exp = new RemoveUserCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def resetPasswordCommand : Parser[String] = "resetPassword" ~ keyString ^^ {
    case cmd_part_1 ~ args_1 => {
      val exp = new ResetPasswordCommand(new CommandReceiver(Map[Any, Any]("username" -> args_1)))
      commandInvoker.storeAndExecute(exp)
    }
  }

  def commandList = rep( insertItemCommand | exportCommand | loginCommand | addCollaboratorCommand | findCommand |
                        helpCommand | logoutCommand | createCollectionCommand | listCollectionsCommand |
                        renameCollectionCommand | deleteCollectionCommand | removeCollaboratorCommand |
                        removeItemCommand | changePasswordCommand|addUserCommand | removeUserCommand |
                        resetPasswordCommand) //all possible commands
  /**
    * Parse CommandLoop input line, sets state on observable view
    * and notify them
    */
  def parseInput(input: String) : Boolean = {
    val os = System.getProperty("os.name")
    var status : Boolean = true
    val reader : ConsoleReader = new ConsoleReader()
    if(input.matches("(quit|exit)\\s*"))
      status = false
    else {
      val pattern = """login(\s*)(\w*)""".r
      var line : String = input
      line match {
        case login if login.matches("login\\s*.*") => {
          pattern.findAllIn(login).matchData foreach {
            m =>
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
        case change if change.matches("changePassword\\s*") => {
          val oldPassword = reader.readLine(">> password: ", '*')
          line += " " + """.*""".r.findFirstIn(oldPassword).get
          val newPassword = reader.readLine(">> new password: ", '*')
          line += " " + """\w*""".r.findFirstIn(newPassword).get
          val repeatPassword = reader.readLine(">> repeat password: ", '*')
          line += " " + """\w*""".r.findFirstIn(repeatPassword).get
        }
        case quit if quit.matches("(quit|exit)\\s*") => status = false
        case _ => line
      }
      parseAll(commandList, line) match {
        case Success(matched, _) => setState("")
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
    }
    notifyAllObservers()
    return status
  }
}
