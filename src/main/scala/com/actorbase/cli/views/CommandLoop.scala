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
  do {
    line = reader.readLine()
    if(line.matches("login .*")) //checks if the users tried to type the login command
      if(line.matches("login [a-zA-Z]{4,}")) { //checks if login command is used correctly
        line += " " + reader.readLine(">> password: ", '*')
        reader.setPrompt(prompt.getPrompt)
      }
      else //login command is used incorrectly
        line = "" // resets line so that the match fails
    loop = grammarParser.parseInput(line)
    out.flush
  }  while(line != null && loop)
    reader.getHistory.asInstanceOf[FileHistory].flush()
}
