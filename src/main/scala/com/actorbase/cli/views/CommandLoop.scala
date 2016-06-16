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

package com.actorbase.cli.views

import com.actorbase.cli.controllers.GrammarParser
import com.actorbase.cli.models._

import com.typesafe.config.ConfigFactory
import scala.tools.jline.console.ConsoleReader
import scala.tools.jline.console.history.FileHistory
import scala.tools.jline.console.completer._

import java.io._
import scala.collection.JavaConversions._

object CommandLoop {

  def main(args: Array[String]) = {
    var hostname = "127.0.0.1"
    var port = 9999
    // val (hostname, port) =
    if (args.nonEmpty) {
      // (args(0), args(1))
      hostname = args(0)
      port = args(1).toInt
    }
    // else {
      // ("127.0.0.1", 9999)
    // }
    // status variable, represents do\while condition
    var loop : Boolean = true

    // model ref
    val commandInvoker = new CommandInvoker

    // view ref
    val view = new ResultView

    // controller ref
    val grammarParser = new GrammarParser(commandInvoker, view, hostname, port)

    // attach view to observers
    commandInvoker.attach(view)
    grammarParser.attach(view)

    val reader = new ConsoleReader()
    val history = new FileHistory(new File(".history"))
    val prompt = new ActorbasePrompt
    val banner = new ActorbaseBanner
    var completers : List[String] = Nil
    print(banner.getBanner())

    reader.setHistory(history)
    reader.setPrompt(prompt.getPrompt)
    reader.setBellEnabled(false)
    ConfigFactory.load("commands.conf").getConfig("commands").entrySet.foreach { entry =>
      completers :+= entry.getKey
    }
    reader.addCompleter(new StringsCompleter(completers))

    val out : PrintWriter = new PrintWriter(reader.getTerminal().wrapOutIfNeeded(System.out))

    while(loop) {
      loop = grammarParser.parseInput(reader.readLine)
      out.flush
    }
    // reader.getHistory.asInstanceOf[FileHistory].flush()
  }
}
