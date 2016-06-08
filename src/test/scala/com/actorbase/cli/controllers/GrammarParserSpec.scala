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

import com.actorbase.cli.CLISpecs.CLIUnitSpec

import com.actorbase.cli.views.ResultView
import com.actorbase.cli.models.CommandInvoker

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class GrammarParserSpec extends CLIUnitSpec {

  "GrammarParser.parseInput" should "parse 'listCollections' command" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("listCollections") === true)
  }

  it should "parse 'addCollaborator user to collection'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("addCollaborator testUser to testCOllection") === true)
  }

  it should "parse 'removeCollaborator user from collection'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("removeCollaborator testUser from testCOllection") === true)
  }

  it should "parse 'createCollection collection'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("createCollection testCollection") === true)
  }

  it should "parse 'renameCollection collection to newname'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("createCollection testCollection to testName") === true)
  }

  it should "parse 'insert (key -> value ) to collection'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("insert (key -> value ) to testCollection") === true)
  }

  it should "parse 'remove key from collection'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("remove key from testCollection") === true)
  }

  it should "parse 'find key from collection'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("find key from testCollection") === true)
  }

  it should "parse 'find key'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("find key from") === true)
  }

  it should "parse 'find from collection'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("find from testCollection") === true)
  }

  it should "parse 'find from'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("find from ") === true)
  }

  it should "parse 'logout' command" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("logout") === true)
  }

  it should "parse a single key as first argument in export command e.g. 'export key to path'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("export key to path") === true)
  }

  it should "parse list as first argument in export command e.g. 'export key1,key2,key3 to path'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("export key1,key2,key3 to path") === true)
  }

  it should "parse 'help'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("help") === true)
  }

  it should "parse a single command in the help command 'help command'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("help test") === true)
  }

  it should "parse 'addUser user'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("addUser testUser") === true)
  }

  it should "parse 'removeUser user'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("removeUser testUser") === true)
  }

  it should "parse 'resetPassword user'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("resetPassword testUser") === true)
  }

  it should "parse 'exit'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("exit") === false)
  }

  it should "parse 'quit'" in {
    val grammarParser = new GrammarParser(new CommandInvoker, new ResultView)
    assert(grammarParser.parseInput("quit") === false)
  }

}
