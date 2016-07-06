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

import com.actorbase.cli.controllers.DriverConnection
import com.actorbase.cli.controllers.GrammarParser
import com.actorbase.cli.models._

import com.typesafe.config.ConfigFactory
import scala.tools.jline.console.ConsoleReader
import scala.tools.jline.console.history.FileHistory
import scala.tools.jline.console.completer._

import java.io._
import scala.collection.JavaConversions._
import scala.util.{ Failure, Success }

object CommandLoop {

  def main(args: Array[String]) = {
    val reader = new ConsoleReader()
    var (hostname, port, username, password) = ("127.0.0.1", 9999, "anonymous", "Actorb4se")
    //Argument GrammarParser
    if (args.length == 0)
      println("[!] no arg, Client loaded by default param");
    else {
      val arglist = args.toList
      type OptionMap = Map[String, String]

      def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
        def isSwitch(s : String) = (s(0) == '-')
        list match {
          case Nil => map
          case "-h" :: value :: tail =>
            nextOption(map ++ Map("host" -> value), tail)
          case "-p" :: value :: tail =>
            nextOption(map ++ Map("port" -> value), tail)
          case "-u" :: value :: tail =>
            nextOption(map ++ Map("username" -> value), tail)
          case string :: Nil => nextOption(map ++ Map("error" -> string), list.tail)
          case _ :: value :: tail =>
            nextOption(map ++ Map("error" -> value), tail)
        }
      }
      val options = nextOption(Map(), arglist)

      options get "host" map (hostname = _)
      options get "port" map (s => port = s.toInt)
      options get "username" map { u =>
        val pass: String =
          if (u != "anonymous" && !options.contains("password"))
            reader.readLine(">> password: ", '*')
          else options.get("password").getOrElse("Actorb4se")
        password = pass
        username = u
      }
    }
    DriverConnection.getDriver(hostname, port, username, password) match {
      case Success(d) =>

        var loop : Boolean = true

        // model ref
        val commandInvoker = new CommandInvoker

        // view ref
        val view = new ResultView

        // controller ref
        val grammarParser = new GrammarParser(commandInvoker, view, d)

        // attach view to observers
        commandInvoker.attach(view)
        grammarParser.attach(view)

        val history = new FileHistory(new File(".history"))
        val prompt = new ActorbasePrompt(d.connection)
        val banner = new ActorbaseBanner
        var completers : List[String] = Nil
        print(banner.getBanner())

        reader.setHistory(history)
        reader.setPrompt(prompt.getPrompt)
        reader.setBellEnabled(false)
        ConfigFactory.load("commands.conf").getConfig("commands").entrySet.foreach { entry =>
          if (username == "admin")
            completers :+= entry.getKey
          else if (entry.getKey != "addUser" && entry.getKey != "removeUser" && entry.getKey != "resetPassword")
            completers :+= entry.getKey
        }
        reader.addCompleter(new StringsCompleter(completers))

        val out : PrintWriter = new PrintWriter(reader.getTerminal().wrapOutIfNeeded(System.out))

        while(loop) {
          loop = grammarParser.parseInput(reader.readLine)
          out.flush
        }
      // reader.getHistory.asInstanceOf[FileHistory].flush()
      case Failure(e) => println(e.getMessage)
    }
  }
}
