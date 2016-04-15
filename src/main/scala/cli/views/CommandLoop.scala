package cli.views

import scala.tools.jline.console.ConsoleReader
import scala.tools.jline.console.history.FileHistory
import scala.tools.jline.console.completer._

import java.io._
import java.util._

import cli.controllers._
import cli.models._

object ParserDSLTest extends ParserDSL with App {

  var loop = true
  val reader : ConsoleReader = new ConsoleReader()
  val history = new FileHistory(new File(".history"))
  val os = System.getProperty("os.name")

  reader.setHistory(history)
  reader.setPrompt("actorbasecli@" + os.toLowerCase + "$ ")
  reader.setBellEnabled(false)
  reader.addCompleter(new StringsCompleter("export", "insert"))

  var line : String = ""
  val out : PrintWriter = new PrintWriter(reader.getTerminal().wrapOutIfNeeded(System.out))
  do {
    line = reader.readLine()
    if(line  == "login") {
      line = reader.readLine(">> password: ", '*')
      reader.setPrompt("actorbasecli@" + os.toLowerCase + "$ ")
    }
    parseAll(commandList, line)  match {
    //   case Success(matched, _) => {
    //     if (matched.length > 0)
    //       println(matched.head)
    //   }
      case Success(matched, _) =>
      case Failure(msg, _) => {
        os match {
          case linux if linux.contains("Linux") => println("\u001B[33mFAILURE:\u001B[0m ", msg) // handle with exceptions etc..
          case windows if windows.contains("Windows") => println("\u001B[33mFAILURE:\u001B[1m ", msg) // handle with exceptions etc..
          case mac if mac.contains("Darwin") => println("\u001B[33mFAILURE:\u001B[1m ", msg) // handle with exceptions etc..
          case _ => println("FAILURE: ", msg)
        }
      }
      case Error(msg, _) => println("ERROR: ", msg)     // handle with exceptions etc..
    }
    out.flush
  }  while(line != null && line != "quit" && line != "exit")
    reader.getHistory.asInstanceOf[FileHistory].flush()
}
