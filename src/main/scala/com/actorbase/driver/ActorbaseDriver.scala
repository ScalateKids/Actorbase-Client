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

package com.actorbase.driver

import com.actorbase.driver.client.Connector
import com.actorbase.driver.client.api.RestMethods._
import com.actorbase.driver.client.api.RestMethods.Status._
import com.actorbase.driver.data.{ ActorbaseCollection, ActorbaseCollectionMap, ActorbaseObject }
import com.actorbase.driver.exceptions._

import org.json4s._
import org.json4s.jackson.JsonMethods._
// import org.json4s.JsonDSL._
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

import scala.io.Source
import scala.collection.immutable.TreeMap
import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global

case class SingleResponse(response: Any)

case class ListResponse(list: List[String])

// class StringTupleSerializer extends CustomSerializer[(String, String)](format => ( {
//   case JObject(List(JField(k, JString(v)))) => (k, v)
// }, {
//   case (s: String, t: String) => (s -> t)
// }))

case class ListTupleResponse(tuples: List[Map[String, String]])

case class CollectionResponse(owner: String, collectionName: String, contributors: Map[String, Boolean] = Map.empty[String, Boolean], data: Map[String, Any] = Map.empty[String, Any])

object ActorbaseDriver extends Connector {

  /**
    * Apply method for authentication to the Actorbase server
    *
    * @param username a String representing the username of the user, fallback to anonymous
    * @param password a String representing the password associated to the username, fallback to Actorb4se
    * @param address a String representing the ip-address (hostname) of the Actorbase instance running to connect with, fallback to localhost
    * @param port an Int representing the port of the Actorbase instance running to connect with, fallback to 9999
    * @param ssl a Boolean flag representing the adoption of encrypted connection, true means TLS/SSL adoption while false means plain HTTP connection, fallback to false
    * @return an instance of ActorbaseDriver, with administration methods only usable if credentials privilege level allows it
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def apply(username: String = "anonymous",
    password: String = "Actorb4se",
    address: String = "127.0.0.1",
    port: Int = 9999,
    ssl: Boolean = false): ActorbaseDriver = {
    val scheme = if (ssl) "https://" else "http://"
    val request = requestBuilder
      .withCredentials(username, password)
      .withUrl(scheme +  address + ":" + port + "/auth/" + username)
      .withBody(toBase64(password.getBytes("UTF-8")))
      .withMethod(POST) send()
    request.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Wrong credentials: username or password is not recognized by the system, or insufficient permissions")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case _ =>
        var response = ""
        request.body map (x => response = x.asInstanceOf[String]) getOrElse (response = "None")
        if (response != "None")
          new ActorbaseDriver(Connection(username, password, address, port))
        else throw WrongCredentialsExc("Wrong credentials: username or password is not recognized by the system, or insufficient permissions")
    }
  }

  /**
    * Apply method for authentication to the Actorbase server
    *
    * @param url a String representing the URL to connect with the Actorbase server
    * it must follow some rules: [scheme]://[username:password]@[address]:[port]
    * e.g "http://noname:nopass@my.domain:9999
    * @return an instance of ActorbaseDriver, with administration methods only usable if credentials privilege level allows it
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def apply(url: String): ActorbaseDriver = {
    val uri = new URI(url)
    implicit val scheme = uri.getScheme + "://"
    val credentials = uri.getUserInfo.split(":")
    val request = requestBuilder
      .withCredentials(credentials(0), credentials(1))
      .withUrl(scheme +  uri.getHost + ":" + uri.getPort + "/auth/" + credentials(0))
      .withBody(toBase64(credentials(1).getBytes("UTF-8")))
      .withMethod(POST) send()
    request.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Wrong credentials: username or password is not recognized by the system, or insufficient permissions")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case _ =>
        var response = ""
        request.body map (x => response = x.asInstanceOf[String]) getOrElse (response = "None")
        if (response != "None")
          new ActorbaseDriver(Connection(credentials(0), credentials(1), uri.getHost, uri.getPort))
        else throw WrongCredentialsExc("Wrong credentials: username or password is not recognized by the system, or insufficient permissions")
    }
  }

  /**
    * Simple case class used to pass around connection info
    */
  case class Connection(private var user: String, private var pass: String, address: String, port: Int) {
    def username: String = user
    def password: String = pass
    def setUsername(u: String): Unit = user = u
    def setPassword(p: String): Unit = pass = p
  }

}

/**
  * Main class of the driver component, representing an interface that expose
  * methods to use Actorbase from a Scala program.
  * This class allow all general operations of insert, remove, delete and find
  * of collections or items or both, exposing in addition "fast" methods to
  * perform operations directly on the remote side without having to query for
  * contents the system.
  *
  */
class ActorbaseDriver (val connection: ActorbaseDriver.Connection) (implicit val scheme: String = "http://") extends Connector {

  val uri: String = scheme + connection.address + ":" + connection.port

  /**
    * Authentication method, allow to insert credentials to be confirmed by
    * Actorbase server side
    *
    * @param username a String representing the username of the user
    * @param password a String representing the password associated to the username
    * @return an instance of the class ActorbaseDriver
    * @throws
    */
  def authenticate(username: String, password: String): ActorbaseDriver = {
    ActorbaseDriver(username, password, connection.address, connection.port)
  }

  /**
    * Insertion method, directly insert items without preventively querying the
    * system
    *
    * @param collection a String representing the collection uuid
    * @param update a Boolean flag, true means overwrite the item inserted, false otherwise
    * @param kv a Tuple2[String, Any] representing one or more key-value pair item to be inserted
    * @param owner a String representing the orignal owner of the collection
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedCollectionExc in case of a non-existant collection parameter
    * @throws DuplicateKeyExc in case of a duplicate key while inserting without update flag
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  @throws(classOf[DuplicateKeyExc])
  def insertTo(collection: String, update: Boolean, kv: (String, Any)*)(owner: String = connection.username): Unit = {
    kv.foreach {
      case (k, v) =>
        val response =
          if (!update)
            requestBuilder
              .withCredentials(connection.username, connection.password)
              .withUrl(uri + "/collections/" + collection + "/" + k)
              .withBody(serialize(v))
              .addHeaders("owner" -> toBase64FromString(owner))
              .withMethod(POST).send()
          else
            requestBuilder
              .withCredentials(connection.username, connection.password)
              .withUrl(uri + "/collections/" + collection + "/" + k)
              .withBody(serialize(v))
              .addHeaders("owner" -> toBase64FromString(owner))
              .withMethod(PUT).send()
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
                case _ =>
              }
            }
          case _ =>
        }
    }
  }

  /**
    * Overloaded insertion method, directly insert items without preventively
    * querying the system, accepting an ActorbaseObject as kv parameter, using
    * generic type A.
    *
    * @param collection a String representing the collection uuid
    * @param update a Boolean flag, true means overwrite the item inserted, false otherwise
    * @param kv an object of type ActorbaseObject[A] representing one or more key-value pair item to be inserted
    * @return no return value
    */
  def insertTo[A >: Any](collection: String, update: Boolean, kv: ActorbaseObject[A], owner: String): Unit =
    this.insertTo(collection, update, kv.toSeq:_*)(owner)

  /**
    * Uncurried version of the method insertTo, accept same parameters but
    * fallback owner to the current.
    *
    * @param collection a String representing the collection uuid
    * @param update a Boolean flag, true means overwrite the item inserted, false otherwise
    * @param kv a Tuple2[String, Any] representing one or more key-value pair item to be inserted
    * @return no return value
    */
  def insert(collection: String, update: Boolean, kv: (String, Any)*): Unit = insertTo(collection, update, kv:_*)(connection.username)

  /**
    * Remove method, directly remove items without preventively querying
    * the system, accepting a sequence of keys.
    *
    * @param collection a String representing the collection uuid
    * @param keys a vararg of String representing a sequence of keys designed for removal
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedCollectionExc in case of a non-existant collection parameter
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  def removeFrom(collection: String, keys: String*)(owner: String = connection.username): Unit = {
    keys.foreach { key =>
      val response = requestBuilder
        .withCredentials(connection.username, connection.password)
        .withUrl(uri + "/collections/" + collection + "/" + key)
        .addHeaders("owner" -> toBase64FromString(owner))
        .withMethod(DELETE).send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
        case BadRequest => throw InternalErrorExc("Invalid or malformed request")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case OK =>
          response.body map { x =>
            x.asInstanceOf[String] match {
              case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
              case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
              case _ =>
            }
          }
        case _ =>
      }
    }
  }

  /**
    * Uncurring version of remove method, directly remove items without preventively querying
    * the system, accepting a sequence of keys, but fallback owner to current one.
    *
    * @param collection a String representing the collection uuid
    * @param keys a vararg of String representing a sequence of keys designed for removal
    * @return no return value
    */
  def remove(collection: String, keys: String*): Unit = removeFrom(collection, keys:_*)(connection.username)

  /**
    * Find method, directly query the remote system without preventively requesting
    * the entire collection set. Accept a key and a sequence of collections to search
    * for.
    *
    * @param key a String representing a key designed for search
    * @param collections a vararg of String representing a sequence of uuid designed for search of the given key
    * @return an instance of ActorbaseObject[A] containing the results of the find operation
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def findFrom[A >: Any](key: String, collections: String*)(owner: String = connection.username): ActorbaseObject[A] = {
    implicit val formats = DefaultFormats
    var buffer = Map.empty[String, Any]
    collections.foreach { collectionName =>
      val response = requestBuilder
        .withCredentials(connection.username, connection.password)
        .withUrl(uri + "/collections/" + collectionName + "/" + key)
        .addHeaders("owner" -> toBase64FromString(owner))
        .withMethod(GET).send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
        case BadRequest => throw InternalErrorExc("Invalid or malformed request")
        // case NotFound =>
        //   response.body map { x =>
        //     x.asInstanceOf[String] match {
        //       case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
        //     }
        //   }
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case OK =>
          response.body map { content =>
            val ret = parse(content).extract[SingleResponse]
            buffer += (collectionName -> ret.response)
          } getOrElse (Map.empty[String, Any])
        case _ =>
      }
    }
    ActorbaseObject(buffer)
  }

  /**
    * Unurried version of find method, directly query the remote system without
    * preventively requesting the entire collection set. Accept a key and a
    * sequence of collections to search for, but owner fallback to current one.
    *
    * @param key a String representing a key designed for search
    * @param collections a vararg of String representing a sequence of uuid designed for search of the given key
    * @return an instance of ActorbaseObject[A] containing the results of the find operation
    */
  def find [A >: Any](key: String, collections: String*): ActorbaseObject[A] = findFrom(key, collections:_*)(connection.username)

  /**
    * Change the password associated to the user profile on the system
    *
    * @param newpassword a String representing the new password to be associated to the user profile
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws WrongNewPasswordExc in case of a new password that does not meet Actorbase criteria (e.g. at least one
    * uppercase character, at least one lowercase character, at least one digit)
    * @throws UndefinedUsernameExc in case of an undefined username
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[WrongNewPasswordExc])
  @throws(classOf[UndefinedUsernameExc])
  def changePassword(newpassword: String): Unit = {
    val response = requestBuilder.withCredentials(connection.username, connection.password)
      .withUrl(uri + "/private/" + connection.username)
      .withBody(toBase64(newpassword.getBytes("UTF-8")))
      .addHeaders("Old-password" -> toBase64FromString(connection.password))
      .withMethod(POST).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { x =>
          x.asInstanceOf[String] match {
            case "WrongNewPassword" => throw WrongNewPasswordExc("The password inserted does not meet Actorbase criteria")
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username")
            case "OK" => connection.setPassword(newpassword)
          }
        }
      case _ =>
    }
  }

  /**
    * Return a list of collections name stored remotely on the server
    *
    * @return a List[String] containing the collections names
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def listCollections : List[Map[String, String]] = {
    implicit val formats = DefaultFormats
    var collections = List.empty[Map[String, String]]
    val response =
      requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections" withMethod GET send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          val ret = parse(r).extract[ListTupleResponse]
          collections :::= ret.tuples
        }
      case _ => collections :::= List()
    }
    collections
  }

  /**
    * Return a list of collections, querying the system for the entire contents
    * generating an ActorbaseCollectionMap
    *
    * @return an ActorbaseCollectionMap containing a map of collections
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def getCollections: ActorbaseCollectionMap = {
    var colls = TreeMap.empty[String, ActorbaseCollection]
    try {
      listCollections map (x => colls += (x.head._2 -> getCollection(x.head._2, x.head._1)))
    } catch {
      case uce:UndefinedCollectionExc =>
    }
    ActorbaseCollectionMap(colls)(connection, scheme)
  }

  def asyncGetCollections: ActorbaseCollectionMap = {
    var colls = TreeMap.empty[String, ActorbaseCollection]
    val collections = listCollections map (x => (x.head._1 -> x.head._2))
    val futureList = Future.traverse(collections)(elem =>
      Future {
        var collls = Map.empty[String, ActorbaseCollection]
        try {
          collls += (elem._2 -> getCollection(elem._2, elem._1))
        } catch {
          case uce:UndefinedCollectionExc =>
        }
        collls
      })
    val listOfFutures = futureList.map(x => x.map (colls ++= _))
    Await.result(listOfFutures, Duration.Inf)
    ActorbaseCollectionMap(colls)(connection, scheme)
  }

  /**
    * Return a list of collections, querying the system for the entire contents
    * generating an ActorbaseCollectionMap
    *
    * @return an ActorbaseCollectionMap containing a map of collections
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def getCollections(collections: String*): ActorbaseCollectionMap = {
    var colls = TreeMap.empty[String, ActorbaseCollection]
    collections.foreach { c =>
      try {
        colls += (c -> getCollection(c))
      } catch {
        case uce:UndefinedCollectionExc =>
      }
    }
    ActorbaseCollectionMap(colls)(connection, scheme)
  }

  /**
    * Retrieves an entire collection from server given the name. A collection is
    * composed by an owner, a collection name and a list of key/value pairs.
    * Keys are represented as String, value can be anything, from primitive
    * types to custom objects. This tuples are stored inside the server as array
    * of bytes; sending a getCollection request call for a marshaller on server
    * side that convert to JSON string all contents of the collection requested,
    * then, the value as Array[Byte] stream is deserialized to the original
    * object stored inside the database.
    *
    * @param collectionName a String representing the collection to fetch
    * @param owner a String representing the collection owner
    * @return an object of type ActorbaseCollection, traversable with foreach,
    * containing a list of ActorbaseObject, representing key/value type object
    * of Actorbase
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedCollectionExc in case of undefined collection on the server side
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedCollectionExc])
  def getCollection(collectionName: String, originalOwner: String = connection.username): ActorbaseCollection = {
    implicit val formats = DefaultFormats
    var buffer = TreeMap.empty[String, Any]
    var owner = ""
    var ret: CollectionResponse = null
    val response = requestBuilder
      .withCredentials(connection.username, connection.password)
      .withUrl(uri + "/collections/" + collectionName)
      .addHeaders("owner" -> toBase64FromString(originalOwner))
      .withMethod(GET).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case NotFound => throw UndefinedCollectionExc("Undefined collection")
      case OK => response.body map (b => ret = parse(b).extract[CollectionResponse])
      case _ =>
    }
    ActorbaseCollection(ret.owner, ret.collectionName, ret.contributors, TreeMap(ret.data.toArray:_*))(connection, scheme)
  }


  /**
    * Add a collection on server side of Actorbase
    *
    * @param collectionName a String representing the collection name to be added to the system
    * @return an ActorbaseCollection representing the collection currently added to the system
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def addCollection(collectionName: String, owner: String = connection.username): ActorbaseCollection = {
    val response = requestBuilder
      .withCredentials(connection.username, connection.password)
      .withUrl(uri + "/collections/" + urlEncode(collectionName))
      .addHeaders("owner" -> toBase64FromString(owner))
      .withMethod(POST).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK => ActorbaseCollection(connection.username, collectionName)(connection, scheme) // stub owner
    }
  }

  /**
    * Add a collection on server side of Actorbase
    *
    * @param collection an object ActorbaseCollection
    * @return an object ActorbaseCollection
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def addCollection(collection: ActorbaseCollection): ActorbaseCollection = {
    val response = requestBuilder
      .withCredentials(connection.username, connection.password)
      .withUrl(uri + "/collections/" + collection.collectionName)
      .addHeaders("owner" -> toBase64FromString(collection.owner))
      .withMethod(POST).send() // control response and add payload to post
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK => collection
    }
  }

  /**
    * Wipe out the entire database by dropping every collection inside the
    * system
    *
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  def dropCollections: Unit = {
    if (connection.username != "admin")
      listCollections map (x => dropCollections(x.head._2))
    else listCollections map { x =>
      listUsers map (u => dropCollectionsFrom(x.head._2)(u))
    }
  }

  /**
    * Drop one or more specified collections from the database, silently fail in
    * case of no match of the specified collections
    *
    * @param collections a vararg of String, represents a sequence of collections to be removed from the system
    * @return Unit, no return value
    * @throws WrongCredentialsExc in case of unathorized section reply from the system
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def dropCollectionsFrom(collections: String*)(owner: String = connection.username): Unit = {
    collections.foreach { collectionName =>
      val response = requestBuilder
        .withCredentials(connection.username, connection.password)
        .withUrl(uri + "/collections/" + collectionName)
        .addHeaders("owner" -> toBase64FromString(owner))
        .withMethod(DELETE).send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
        case BadRequest => throw InternalErrorExc("Invalid or malformed request")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case _ =>
      }
    }
  }

  /**
    * Uncurried version of the method dropCollectionsFrom, drop one or more
    * specified collections from the database, silently fail in case of no match
    * of the specified collections, but fallback owner to the current one.
    *
    * @param collections a vararg of String, represents a sequence of collections to be removed from the system
    * @return Unit, no return value
    */
  def dropCollections(collections: String*): Unit = dropCollectionsFrom(collections:_*)(connection.username)

  /**
    * Import a sequence of collections from a JSON file located into the
    * filesystem at a given path.
    *
    * @param path a String representing a folder into the filesystem
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws MalformedFileExc in case of a not well balanced JSON file or a non-existant file at the given path
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[MalformedFileExc])
  def importData(path: String): Unit = {
    implicit val formats = DefaultFormats
    try {
      val json = Source.fromFile(path).getLines.mkString
      val mapObject = parse(json).extract[CollectionResponse]
      val collectionName = mapObject.collectionName
      val buffer = mapObject.data
      val contributors = mapObject.contributors
      val owner = mapObject.owner
      buffer map { x =>
        val response = requestBuilder
          .withCredentials(connection.username, connection.password)
          .withUrl(uri + "/collections/" + collectionName + "/" + x._1)
          .withBody(serialize(x._2))
          .addHeaders("owner" -> toBase64FromString(owner))
          .withMethod(POST).send()
        response.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
          case BadRequest => throw InternalErrorExc("Invalid or malformed request")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case OK =>
            response.body map { x =>
              x.asInstanceOf[String] match {
                case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
                case "OK" =>
              }
            }
        }
      }
      contributors map ( x => addContributorTo(x._1, collectionName, x._2, owner))
    } catch {
      case jpe: com.fasterxml.jackson.core.JsonParseException => throw MalformedFileExc("Malformed json file")
      case nse: NoSuchElementException => throw MalformedFileExc("Malformed json file")
      case wce: WrongCredentialsExc => throw wce
      case mfe: MalformedFileExc => throw mfe
      case err: InternalErrorExc => throw err
      case und: UndefinedCollectionExc => throw und
    }
  }

  /**
    * Export all the collections owned or in-contribution by the user on the filesystem
    * at a specified path in JSON format.
    *
    * @param path a String representing a folder into the filesystem
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws MalformedFileExc in case of a not well balanced JSON file or a non-existant file at the given path
    */
  def exportData(path: String, owner: String = connection.username): Unit = {
    listCollections map { x =>
      try {
        getCollection(x.head._2, owner).export(path)
      } catch {
        case uce:UndefinedCollectionExc =>
      }
    }
  }

  /**
    * Add a contributor to a collection, without preventively query the server-side of the
    * system
    *
    * @param username a String representing the username designed to become a contributor to the
    * collection of choice
    * @param collection a String representing the collection designed to receive a contributor
    * @param writePermission a Boolean representing wether the contributor must have read-write permissions
    * or just read permissions on the collection. <code>true</code> means read-write permissions, false
    * only read
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws MalformedFileExc in case of a not well balanced JSON file or a non-existant file at the given path
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[MalformedFileExc])
  def addContributorTo(username: String, collection: String, writePermission: Boolean, owner: String = connection.username): Unit = {
    val permissions = if (writePermission) "readwrite" else "read"
    val response = requestBuilder
      .withCredentials(connection.username, connection.password)
      .withUrl(uri + "/contributors/" + collection)
      .withBody(toBase64(username.getBytes("UTF-8")))
      .addHeaders("owner"-> toBase64FromString(owner), "permission" -> toBase64FromString(permissions))
      .withMethod(POST).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          r.asInstanceOf[String] match {
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username: Actorbase does not contains such credential")
            case "UsernameAlreadyExists" => throw UsernameAlreadyExistsExc("Username already in contributors for the given collection")
            case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
            case "OK" =>
          }
        }
    }
  }

  /**
    * Remove a contributor from a collection, without preventively query the server-side of the
    * system
    *
    * @param username a String representing the username designed for remove from the
    * collection of choice
    * @param collection a String representing the collection designed to remove the contributor from
    * @param writePermission a Boolean representing wether the contributor must have read-write permissions
    * or just read permissions on the collection. <code>true</code> means read-write permissions, false
    * only read
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws MalformedFileExc in case of a not well balanced JSON file or a non-existant file at the given path
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[MalformedFileExc])
  def removeContributorFrom(username: String, collection: String, owner: String = connection.username): Unit = {
    val response = requestBuilder
      .withCredentials(connection.username, connection.password)
      .withUrl(uri + "/contributors/" + collection)
      .withBody(toBase64(username.getBytes("UTF-8")))
      .addHeaders("owner" -> toBase64FromString(owner))
      .withMethod(DELETE).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          r.asInstanceOf[String] match {
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username: Actorbase does not contains such credential")
            case "UsernameAlreadyExists" => throw UsernameAlreadyExistsExc("Username already in contributors for the given collection")
            case "NoPrivileges" => throw WrongCredentialsExc("Insufficient permissions")
            case "OK" =>
          }
        }
    }
  }

  /**
    * Add a new user to the system with the given username and the Actorbase
    * standard password
    *
    * @param username a String representing the username of a new user profile
    * to be added to the system
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UsernameAlreadyExistsExc in case of username already present inside Actorbase system
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UsernameAlreadyExistsExc])
  def addUser(username: String): Unit = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" + username withMethod POST send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          r.asInstanceOf[String] match {
            case "UsernameAlreadyExists" => throw UsernameAlreadyExistsExc("Username already exists in the system Actorbase")
            case "OK" =>
          }
        }
      case _ =>
    }
  }

  /**
    * Remove a user from the system represented by a given username.
    *
    * @param username a String representing the username of the user
    * designed for removal.
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedUsernameExc in case of username not found on the remote system
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedUsernameExc])
  def removeUser(username: String): Unit = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" + username withMethod DELETE send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          r.asInstanceOf[String] match {
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username: Actorbase does not contains such credential")
            case "OK" =>
          }
        }
      case _ =>
    }
  }

  /**
    * Reset the password of a user by a given username to the standard one of
    * Actorbase
    *
    * @param username a String representing the username of the user designed
    * for password reset
    * @return no return value
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    * @throws UndefinedUsernameExc in case of username not found on the remote system
    * @throws UsernameAlreadyExistsExc in case of username already present inside Actorbase system
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  @throws(classOf[UndefinedUsernameExc])
  @throws(classOf[UsernameAlreadyExistsExc])
  def resetPassword(username: String): Unit = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" + username withMethod PUT send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          r.asInstanceOf[String] match {
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username: Actorbase does not contains such credential")
            case "UsernameAlreadyExists" => throw UsernameAlreadyExistsExc("Username already exists in the system Actorbase")
            case "OK" =>
          }
        }
      case _  =>
    }
  }

  /**
    * Lists all users inside the remote system
    *
    * @return a List[String] containing the usernames of the users
    * @throws WrongCredentialsExc in case of wrong username or password, or non-existant ones
    * @throws InternalErrorExc in case of internal server error
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def listUsers: List[String] = {
    implicit val formats = DefaultFormats
    var users = List.empty[String]
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" withMethod GET send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
      case BadRequest => throw InternalErrorExc("Invalid or malformed request")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          val ret = parse(r).extract[ListResponse]
          users :::= ret.list
        }
        users
      case _ => List()
    }
  }

}
