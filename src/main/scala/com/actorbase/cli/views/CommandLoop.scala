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

package com.actorbase.cli.views

import com.actorbase.cli.controllers.GrammarParser
import com.actorbase.cli.models._

import scala.tools.jline.console.ConsoleReader
import scala.tools.jline.console.history.FileHistory
import scala.tools.jline.console.completer._

import java.io._

object CommandLoop extends App {

  // status variable, represents do\while condition
  var loop : Boolean = true

  // model ref
  val commandInvoker = new CommandInvoker

  // view ref
  val view = new ResultView

  // controller ref
  val grammarParser = new GrammarParser(commandInvoker, view)

  // attach view to observers
  commandInvoker.attach(view)
  grammarParser.attach(view)

  val reader : ConsoleReader = new ConsoleReader()
  val history = new FileHistory(new File(".history"))
  val prompt : PromptProvider = new ActorbasePrompt
  val banner = new ActorbaseBanner
  print(banner.getBanner())

  reader.setHistory(history)
  reader.setPrompt(prompt.getPrompt)
  reader.setBellEnabled(false)
  reader.addCompleter(new StringsCompleter("addCollaborator", "changePassword", "createCollection", "deleteCollection",
    "export", "find", "help", "insert", "listCollections", "login", "logout",
    "removeCollaborator", "removeItem", "renameCollection", "addUser", "removeUser",
    "resetPassword"))                    //autocompleted commands

  var line : String = ""
  val out : PrintWriter = new PrintWriter(reader.getTerminal().wrapOutIfNeeded(System.out))

  // login check regex
  val pattern = """login(\s*)(\w*)""".r

  do {
    line = reader.readLine()

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
        reader.setPrompt(prompt.getPrompt)
        loop = grammarParser.parseInput(line)
      }
      case change if change.matches("changePassword\\s*") => {
        val oldPassword = reader.readLine(">> password: ", '*')
        line += " " + """.*""".r.findFirstIn(oldPassword).get
        val newPassword = reader.readLine(">> new password: ", '*')
        line += " " + """\w*""".r.findFirstIn(newPassword).get
        val repeatPassword = reader.readLine(">> repeat password: ", '*')
        line += " " + """\w*""".r.findFirstIn(repeatPassword).get
        reader.setPrompt(prompt.getPrompt)
        loop = grammarParser.parseInput(line)
      }
      case quit if quit.matches("(quit|exit)\\s*") => loop = false
      case _ => loop = grammarParser.parseInput(line)
    }
    out.flush
  } while(line != null && loop)
    // reader.getHistory.asInstanceOf[FileHistory].flush()
}
