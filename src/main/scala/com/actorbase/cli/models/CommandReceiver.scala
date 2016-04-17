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

  def addContributor() : String = {
    var result: String="[ADD CONTRIBUTOR]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
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
}
