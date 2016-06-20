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
  * @author Scalatekids
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.cli.models

import java.io.FileNotFoundException

import com.actorbase.driver.exceptions._
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._
import java.io.FileNotFoundException
// import scala.concurrent.ExecutionContext.Implicits.global

import com.actorbase.driver.ActorbaseDriver

object CommandReceiver {

  /**
    * Driver singleton instance to send command and receive response
    */
  def actorbaseDriver(hostname: String, port: Int, username: String,  password: String): ActorbaseDriver =
    ActorbaseDriver("http://" + username + ":" + password + "@" + hostname + ":" + port)

}

/**
  * Receiver class, process input arguments sent by the controller
  * using a driver reference to send requests to a listening
  * Actorbase instance.
  *
  * @param params a map containing the parameters that are used
  *                for the methods.
  */
class CommandReceiver(params: Map[Any, Any], driver: ActorbaseDriver) {

  /**
    * Insert an item to the actorbase server.
    *
    * @return a String, "Item inserted" if the method succeeded, an error message is returned if the method failed
    */

  def insert() : String = {
    var result = "Item inserted."
    params get "key" map { k =>
      params get "value" map { v =>
        params get "collection" map { c =>
          try {
            driver.insert(c.asInstanceOf[String], false, (k.asInstanceOf[String] -> v))
          } catch {
            case wce: WrongCredentialsExc => result =  "Credentials privilege level does not meet criteria needed to perform this operation."
            case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
          }
        }
      }
    }
    result
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
      driver.remove(collection, key)
    }
    catch {
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
    // val username =  params.get("username").get.asInstanceOf[String]
    // val password = params.get("password").get.asInstanceOf[String]
    // try {
    //   driver = driver.authenticate(username, password)
    // } catch {
    //   case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
    //   case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
    // }
    "Login successful"
  }

  /**
    * Logout the active connection with the server instance of Actorbase
    *
    * @return a String, "logout succeeded" if the method succeeded, an error message is returned if the method failed
    */
  def logout() : String = { //TODO ?
                            // driver.logout
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
              response = driver.getCollections.toString
            case Some(c) =>
              //TODO if its a list should call another method, or change this in the driver
              response = driver.getCollection(c.asInstanceOf[List[String]](0)).toString
          }
        case Some(k) =>
          params.get("collection") match {
            case None =>
            //TODO find key from all database
            case Some(c) =>
              // val actColl = driver.getCollection( c.asInstanceOf[List[String]](0) )
              // response = actColl.findOne( k.toString ).toString
              val actColl = driver.find(k.asInstanceOf[String], c.asInstanceOf[List[String]].toSeq: _*)
              response = actColl.toString
          }
      }
    }
    catch {
      case uce: UndefinedCollectionExc => response = "Undefined collection"
      case wce: WrongCredentialsExc => response = "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => response = "There was an internal server error, something wrong happened."
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
      driver.addCollection(name)
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
    }

    "collection " + name + " created"
  }

  /**
    * List all the collections from the server instance of Actorbase.
    *
    * @return a String containing all the collections names the used has access to
    */
  def listCollections() : String = {  //TODO need test when the server will implement this feature
    try {
      val collectionList = driver.listCollections
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
    * Drop a collection in the server instance of Actorbase.
    *
    * @return a String, "Collection deleted" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def deleteCollection() : String = { //TODO need test when the server will implement this feature
    val name = params.get("Collection").get.asInstanceOf[String]
    try {
      driver.dropCollections(name)
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
    var result: String = ""
    val collection = params.get("collection").get.asInstanceOf[String]
    val username = params.get("username").get.asInstanceOf[String]
    val permission = params.get("permissions").get.asInstanceOf[String]
    val p = if (permission == "read") false else true
    try {
      driver.getCollection(collection).addContributor(username, p)
    } catch {
      case wce: WrongCredentialsExc => result = "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
      case uue: UndefinedUsernameExc => result = "Contributor username not found."
      case uae: UsernameAlreadyExistsExc => result = "Contributor already added."
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
    var result: String = ""
    val collection = params.get("collection").get.asInstanceOf[String]
    val username = params.get("username").get.asInstanceOf[String]
    try {
      driver.getCollection(collection).removeContributor(username)
    } catch {
      case wce: WrongCredentialsExc => result = "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
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
      val oldPsw = params.get("oldPsw").get.asInstanceOf[String]
      val newPsw = params.get("newPsw").get.asInstanceOf[String]
      driver.changePassword(newPsw)

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
      driver.addUser(username)
      username + " added to the system"
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
      driver.removeUser(username)
      username + " removed from the system"
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
      driver.resetPassword(user)
      user + " password reset"
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
  def export() : String = {
    //val list = params.get("p_list").asInstanceOf[List[String]]
    //val path = params.get("f_path").asInstanceOf[String]
    try{
      val path = params.get("f_path").get.asInstanceOf[String]
      driver.exportData(path)
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
      case uue: UndefinedUsernameExc => return "Undefined username: Actorbase does not contains such credential"
      case uae: UsernameAlreadyExistsExc => return "Username already exists in the system Actorbase"
    }
    "exported"
  }

  /**
    * Import data from a well formatted JSON file.
    *
    * @return a String, "imported" if the method succeded, an error message if it fails
    */

  def importFrom() : String = {
    try{
      val path = params.get("path").get.asInstanceOf[String]
      driver.importData(path)
    }
    catch{
      case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
      case mfe: MalformedFileExc => return "Malformed json file"
      case fnfe: FileNotFoundException => return "file not found"
    }
    "imported"
  }
}
