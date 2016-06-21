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
  * @author Scalatekids
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.driver.data

import com.actorbase.driver.ActorbaseDriver.Connection
import com.actorbase.driver.client.Connector
import com.actorbase.driver.client.api.RestMethods._
import com.actorbase.driver.client.api.RestMethods.Status._
import com.actorbase.driver.exceptions._

import scalaj.http.HttpConstants._

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
        val response = requestBuilder
          .withCredentials(conn.username, conn.password)
          .withUrl(uri + "/collections/" + collectionName + "/" + k)
          .withBody(serialize2byteArray(v))
          .addHeaders(("owner", base64FromString(owner)))
          .withMethod(POST).send()
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
      if(data.contains(k)) {
        data -= k
        data += (k -> v)
        val response = requestBuilder
          .withCredentials(conn.username, conn.password)
          .withUrl(uri + "/collections/" + collectionName + "/" + k)
          .withBody(serialize2byteArray(v))
          .addHeaders(("owner", base64FromString(owner)))
          .withMethod(PUT).send()
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
    val response = requestBuilder
      .withCredentials(conn.username, conn.password)
      .withUrl(uri + "/contributors/" + collectionName)
      .withBody(base64(username.getBytes("UTF-8")))
      .addHeaders(("permission", base64FromString(permission)))
      .withMethod(POST).send()
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
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
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
    * Count the number of items inside this collection
    *
    * @return an Int, represents the number of key-value pair
    */
  def count: Int = data.size

  /**
    * Export the contente of the current collection to a given path on
    * the filesystem, JSON formatted
    *
    * @param path a String representing the path on the filesystem where
    * the JSON file will be saved
    * @return no return value
    * @throws
    */
  def export(path: String): Unit = {
    val printWriter = new PrintWriter(new File(path))
    printWriter.write(serialize2JSON(this))
    printWriter.close
  }

  /**
    * Foreach method, applies a function f to all elements of this map.
    *
    * @param f the function that is applied for its side-effect to every element.
    * The result of function f is discarded.
    * @return no return value
    */
  def foreach(f: ((String, Any)) => Unit): Unit = data.foreach(f)

  /**
    * Creates a non-strict filter of this traversable collection.
    *
    * @param p the predicate used to test elements.
    * @return an object of class WithFilter, which supports map, flatMap, foreach,
    * and withFilter operations. All these operations apply to those elements of
    * this traversable collection which satisfy the predicate p.
    */
  def withFilter(p: ((String, Any)) => Boolean): FilterMonadic[(String, Any), TreeMap[String, Any]] = data.withFilter(p)

  /**
    * Converts this collection to a string.
    *
    * @param
    * @return a string representation of this collection. By default this string
    * consists of a JSON containing the colleciton name, the owner and items
    */
  override def toString: String = {
    var headers = new TreeMap[String, Any]()
    headers += ("collection" -> collectionName)
    headers += ("owner" -> owner)
    headers += ("items" -> data)
    serialize2JSON4s(headers)
  }

}
