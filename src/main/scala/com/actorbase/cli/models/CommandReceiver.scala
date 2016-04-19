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

package com.actorbase.cli.models

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

class CommandReceiver(params: Map[Any, Any]) {

  def insert() : String = {
    var result : String = "[INSERT]\n"
    for ((k, v) <- params) {
      result += s"$k -> $v\n"
    }
    result
  }

  def removeItem() : String = {
    var result : String = "[REMOVE ITEM]\n"
    for ((k, v) <- params) {
      result += s"$k -> $v\n"
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

  def logout() : String = {
    val result : String ="[LOGOUT]\nSuccessfully logged out from actorbase"
    result
  }

  def find() : String = {
    var result: String="[FIND]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  // ugly as hell
  def help() : String = {
    var result : String = "[HELP]\n"
    val set = ConfigFactory.load("commands.conf").getConfig("commands").entrySet.foreach { entry =>
      result += entry.getKey + "\t" + entry.getValue.unwrapped + "\n"
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

  def listCollections() : String = {
    var result : String = "[LIST COLLECTIONS]\n"
    result
  }

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
