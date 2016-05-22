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

  /**
    *
    * @return
    */
  def insert() : String = {
    val key = params.get("key").get.asInstanceOf[String]
    val value = params.get("value").get
    val collection = params.get("collection").get.asInstanceOf[String]

    val actColl = CommandReceiver.actorbaseDriver.getCollection(collection)

    actColl.insert((key, value))

    "Item inserted" //stub
  }

  /**
    *
    * @return
    */
  def removeItem() : String = {
    val key = params.get("key").get.asInstanceOf[String]
    val collection = params.get("collection").get.asInstanceOf[String]

    val actColl = CommandReceiver.actorbaseDriver.getCollection(collection)

    actColl.remove( key )

    "Item removed" //stub
  }


  /**
    *
    * @return
    */
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
    var response = ""
    params.get("key") match{
      case None =>
        params.get("collection") match{
          case None =>
            //TODO get all database?
          case Some(c) =>
            //TODO if its a list should call another method, or change this in the driver
            response = CommandReceiver.actorbaseDriver.getCollection( c.asInstanceOf[List[String]](0) ).toString
        }
      case Some(k) =>
        params.get("collection") match{
          case None =>
            //TODO find key from all database
          case Some(c) =>
            val actColl = CommandReceiver.actorbaseDriver.getCollection( c.asInstanceOf[List[String]](0) )
            response = actColl.findOne( k.toString ).toString
        }
    }

    response
  }

  /**
    *
    * @return
    */
  // ugly as hell
  def help() : String = {
    var result : String = "[HELP]\n"
    params.get("command") match {
      case None =>
        ConfigFactory.load ("commands.conf").getConfig ("commands").entrySet.foreach {
        entry =>
        result += f"${
        entry.getKey
        }%-25s${
        entry.getValue.unwrapped
        }\n"
        }
      case Some(c) =>
        ConfigFactory.load ("commands.conf").getConfig ("commands").entrySet.foreach {
          entry =>
            if(entry.getKey == c.toString) {
              result += f"${
                entry.getKey
              }%-25s${
                entry.getValue.unwrapped
              }\n"
            }
            else
              result += "command not found, to have a list of commands available type <help>"
        }
    }
    result
  }

  /**
    *
    * @return
    */
  def createCollection() : String = {
    val name = params.get("name").get.asInstanceOf[String]
    CommandReceiver.actorbaseDriver.addCollection(name)

    //TODO check if everything was ok?

    "collection "+name+" created" // stub
  }

  /**
    *
    * @return
    */
  def listCollections() : String = {  //TODO need test when the server will implement this feature
    val collectionList = CommandReceiver.actorbaseDriver.listCollections

    collectionList.foreach(println)

    var list = ""
    collectionList.foreach(c => list = list+c+"\n")
    list
  }

  /**
    *
    * @return
    */
  def renameCollection() : String = { //TODO
    "to be implemented soon"
  }

  /**
    *
    * @return
    */
  def deleteCollection() : String = { //TODO need test when the server will implement this feature
    val name = params.get("Collection").get.asInstanceOf[String]
    val done = CommandReceiver.actorbaseDriver.dropCollection(name)

    if (done) name+" deleted" else "there was an error deleting "+name
  }

  /**
    *
    * @return
    */
  def addCollaborator() : String = {
    var result: String="[ADD CONTRIBUTOR]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    *
    * @return
    */
  def removeCollaborator() : String = {
    var result: String="[REMOVE COLLABORATOR]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    *
    * @return
    */
  def changePassword() : String = { //TODO checks on psw?
    val oldPsw = params.get("oldPsw").asInstanceOf[String]
    val newPsw = params.get("newPsw").asInstanceOf[String]
    val done = CommandReceiver.actorbaseDriver.changePassword(/*oldPsw, */newPsw)

    if (done) "Password changed correctly" else "Something went wrong during the oepration"
  }

  /**
    *
    * @return
    */
  def addUser() : String = {
    var result: String="[ADD USER]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    *
    * @return
    */
  def removeUser() : String = {
    var result: String="[REMOVE USER]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    *
    * @return
    */
  def resetPassword() : String = {
    var result: String="[RESET PASSWORD]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    *
    * @return
    */
  def export() : String = { //TODO to be done
    //val list = params.get("p_list").asInstanceOf[List[String]]
    //val path = params.get("f_path").asInstanceOf[String]

    "Exported into "//+path
  }

  /**
    *
    * @return
    */
  /* TODO to be done
  def import() : String = {
    var result : String = "[EXPORT]\n"
    for ((k, v) <- params) {
      result += s"$k -> $v\n"
    }
    result
  }*/
}
