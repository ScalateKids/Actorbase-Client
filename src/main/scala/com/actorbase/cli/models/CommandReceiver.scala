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
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.actorbase.driver.ActorbaseDriver
import scala.util.{ Failure, Success }

// object CommandReceiver {

// implicit def anyToString(m: Map[String, Any]): Map[String, String] = m.asInstanceOf[Map[String, String]]
//   /**
//     * Driver singleton instance to send command and receive response
//     */
//   def actorbaseDriver(hostname: String, port: Int, username: String,  password: String): ActorbaseDriver =
//     ActorbaseDriver("http://" + username + ":" + password + "@" + hostname + ":" + port)

// }

sealed trait Helper {
  def as[A <: Any](o: Any): A = o.asInstanceOf[A]
}

/**
  * Receiver class, process input arguments sent by the controller
  * using a driver reference to send requests to a listening
  * Actorbase instance.
  *
  * @param params a map containing the parameters that are used
  *                for the methods.
  */
class CommandReceiver(params: Map[String, Any], driver: ActorbaseDriver) extends Helper {

  /**
    * Insert an item to the actorbase server.
    *
    * @return a String, "Item inserted" if the method succeeded, an error message is returned if the method failed
    */
  def insert(): String = {
    var result = "Item inserted."
    params get "key" map { k =>
      params get "value" map { v =>
        params get "collection" map { c =>
          params get "update" map { u =>
            val value = as[String](v) match {
              case integer if integer matches("""^\d+$""") => integer.toInt
              case double if double matches("""^\d+\.\d+""") => double.toDouble
              case _ => as[String](v)
            }
            try {
              val update = as[Boolean](u)
              if (as[String](c) contains ".") {
                val collection = as[String](c).split("\\.")
                driver.insertTo(collection(1), update, (as[String](k) -> value))(collection(0))
              } else driver.insert(as[String](c), update, (as[String](k) -> value))
            }
            catch {
              case wce: WrongCredentialsExc => result = "Credentials privilege level does not meet criteria needed to perform this operation."
              case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
              case dke: DuplicateKeyExc => result = "Key already stored"
            }
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
    params get "key" map { ka =>
      params get "collection" map { ca =>
        val c = as[String](ca)
        val k = as[String](ka)
        try {
          if (c contains ".") {
            val collection = c.split("\\.")
            driver.removeFrom(collection(1), k)(collection(0))
          } else driver.remove(c, k)
        }
        catch {
          case uce: UndefinedCollectionExc => return "Undefined collection."
          case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
          case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
        }
      }
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
  def listUsers(): String = {
    var result = "\n"
    try {
      result = driver.listUsers.mkString("\n")
    } catch {
      case wce: WrongCredentialsExc => result = "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
      case uae: UsernameAlreadyExistsExc=> result = "Username already exists in the system Actorbase"
    }
    result
  }

  /**
    * Find command, this method is used to search in the server instance of Actorbase.
    * Based on the params of the class this method can:
    *  _search for a key in one or more collections;
    *  _return one or more collections;
    *
    * @return a String representing the output from the server instance of Actorbase
    */
  def find(): String = {
    var response = ""
    try {
      params.get("key") match {
        case None =>
          params.get("collection") match {
            case None =>
              // get all collections
              response = driver.getCollections.toString
            case Some(c) =>
              // get collections contained into a list
              as[List[String]](c).foreach(x => {
                if (x contains ".") {
                  val splitted = x.split("\\.")
                  response += driver.getCollection( splitted(1), splitted(0) ).toString+"\n"
                }
                else 
                  response += driver.getCollection( x ).toString+"\n"
              })
          }
        case Some(k) =>
          params.get("collection") match {
            case None =>
              // find key from all database
              val allCollections = driver.listCollections map (x => x.head._2.head -> x.head._1)
              allCollections.foreach( x => {
                val obj = (driver.findFrom(k.asInstanceOf[String], x._1)(x._2))
                if(obj != new com.actorbase.driver.data.ActorbaseObject(Map[String,Any]()))
                  response += obj.toString+"\n"                
                }
              )
              //response = (driver.findFrom(k.asInstanceOf[String], allCollections.toSeq:_*)()).toString
            case Some(c) =>
              // find key from a list of collections
      			  c.asInstanceOf[List[String]].foreach{ x => 
                if(x contains "."){
        				  val collection = x.split("\\.")
        			    response += (driver.findFrom(k.asInstanceOf[String], collection(1))(collection(0))).toString+"\n"
                }
                else 
                  response += (driver.findFrom(k.asInstanceOf[String], x)() ).toString+"\n"
      			  }
          }
      }
    }
    catch {
      case uce: UndefinedCollectionExc => response = "Undefined collection"
      // if (driver.connection.username == "admin")
      // response = driver.getCollections.toString
      // else response = "Undefined collection"
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
  def help(): String = {
    var result: String = "\n"
    params get "command" map { c =>
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
    } getOrElse {
      ConfigFactory.load ("commands.conf").getConfig ("commands").entrySet.foreach {
        entry =>
        result += f"  ${
          entry.getKey
        }%-25s${
          entry.getValue.unwrapped
        }\n"
      }
    }
    if(result == "\n")
      result += "Command not found, to have a list of commands available type <help>"
    result
  }

  /**
    * Create a collection in the server instance of Actorbase.
    *
    * @return a String, "Collection created" if the method succeeded, an error message is returned if the method failed
    */
  def createCollection(): String = {
    var name = ""
    params get "name" map { c =>
      name = as[String](c)
      try {
        driver.addCollection(name)
      } catch {
        case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
        case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
      }
    }
    "collection " + name + " created"
  }

  /**
    * List all the collections from the server instance of Actorbase.
    *
    * @return a String containing all the collections names the used has access to
    */
  def listCollections(): String = {
    val divisor = 1024 * 1024
    val (header1, header2, header3) = ("OWNER", "COLLECTION", "SIZE")
    var list = f"\n $header1%-14s | $header2%14s | $header3%14s \n"
    list += " -------------------------------------------------\n"
    try {
      val collectionList = driver.listCollections
      if (collectionList.length > 0) {
        collectionList.foreach { c =>
          val mb = c.head._2.last.toDouble / divisor
          list += f" ${c.head._1}%-14s | ${c.head._2.head}%14s | ${mb}%.6f MB\n"
        }
      }
      else list = "No collections found"
    }
    catch {
      case wce: WrongCredentialsExc => list = "Credentials privilege level does not meet criteria needed to perform this operation."
      case iec: InternalErrorExc => list = "There was an internal server error, something wrong happened."
    }
    list
  }

  /**
    * Drop a collection in the server instance of Actorbase.
    *
    * @return a String, "Collection deleted" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def deleteCollection() : String = {
    var response = "deleted"
    params get "collection" map { c =>
      try {
        driver.dropCollections(as[String](c))
      }
      catch {
        case uc: UndefinedCollectionExc => response = "Undefined collection."
        case wce: WrongCredentialsExc => response = "Credentials privilege level does not meet criteria needed to perform this operation."
        case iec: InternalErrorExc => response = "There was an internal server error, something wrong happened."
      }
    }
    response
  }

  /**
    * Add a collaborator to a collection in the server instance of Actorbase.
    *
    * @return a String, "Collaborator added" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def addCollaborator() : String = {
    var result: String = ""
    params get "collection" map { c =>
      params get "username" map { u =>
        params get "permissions" map { p =>
          val collection = as[String](c)
          val username = as[String](u)
          val permission = if (as[String](p) == "ReadOnly") false else true
          try {
            if (collection contains ".") {
              val coll = collection.split("\\.")
              driver.addContributorTo(username, coll(1), permission, coll(0))
            }
            else 
              driver.addContributorTo(username, collection, permission)
            result = s"$username added to collection $collection"
          } catch {
            case wce: WrongCredentialsExc => result = "Credentials privilege level does not meet criteria needed to perform this operation."
            case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
            case uue: UndefinedUsernameExc => result = "Contributor username not found."
            case uae: UsernameAlreadyExistsExc => result = "Contributor already added."
            case uc: UndefinedCollectionExc => result = "Undefined collection."
          }
        }
      }
    }
    // val collection = params.get("collection").get.asInstanceOf[String]
    // val username = params.get("username").get.asInstanceOf[String]
    // val permission = params.get("permissions").get.asInstanceOf[String]
    // val p = if (permission == "read") false else true
    // try {
    //   driver.getCollection(collection).addContributor(username, p)
    // } catch {
    //   case wce: WrongCredentialsExc => result = "Credentials privilege level does not meet criteria needed to perform this operation."
    //   case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
    //   case uue: UndefinedUsernameExc => result = "Contributor username not found."
    //   case uae: UsernameAlreadyExistsExc => result = "Contributor already added."
    // }
    result
  }

  /**
    * Remove a collaborator from a collection in the server instance of Actorbase.
    *
    * @return a String, "username removed from collection" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def removeCollaborator() : String = {
    var result: String = ""
    params get "collection" map { c =>
      params get "username" map { u =>
        val collection = as[String](c)
        val username = as[String](u)
        try {
          if (collection contains ".") {
            val coll = collection.split("\\.")
            driver.removeContributorFrom(username, coll(1), coll(0))
          }
          else 
            driver.removeContributorFrom(username, collection)
          result = s"$username removed from collection $collection"
        } catch {
          case wce: WrongCredentialsExc => result = "Credentials privilege level does not meet criteria needed to perform this operation."
          case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
          case uue: UndefinedUsernameExc => result = "Contributor username not found."
          case uae: UsernameAlreadyExistsExc => result = "Contributor already added."
          case uc: UndefinedCollectionExc => result = "Undefined collection."
        }
      }
    }
    // var result: String = ""
    // val collection = params.get("collection").get.asInstanceOf[String]
    // val username = params.get("username").get.asInstanceOf[String]
    // try {
    //   driver.getCollection(collection).removeContributor(username)
    // } catch {
    //   case wce: WrongCredentialsExc => result = "Credentials privilege level does not meet criteria needed to perform this operation."
    //   case iec: InternalErrorExc => result = "There was an internal server error, something wrong happened."
    // }
    result
  }

  /**
    * Change the user password in the server instance of Actorbase.
    *
    * @return a String, "Password changed" if the method succeeded, an error message is returned
    *         if the method failed
    */
  def changePassword() : String = {
    params get "oldPsw" map { o =>
      params get "newPsw" map { n =>
        try {
          driver.changePassword(as[String](n))
        }
        catch {
          case wce: WrongCredentialsExc => "Credentials privilege level does not meet criteria needed to perform this operation."
          case iec: InternalErrorExc => "There was an internal server error, something wrong happened."
          case wnp: WrongNewPasswordExc => "The password inserted does not meet Actorbase criteria"
          case uue: UndefinedUsernameExc => "Undefined username"
        }
      }
    }
    "Password changed"
    // try{
    //   val oldPsw = params.get("oldPsw").get.asInstanceOf[String]
    //   val newPsw = params.get("newPsw").get.asInstanceOf[String]
    //   driver.changePassword(newPsw)
    //
    //   "Password changed"
    // }
    // catch{
    //   case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
    //   case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
    //   case wnp: WrongNewPasswordExc => return "The password inserted does not meet Actorbase criteria"
    //   case uue: UndefinedUsernameExc => return "Undefined username"
    // }
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
    try {
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

  def importFrom(): String = {
    params get "path" map { p =>
      try {
        driver.importData(as[String](p))
      }
      catch {
        case fnfe: FileNotFoundException => return "File not found"
        case wce: WrongCredentialsExc => return "Credentials privilege level does not meet criteria needed to perform this operation."
        case iec: InternalErrorExc => return "There was an internal server error, something wrong happened."
        case mfe: MalformedFileExc => return "Malformed json file"
        case uun: UndefinedUsernameExc => return "Undefined username"
        case dk: DuplicateKeyExc => return "Duplicated key request found"
      }
    }
    "imported"
  }

}
