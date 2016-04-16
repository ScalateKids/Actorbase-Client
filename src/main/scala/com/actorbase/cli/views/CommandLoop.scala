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
  reader.addCompleter(new StringsCompleter("export", "insert", "login", "addContributor", "find")) //autocompleted commands

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
            case username if m.group(2).isEmpty => line += " " + reader.readLine(">> username: ")
            case nousername if !m.group(2).isEmpty =>
          }
        }
        line += " " + reader.readLine(">> password: ", '*')
        reader.setPrompt(prompt.getPrompt)
        loop = grammarParser.parseInput(line)
      }
      case quit if quit.matches("(quit|exit)\\s*") => loop = false
      case _ => loop = grammarParser.parseInput(line)
    }
    out.flush
  }  while(line != null && loop)
    // reader.getHistory.asInstanceOf[FileHistory].flush()
}
