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

import com.actorbase.driver.exceptions._
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._
// import scala.concurrent.ExecutionContext.Implicits.global

import com.actorbase.driver.ActorbaseDriver

object CommandReceiver {

  /**
    * Driver singleton instance to send command and receive response
    */
  lazy val actorbaseDriver = ActorbaseDriver()

}

/**
  * Receiver class, process input arguments sent by the controller
  * using a driver reference to send requests to a listening
  * Actorbase instance.
  *
  * @param params a map containing the parameters that are used
  *                for the methods.
  */
class CommandReceiver(params: Map[Any, Any]) {

  /**
    * Insert an item to the actorbase server.
    *
    * @return a String, "Item inserted" if the method succeeded, an error message is returned if the method failed
    */

  def insert() : String = {
    val key = params.get("key").get.asInstanceOf[String]
    val value = params.get("value").get
    val collection = params.get("collection").get.asInstanceOf[String]
    try {
      val actColl = CommandReceiver.actorbaseDriver.getCollection(collection)
      actColl.insert((key, value))
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
    }
    "Item inserted."
  }

  /**
    * Remove an item from the actorbase server.
    *
    * @return a String, "Item removed" if the method succeeded, an error message is returned if the method failed
    */
  def removeItem() : String = {
    val key = params.get("key").get.asInstanceOf[String]
    val collection = params.get("collection").get.asInstanceOf[String]

    try {
      val actColl = CommandReceiver.actorbaseDriver.getCollection(collection)
      actColl.remove(key)
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
    }
    "Item removed"
  }


  /**
    * Authenticate to the actorbase server.
    *
    * @return a String, "login succeeded" if the method succeeded, an error message is returned if the method failed
    */
  def login() : String = {
    var result : String ="[LOGIN]\n"  //TODO ?
    for((k,v) <- params) {
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    * Logout the active connection with the server instance of Actorbase
    *
    * @return a String, "logout succeeded" if the method succeeded, an error message is returned if the method failed
    */
  def logout() : String = { //TODO ?
    // CommandReceiver.actorbaseDriver.logout
    "[LOGOUT]\nSuccessfully logged out from actorbase"
  }

  /**
    * Find command, this method is used to search in the server instance of Actorbase.
    * Based on the params of the class this method can:
    *  _search for a key in one or more collections;
    *  _return one or more collections;
    *
    * @return a String representing the output from the server instance of Actorbase
    */
  def find() : String = { //TODO THIS HAS TO BE FINISHED
    var response = ""
    try {
      params.get("key") match {
        case None =>
          params.get("collection") match {
            case None =>
              response = CommandReceiver.actorbaseDriver.getCollections.toString
            case Some(c) =>
              //TODO if its a list should call another method, or change this in the driver
              response = CommandReceiver.actorbaseDriver.getCollection(c.asInstanceOf[List[String]](0)).toString
          }
        case Some(k) =>
          params.get("collection") match {
            case None =>
            //TODO find key from all database
            case Some(c) =>
              // val actColl = CommandReceiver.actorbaseDriver.getCollection( c.asInstanceOf[List[String]](0) )
              // response = actColl.findOne( k.toString ).toString
              val actColl = CommandReceiver.actorbaseDriver.find(k.asInstanceOf[String], c.asInstanceOf[List[String]].toSeq: _*)()
              response = actColl.toString
          }
      }
    }
    catch {
      case uce: UndefinedCollectionExc => return "Undefined collection"
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
    }
    response
  }

  /**
    * This method is used to get help to the user, can give a generic help containing
    * all the possible commands that the user can call or help about one specific command
    *
    * @return a String representing the help message
    */
  // ugly as hell
  def help() : String = {   //TODO dovrebbe mostrare solo i comandi del privilegio relativo all'account
    var result : String = "\n"//"[HELP]\n"
    params.get("command").get match {
      case None =>
        ConfigFactory.load ("commands.conf").getConfig ("commands").entrySet.foreach {
          entry =>
          result += f"  ${
            entry.getKey
          }%-25s${
            entry.getValue.unwrapped
          }\n"
        }
      case Some(c) =>
        ConfigFactory.load ("commands.conf").getConfig ("commands").entrySet.foreach {
          entry =>
          if(entry.getKey == c.toString) {
            result += f"  ${
              entry.getKey
            }%-25s${
              entry.getValue.unwrapped
            }\n"
          }
        }
    }
    if(result == "")
      result += "Command not found, to have a list of commands available type <help>"
    result
  }

  /**
    * Create a collection in the server instance of Actorbase.
    *
    * @return a String, "Collection created" if the method succeeded, an error message is returned if the method failed
    */
  def createCollection() : String = {
    val name = params.get("name").get.asInstanceOf[String]
    try {
      CommandReceiver.actorbaseDriver.addCollection(name)
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
    }

    "collection "+name+" created"
  }

  /**
    * List all the collections from the server instance of Actorbase.
    *
    * @return a String containing all the collections names the used has access to
    */
  def listCollections() : String = {  //TODO need test when the server will implement this feature
    try {
      val collectionList = CommandReceiver.actorbaseDriver.listCollections
      //collectionList.foreach(println)
      var list = ""
      collectionList.foreach(c => list = list + c + "\n")
      list
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
    }
  }

  /**
    * Rename a collection in the server instance of Actorbase.
    *
    * @return a String, "Collectiong renamed" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def renameCollection() : String = { //TODO
  try{}
  catch{
    case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
    case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
  }
    "Collection renamed"
  }

  /**
    * Drop a collection in the server instance of Actorbase.
    *
    * @return a String, "Collection deleted" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def deleteCollection() : String = { //TODO need test when the server will implement this feature
    val name = params.get("Collection").get.asInstanceOf[String]
    try {
      CommandReceiver.actorbaseDriver.dropCollections(name)
    }
  catch{
    case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
    case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
  }
  "deleted"
}

  /**
    * Add a collaborator to a collection in the server instance of Actorbase.
    *
    * @return a String, "Collaborator added" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def addCollaborator() : String = {   //TODO
    var result: String="[ADD CONTRIBUTOR]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    * Remove a collaborator from a collection in the server instance of Actorbase.
    *
    * @return a String, "Collaborator removed" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def removeCollaborator() : String = {   //TODO
    var result: String="[REMOVE COLLABORATOR]\n"
    for((k,v) <- params){
      result += s"$k -> $v\n"
    }
    result
  }

  /**
    * Change the user password in the server instance of Actorbase.
    *
    * @return a String, "Password changed" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def changePassword() : String = {
    try{
      val oldPsw = params.get("oldPsw").asInstanceOf[String]
      val newPsw = params.get("newPsw").asInstanceOf[String]
      CommandReceiver.actorbaseDriver.changePassword(newPsw)

      "Password changed"
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
      case wnp: WrongNewPasswordExc => return "The password inserted does not meet Actorbase criteria"
      case uue: UndefinedUsernameExc => return "Undefined username"
    }
  }

  /**
    * Add a user to the server instance of Actorbase. This operation needs Admin privileges
    *
    * @return a String, "User added" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def addUser() : String = {
    try{
      val username = params.get("username").get.asInstanceOf[String]
      CommandReceiver.actorbaseDriver.addUser(username)
      username+" removed from the system"
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
      case uae: UsernameAlreadyExistsExc=> return "Username already exists in the system Actorbase"
    }
  }

  /**
    * Remove a user from the server instance of Actorbase. This operation needs Admin privileges
    *
    * @return a String, "User removed" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def removeUser() : String = {
    try{
      val username = params.get("username").get.asInstanceOf[String]
      CommandReceiver.actorbaseDriver.removeUser(username)
      username+" removed from the system"
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
      case uue: UndefinedUsernameExc => return "Undefined username: Actorbase does not contains such credential"
    }
  }

  /**
    * Reset the password of a user in the server instance of Actorbase. This operation needs Admin privileges.
    * The password is reset to the default Actorbase password: Actorb4se
    *
    * @return a String, "Password reset" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def resetPassword() : String = {
    try{
      val user = params.get("username").get.asInstanceOf[String]
      CommandReceiver.actorbaseDriver.resetPassword(user)
      user+" password reset"
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
      case uue: UndefinedUsernameExc => return "Undefined username: Actorbase does not contains such credential"
      case uae: UsernameAlreadyExistsExc => return "Username already exists in the system Actorbase"
    }
  }

  /**
    * Export actorbase data into a file. Based on params this method can export:
    *  _a key in one or more collections;
    *  _one or more collections;
    *
    * @return a String, "Exported" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def export() : String = { //val list = params.get("p_list").asInstanceOf[List[String]]
                            //val path = params.get("f_path").asInstanceOf[String]
    /* todo
    try{
      val path = params.get("f_path").asInstanceOf[String]
      CommandReceiver.actorbaseDriver.exportToFile(path)
      "Exported into "+path
    }
    catch{
      case wce: WrongCredentialsExc => "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => "There was an internal server error, something wrong happened."
      case uue: UndefinedUsernameExc => "Undefined username: Actorbase does not contains such credential"
      case uae: UsernameAlreadyExistsExc => "Username already exists in the system Actorbase"
    }*/
    "exported"
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
