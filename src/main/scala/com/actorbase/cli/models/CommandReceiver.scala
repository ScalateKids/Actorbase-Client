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
  * @author Scalatekids TODO DA CAMBIARE
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.cli.models

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import com.actorbase.driver.ActorbaseDriver

object CommandReceiver {

  /**
    * Driver singleton instance to send command and receive response
    */
  lazy val actorbaseDriver = new ActorbaseDriver()
}

/**
  * Receiver class, process input arguments sent by the controller
  * using a driver reference to send requests to a listening
  * Actorbase instance
  *
  * @param
  * @return
  * @throws
  */
class CommandReceiver(params: Map[Any, Any]) {

  def insert() : String = {
    var result : String = "[INSERT]\n"
    for ((k, v) <- params) {
      println(v.getClass)
      result += s"$k -> $v\n"
    }
    // result
    CommandReceiver.actorbaseDriver.addCollection("customers")
    "ok"
    // CommandReceiver.actorbaseDriver.insert("chiave10", "dummy", result).body.getOrElse("Nonnne")
  }

  def removeItem() : String = {
    var result : String = "[REMOVE ITEM]\n"
    for ((k, v) <- params) {
      result += CommandReceiver.actorbaseDriver.delete(
        params.get("key").get.asInstanceOf[String],
        params.get("collection").get.asInstanceOf[String]).body.getOrElse("None")
    }
    result
  }

  def export() : String = {
    var result : String = "[EXPORT]\n"
    for ((k, v) <- params) {
      result += s"$k -> $v\n"
    }
    result
  }

  def login() : String = {
    var result : String ="[LOGIN]\n"
    for((k,v) <- params) {
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    * Logout the active connection with the server instance of Actorbase
    */
  def logout() : String = {
    // CommandReceiver.actorbaseDriver.logout
    "[LOGOUT]\nSuccessfully logged out from actorbase"
  }

  /**
    * Test driver, returning a String by blocking is ugly as fuck
    * probably best to return Future[String] and demand printing
    * to the invoker.
    *
    * Also try/catch is temporary, probably not the right place to
    * handle errors
    *
    * @param
    * @return a String representing the output from the server instance
    * of Actorbase
    * @throws
    */
  def find() : String = {
    /*val key = params.get("key").getOrElse("None").asInstanceOf[String]
    val collectionList = params.get("collection").get.asInstanceOf[List[String]]
    val collection = collectionList(0)*/

    var response = ""
    params.get("key") match{
      case None =>
        params.get("collection") match{
          case None =>
            //TODO get all database?
            println("no collection")
          case Some(c) =>
            //TODO if its a list should call another method
            response = CommandReceiver.actorbaseDriver.getCollection( c.asInstanceOf[List[String]](0) ).toString
        }
      case Some(k) =>
        println("key is " + k)
        params.get("collection") match{
          case None =>
            //TODO find key from all database
            println("no collection")
          case Some(c) =>
            response = CommandReceiver.actorbaseDriver.find(k.asInstanceOf[String], c.asInstanceOf[List[String]](0)).toString
            println("collection is "+c.asInstanceOf[List[String]](0))
        }
    }

    response
  }

  // ugly as hell
  def help() : String = {
    var result : String = "[HELP]\n"
    ConfigFactory.load("commands.conf").getConfig("commands").entrySet.foreach { entry =>
      result += f"${entry.getKey}%-25s${entry.getValue.unwrapped}\n"
    }
    result
  }

  /*  collection operations */
  def createCollection() : String = {
    var result : String = "[CREATE COLLECTION]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  def listCollections() : String = "[LIST COLLECTIONS]\n"

  def renameCollection() : String = {
    var result : String = "[MODIFY COLLECT NAME]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  def deleteCollection() : String = {
    var result : String = "[DELETE COLLECTION]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  def addCollaborator() : String = {
    var result: String="[ADD CONTRIBUTOR]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  def removeCollaborator() : String = {
    var result: String="[REMOVE COLLABORATOR]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  def changePassword() : String = {
    var result: String="[CHANGE PASSWORD]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  def addUser() : String = {
    var result: String="[ADD USER]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  def removeUser() : String = {
    var result: String="[REMOVE USER]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  def resetPassword() : String = {
    var result: String="[RESET PASSWORD]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }
}
