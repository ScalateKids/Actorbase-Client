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

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.JsonDSL._
import java.io.{FileOutputStream, File, PrintWriter}
import scala.collection.immutable.TreeMap
import scala.collection.generic.FilterMonadic
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

case class SingleResponse(response: Any)

/**
  * Class representing a single collection of the database at the current state,
  * that is the state at the very moment of the query time of the system. It
  * contains key-value pairs representing items of the collection and exposes
  * some utility methods to navigate and modify the contents in an easier way,
  * reflecting all changes directly to the remote counterpart.
  */
case class ActorbaseCollection
  (val owner: String, var collectionName: String,
    var contributors: Map[String, Boolean] = Map.empty[String, Boolean],
    var data: TreeMap[String, Any] = new TreeMap[String, Any]())(implicit val conn: Connection, implicit val scheme: String)
    extends Connector {

  implicit val formats = DefaultFormats
  val uri: String = scheme + conn.address + ":" + conn.port

  /**
    * Insert an arbitrary variable number of key-value tuple to the collection
    * reflecting local changes to remote collection on server-side
    *
    * @param kv a vararg Tuple2 of type (String, Any)
    * @return an object of type ActorbaseCollection representing the collection updated
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedCollectionExc in case of undefined collection
    * @throws DuplicateKeyExc in case of duplicate key
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  @throws(classOf[DuplicateKeyExc])
  def insert(kv: (String, Any)*): ActorbaseCollection = {
    for((k, v) <- kv) {
      val response = requestBuilder
        .withCredentials(conn.username, conn.password)
        .withUrl(uri + "/collections/" + collectionName + "/" + k)
        .withBody(serialize(v))
        .addHeaders("owner" -> toBase64FromString(owner))
        .withMethod(POST).send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case BadRequest => throw InternalErrorExc("Invalid or malformed request")
        case OK =>
          response.body map { x =>
            x.asInstanceOf[String] match {
              case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
              case "DuplicatedKey" => throw DuplicateKeyExc("Inserting duplicate key")
              case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
              case _ => data += (k -> v)
            }
          }
        case _ =>
      }
    }
    ActorbaseCollection(owner, collectionName, contributors, data)
  }

  /**
    * Service method, provide async requests in case of kv length > 1
    *
    * @param kv a vararg Tuple2 of type (String, Any)
    * @return an object of type ActorbaseCollection representing the collection updated
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedCollectionExc in case of undefined collection
    * @throws DuplicateKeyExc in case of duplicate key
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  @throws(classOf[DuplicateKeyExc])
  def asyncInsert(kv: (String, Any)*): ActorbaseCollection = {
    val futureList = Future.traverse(kv)(keyVal =>
      Future {
        (keyVal._1 -> keyVal._2 -> requestBuilder
          .withCredentials(conn.username, conn.password)
          .withUrl(uri + "/collections/" + collectionName + "/" + keyVal._1)
          .withBody(serialize(keyVal._2))
          .addHeaders("owner" -> toBase64FromString(owner))
          .withMethod(POST).send())
      })
    val listOfFutures = futureList.map { x =>
      x map { response =>
        response._2.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case BadRequest => throw InternalErrorExc("Invalid or malformed request")
          case OK =>
            response._2.body map { x =>
              x.asInstanceOf[String] match {
                case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
                case "DuplicatedKey" => throw DuplicateKeyExc("Inserting duplicate key")
                case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
                case _ => data += (response._1._1 -> response._1._2)
              }
            }
          case _ =>
        }
      }
    }
    Await.result(listOfFutures, Duration.Inf)
    ActorbaseCollection(owner, collectionName, contributors, data)
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
    * @throws UndefinedCollectionExc in case of undefined collection
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  def update(kv: (String, Any)*): ActorbaseCollection = {
    for ((k, v) <- kv) {
      val response = requestBuilder
        .withCredentials(conn.username, conn.password)
        .withUrl(uri + "/collections/" + collectionName + "/" + k)
        .withBody(serialize(v))
        .addHeaders("owner" -> toBase64FromString(owner))
        .withMethod(PUT).send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case OK =>
          response.body map { x =>
            x.asInstanceOf[String] match {
              case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
              case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
              case _ =>
                data -= k
                data += (k -> v)
            }
          }
        case _ =>
      }
    }
    ActorbaseCollection(owner, collectionName, contributors, data)
  }

  /**
    * Service method, provide async requests in case of kv length > 1
    *
    * @param kv a vararg of Tuple2[String, Any] representing key-value pairs to be updated in the system
    * @return an object of ActorbaseCollection containing the updated keys
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedCollectionExc in case of undefined collection
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  def asyncUpdate(kv: (String, Any)*): ActorbaseCollection = {
    val futureList = Future.traverse(kv)(keyVal =>
      Future {
        (keyVal._1 -> keyVal._2 -> requestBuilder
          .withCredentials(conn.username, conn.password)
          .withUrl(uri + "/collections/" + collectionName + "/" + keyVal._1)
          .withBody(serialize(keyVal._2))
          .addHeaders("owner" -> toBase64FromString(owner))
          .withMethod(PUT).send())
      })
    val listOfFutures = futureList.map { x =>
      x map { response =>
        response._2.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case BadRequest => throw InternalErrorExc("Invalid or malformed request")
          case OK =>
            response._2.body map { x =>
              x.asInstanceOf[String] match {
                case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
                case "DuplicatedKey" => throw DuplicateKeyExc("Inserting duplicate key")
                case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
                case _ =>
                  data -= response._1._1
                  data += (response._1._1 -> response._1._2)
              }
            }
          case _ =>
        }
      }
    }
    Await.result(listOfFutures, Duration.Inf)
    ActorbaseCollection(owner, collectionName, contributors, data)
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
    * @throws UndefinedCollectionExc in case of undefined collection
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  def remove(keys: String*): ActorbaseCollection = {
    keys.foreach { key =>
      val response = requestBuilder withCredentials(conn.username, conn.password) withUrl uri + "/collections/" + collectionName + "/" + key withMethod DELETE send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case OK =>
          response.body map { x =>
            x.asInstanceOf[String] match {
              case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
              case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
              case _ => data -= key
            }
          }
        case _ =>
      }
    }
    ActorbaseCollection(owner, collectionName, contributors, data)
  }

  /**
    * Service method, provide async request in case of keys > 1
    *
    * @param keys a vararg String representing a sequence of keys to be removed
    * from the collection
    * @return an object of ActorbaseCollection containing the updated keys
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedCollectionExc in case of undefined collection
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  def asyncRemove(keys: String*): ActorbaseCollection = {
    val futureList = Future.traverse(keys)(key =>
      Future {
        (key -> requestBuilder.withCredentials(conn.username, conn.password).withUrl(uri + "/collections/" + collectionName + "/" + key).withMethod(DELETE).send())
      })
    val listOfFutures = futureList.map { x =>
      x map { response =>
        response._2.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case OK =>
            response._2.body map { x =>
              x.asInstanceOf[String] match {
                case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
                case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
                case _ => data -= response._1
              }
            }
          case _ =>
        }
      }
    }
    Await.result(listOfFutures, Duration.Inf)
    ActorbaseCollection(owner, collectionName, contributors, data)
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
  def find[A >: Any]: ActorbaseObject[A] = find()

  /**
    * Find an arbitrary number of elements inside the collection, returning an
    * ActorbaseObject, return all then contents of the collection if the vararg
    * passed asinstanceof parameter is empty
    *
    * @param keys a vararg String representing a sequence of keys to be retrieved
    * @return an object of type ActorbaseObject
    * @throws WrongCredentialsExc in case of credentials not valid
    * @throws InternalErrorExc in case of internal error on the server side
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def find[A >: Any](keys: String*): ActorbaseObject[A] = {
    if(keys.length == 0) ActorbaseObject(data)
    else {
      var buffer = TreeMap[String, Any]().empty
      keys.foreach { key =>
        if (data.contains(key))
          data get key map (k => buffer += (key -> k))
        else {
          val response = requestBuilder
            .withCredentials(conn.username, conn.password)
            .withUrl(uri + "/collections/" + collectionName + "/" + key)
            .addHeaders("owner" -> toBase64FromString(owner))
            .withMethod(GET).send()
          response.statusCode match {
            case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
            case BadRequest => throw InternalErrorExc("Invalid or malformed request")
            case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
            case OK =>
              response.body map { content =>
                val ret = parse(content).extract[SingleResponse]
                buffer += (key -> ret.response)
              }
            case _ =>
          }
        }
      }
      data ++= buffer
      ActorbaseObject(buffer.toMap)
    }
  }

  /**
    * Service method, provide async requests in case of keys > 1
    *
    * @param keys a vararg String representing a sequence of keys to be retrieved
    * @return an object of type ActorbaseObject
    * @throws WrongCredentialsExc in case of credentials not valid
    * @throws InternalErrorExc in case of internal error on the server side
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def asyncFind[A >: Any](keys: String*): ActorbaseObject[A] = {
    var buffer = TreeMap.empty[String, Any]
    val futureList = Future.traverse(keys)(key =>
      Future {
        (key -> requestBuilder
          .withCredentials(conn.username, conn.password)
          .withUrl(uri + "/collections/" + collectionName + "/" + key)
          .addHeaders("owner" -> toBase64FromString(owner))
          .withMethod(GET).send())
      })
    val listOfFutures = futureList.map { x =>
      x map { response =>
        response._2.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
          case BadRequest => throw InternalErrorExc("Invalid or malformed request")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case OK =>
            response._2.body map { content =>
              val ret = parse(content).extract[SingleResponse]
              buffer += (response._1 -> ret.response)
            }
          case _ =>
        }
      }
    }
    Await.result(listOfFutures, Duration.Inf)
    data ++= buffer
    ActorbaseObject(buffer.toMap)
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
    else {
      var buffer: Option[ActorbaseObject[A]] = None
      val response = requestBuilder
        .withCredentials(conn.username, conn.password)
        .withUrl(uri + "/collections/" + collectionName + "/" + key)
        .addHeaders("owner" -> toBase64FromString(owner))
        .withMethod(GET).send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
        case BadRequest => throw InternalErrorExc("Invalid or malformed request")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case OK =>
          response.body map { content =>
            val ret = parse(content).extract[SingleResponse]
            buffer = Some(ActorbaseObject(key -> ret.response))
          }
        case _ =>
      }
      buffer
    }
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
      .withBody(toBase64(username.getBytes("UTF-8")))
      .addHeaders("permission" -> toBase64FromString(permission), "owner" -> toBase64FromString(conn.username))
      .withMethod(POST).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          r.asInstanceOf[String] match {
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username: Actorbase does not contains such credential")
            case "UsernameAlreadyExists" => throw UsernameAlreadyExistsExc("Username already in contributors for the given collection")
            case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
            case "OK" => if (!contributors.contains(username)) contributors += (username -> write)
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
    val response = requestBuilder
      .withCredentials(conn.username, conn.password)
      .withUrl(uri + "/contributors/" + collectionName)
      .addHeaders("owner" -> toBase64FromString(conn.username))
      .withBody(toBase64(username.getBytes("UTF-8")))
      .withMethod(DELETE).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          r.asInstanceOf[String] match {
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username: Actorbase does not contains such credential")
            case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
            case "OK" => if (contributors contains username) contributors -= username
          }
        }
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
    * @param  append if false this method will overwrite the file that's already in the given path,
    *                   if true it will append the exported collection to the end of the file
    * @return no return value
    */
  def export(path: String, append: Boolean = false): Unit = {
    val exportTo = new File(path)
    if (!exportTo.exists)
      try{
        exportTo.getParentFile.mkdirs
      } catch {
        case np: NullPointerException =>
      }
    // printWriter.write(serialize2JSON(this))
    if(!append){ //if append is false it overwrites everything on the file
      val printWriter = new PrintWriter(exportTo)
      printWriter.write(toString)
      printWriter.close
    }
    else{ //append==true, it adds the collection to the end of the file
      val printWriter = new PrintWriter(new FileOutputStream(exportTo, true))
      printWriter.append(",\n")
      printWriter.append(toString)
      printWriter.close
    }
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
    headers += ("collectionName" -> collectionName)
    headers += ("owner" -> owner)
    headers += ("contributors" -> contributors)
    headers += ("data" -> data)
    toJSON(headers)
  }

}
