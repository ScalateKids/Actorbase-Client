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
    //TODO
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
    var result: String="[REMOVE CONTRIBUTOR]\n"
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
