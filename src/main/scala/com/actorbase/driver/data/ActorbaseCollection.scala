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

package com.actorbase.driver.data

import com.actorbase.driver.ActorbaseDriver.Connection
import com.actorbase.driver.client.Connector
import com.actorbase.driver.client.api.RestMethods._
import com.actorbase.driver.client.api.RestMethods.Status._
import com.actorbase.driver.exceptions._

import java.io.{File, PrintWriter}
import scala.collection.immutable.TreeMap
import scala.collection.generic.FilterMonadic


/**
  * Class representing a single collection of the database at the current state,
  * that is the state at the very moment of the query time of the system. It
  * contains key-value pairs representing items of the collection and exposes
  * some utility methods to navigate and modify the contents in an easier way,
  * reflecting all changes directly to the remote counterpart.
  */
case class ActorbaseCollection
  (val owner: String, var collectionName: String,
    var data: TreeMap[String, Any] = new TreeMap[String, Any]())(implicit val conn: Connection, implicit val scheme: String)
    extends Connector {

  val uri: String = scheme + conn.address + ":" + conn.port

  /**
    * Rename the collection, reflecting this change to the remote system
    *
    * @param newName a String representing the new name of the collection to be changed
    * @return no return value
    * @throws
    */
  def rename(newName: String): Unit = ???

  /**
    * Insert an arbitrary variable number of key-value tuple to the collection
    * reflecting local changes to remote collection on server-side
    *
    * @param kv a vararg Tuple2 of type (String, Any)
    * @return an object of type ActorbaseCollection representing the collection updated
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def insert(kv: (String, Any)*): ActorbaseCollection = {
    for((k, v) <- kv) {
      if(!data.contains(k)) {
        data += (k -> v)
        val response = requestBuilder withCredentials(conn.username, conn.password) withUrl uri + "/collections/" + collectionName + "/" + k withBody serialize2byteArray(v) withMethod POST send()
        response.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case _ =>
        }
      }
    }
    ActorbaseCollection(owner, collectionName, data)
  }

  /**
    * Insert a new key-value tuple, representing an ActorbaseObject to the
    * collection reflecting local changes to remote collection on server-side
    *
    * @param kv an ActorbaseObject parameter representing a key/value pair
    * @return an object of type ActorbaseCollection representing the collection updated
    * @throws
    */
  def insert[A >: Any](kv: ActorbaseObject[A]): ActorbaseCollection = this.insert(kv.toSeq:_*)

  /**
    * Update one or more key-value of the collection, reflecting changes
    * directly on remote system
    *
    * @param kv a vararg of Tuple2[String, Any] representing key-value pairs to be updated in the system
    * @return an object of ActorbaseCollection containing the updated keys
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def update(kv: (String, Any)*): ActorbaseCollection = {
    for ((k, v) <- kv) {
      if(!data.contains(k)) {
        data -= k
        data += (k -> v)
        val response = requestBuilder withCredentials(conn.username, conn.password) withUrl uri + "/collections/" + collectionName + "/" + k withBody serialize2byteArray(v) withMethod PUT send()
        response.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case _ =>
        }
      }
    }
    ActorbaseCollection(owner, collectionName, data)
  }

  /**
    * Remove an arbitrary variable number of key-value tuple from the collection
    * reflecting local changes to remote collection on server-side
    *
    * @param keys a vararg String representing a sequence of keys to be removed
    * from the collection
    * @return an object of ActorbaseCollection containing the updated keys
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def remove(keys: String*): ActorbaseCollection = {
    keys.foreach { key =>
      if(data.contains(key)) {
        data -= key
        val response = requestBuilder withCredentials(conn.username, conn.password) withUrl uri + "/collections/" + collectionName + "/" + key withMethod DELETE send()
        response.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case _ =>
        }
      }
    }
    ActorbaseCollection(owner, collectionName, data)
  }

  /**
    * Remove a key-value tuple, representing an ActorbaseObject from the
    * collection reflecting local changes to remote collection on server-side
    *
    * @param kv an ActorbaseObject parameter representing a key/value pair
    * @return an object of ActorbaseCollection containing the updated keys
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def remove[A >: Any](kv: ActorbaseObject[A]): ActorbaseCollection = this.remove(kv.keys.toSeq:_*)

  /**
    * Return all the contents of the collection in an ActorbaseObject
    *
    * @return an object of type ActorbaseObject
    * @throws
    */
  def find[A >: Any]: ActorbaseObject[A] = ActorbaseObject(data)

  /**
    * Find an arbitrary number of elements inside the collection, returning an
    * ActorbaseObject
    *
    *
    * @param keys a vararg String representing a sequence of keys to be retrieved
    * @return an object of type ActorbaseObject
    */
  def find[A >: Any](keys: String*): ActorbaseObject[A] = {
    var coll = TreeMap[String, Any]().empty
    keys.foreach { key =>
      if (data.contains(key))
        data.filterKeys(_ == key) map ( head => coll += head )
    }
    ActorbaseObject(coll.toMap)
  }

  /**
    * Find an element inside the collection, returning an ActorbaseObject
    * representing the key/value pair
    *
    * @param key a String representing the key associated to the value to be retrieved
    * @return an object of type ActorbaseObject
    */
  def findOne[A >: Any](key: String): Option[ActorbaseObject[A]] = {
    if (data.contains(key))
      Some(ActorbaseObject(key -> data.get(key).getOrElse(None)))
    else None
  }

  /**
    * Add a contributor to the collection, updating the remote system
    * counterpart
    *
    * @param username a String representing the username of the user
    * @param write a Boolean flag representing the permissions of the contributor
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedUsernameExc in case of username not found on the remote system
    * @throws UsernameAlreadyExistsExc in case of username already present inside the contributors list
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedUsernameExc])
  @throws(classOf[UsernameAlreadyExistsExc])
  def addContributor(username: String, write: Boolean = false): Unit = {
    val permission = if (!write) "read" else "readwrite"
    val response = requestBuilder withCredentials(conn.username, conn.password) withUrl uri + "/contributors/" + collectionName + "/" + permission withBody serialize2byteArray(username) withMethod POST send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          r.asInstanceOf[String] match {
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username: Actorbase does not contains such credential")
            case "UsernameAlreadyExists" => throw UsernameAlreadyExistsExc("Username already in contributors for the given collection")
          }
        }
    }
  }

  /**
    * Remove a contributor from the collection, updating the remote system
    * counterpart
    *
    * @param username a String representing the username of the user
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedUsernameExc in case of username not found on the remote system
    * @throws UsernameAlreadyExistsExc in case of username already present inside the contributors list
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def removeContributor(username: String): Unit = {
    val response = requestBuilder withCredentials(conn.username, conn.password) withUrl uri + "/contributors/" + collectionName + "/" + username withMethod DELETE send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
    }
  }

  /**
    * Drop the entire collection, reflecting the local change to remote on
    * server-side
    *
    * @param
    * @return
    * @throws
    */
  def drop: Unit = {
    data = data.empty
    val response = requestBuilder withCredentials(conn.username, conn.password) withUrl uri + "/collections/" + collectionName withMethod DELETE send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case _ =>
    }
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def count: Int = data.size

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def export(path: String): Unit = {
    val printWriter = new PrintWriter(new File(path))
    printWriter.write(serialize2JSON(this))
    printWriter.close
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def foreach(f: ((String, Any)) => Unit): Unit = data.foreach(f)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def withFilter(f: ((String, Any)) => Boolean): FilterMonadic[(String, Any), TreeMap[String, Any]] = data.withFilter(f)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  override def toString: String = {
    var headers = new TreeMap[String, Any]()
    headers += ("collection" -> collectionName)
    headers += ("owner" -> owner)
    headers += ("items" -> data)
    serialize2JSON4s(headers)
  }

}
