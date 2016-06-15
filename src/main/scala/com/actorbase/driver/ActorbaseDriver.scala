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

package com.actorbase.driver

import com.actorbase.driver.client.Connector
import com.actorbase.driver.client.api.RestMethods._
import com.actorbase.driver.client.api.RestMethods.Status._
import com.actorbase.driver.data.{ ActorbaseCollection, ActorbaseCollectionMap, ActorbaseObject }
import com.actorbase.driver.exceptions._
import scala.io.Source

import scala.util.parsing.json._
import scala.collection.immutable.TreeMap
import java.net.URI

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
      .withBody(password.getBytes)
      .withMethod(POST) send()
    request.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case _ =>
        var response = ""
        request.body map (x => response = x.asInstanceOf[String]) getOrElse (response = "None")
        if (response == "Admin" || response == "Common")
          new ActorbaseDriver(Connection(username, password, address, port))
        else throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
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
      .withBody(credentials(1).getBytes)
      .withMethod(POST) send()
    request.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case _ =>
        var response = ""
        request.body map (x => response = x.asInstanceOf[String]) getOrElse (response = "None")
        if (response == "Admin" || response == "Common")
          new ActorbaseDriver(Connection(credentials(0), credentials(1), uri.getHost, uri.getPort))
        else throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
    }
  }

  /**
    * Simple case class used to pass around connection info
    */
  case class Connection(username: String, password: String, address: String, port: Int)

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
              .withBody(serialize2byteArray(v))
              .addHeaders(("owner", owner))
              .withMethod(POST).send()
          else
            requestBuilder
              .withCredentials(connection.username, connection.password)
              .withUrl(uri + "/collections/" + collection + "/" + k)
              .withBody(serialize2byteArray(v))
              .addHeaders(("owner", owner))
              .withMethod(PUT).send()
        response.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case OK =>
            response.body map { x =>
              x.asInstanceOf[String] match {
                case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
                case "DuplicatedKey" => throw DuplicateKeyExc("Inserting duplicate key")
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
        .addHeaders(("owner", owner))
        .withMethod(DELETE).send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case OK =>
          response.body map { x =>
            x.asInstanceOf[String] match {
              case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
              case "NoPrivileges" => throw UndefinedCollectionExc("Insufficient permissions")
              case _ =>
            }
          }
        case _ =>
      }
    }
  }

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
  def find[A >: Any](key: String, collections: String*)(owner: String = connection.username): ActorbaseObject[A] = {
    var buffer = Map.empty[String, Any]
    collections.foreach { collectionName =>
      val response = requestBuilder
        .withCredentials(connection.username, connection.password)
        .withUrl(uri + "/collections/" + collectionName + "/" + key)
        .addHeaders(("owner", owner))
        .withMethod(GET).send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case NotFound =>
          response.body map { content =>
            content.asInstanceOf[String] match {
              case "UndefinedCollection" => throw UndefinedCollectionExc("Undefined collection")
              case "NoPrivilege" => throw WrongCredentialsExc("Insufficient permissions")
            }
          }
        case OK =>
          response.body map { content =>
            JSON.parseFull(content) map { jc =>
              buffer ++= Map(jc.asInstanceOf[Map[String, List[Double]]].transform((k, v) => deserializeFromByteArray(v.map(_.toByte).toArray)).toArray:_*)
            }
          } getOrElse (Map[String, Any]().empty)
        case _ =>
      }
    }
    ActorbaseObject(buffer)
  }

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
      .withBody(serialize2byteArray(newpassword))
      .addHeaders(("Old-password" , connection.password))
      .withMethod(POST).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { x =>
          x.asInstanceOf[String] match {
            case "WrongNewPassword" => throw WrongNewPasswordExc("The password inserted does not meet Actorbase criteria")
            case "UndefinedUsername" => throw UndefinedUsernameExc("Undefined username")
            case "OK" =>
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
  def listCollections : List[String] = {
    var collections = List.empty[String]
    val response =
      requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections" withMethod GET send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          JSON.parseFull(r) map { p =>
            val mapObject = p.asInstanceOf[Map[String, List[String]]]
            mapObject get "list" map (collections :::= _)
          }
        }
        collections
      case _ => List()
    }
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
    var collections = TreeMap.empty[String, ActorbaseCollection]
    listCollections map (x => collections += (x -> getCollection(x)))
    ActorbaseCollectionMap(collections)(connection, scheme)
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
    */
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def getCollection(collectionName: String, originalOwner: String = connection.username): ActorbaseCollection = {
    var buffer = TreeMap.empty[String, Any]
    var owner = ""
    val response = requestBuilder
      .withCredentials(connection.username, connection.password)
      .withUrl(uri + "/collections/" + collectionName)
      .addHeaders(("owner", originalOwner))
      .withMethod(GET).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case NotFound => throw UndefinedCollectionExc("Undefined collection")
      case OK =>
        response.body map { b =>
          JSON.parseFull(b) map { js =>
            val mapObject = js.asInstanceOf[Map[String, Any]]
            mapObject get "owner" map (x => owner = x.asInstanceOf[String])
            mapObject get "map" map { m =>
              buffer = TreeMap(m.asInstanceOf[Map[String, List[Double]]].transform((k, v) => deserializeFromByteArray(v.map(_.toByte).toArray)).toArray:_*)
            }
          }
        }
      case _ =>
    }
    ActorbaseCollection(owner, collectionName, buffer)(connection, scheme)
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
      .withUrl(uri + "/collections/" + collectionName)
      .addHeaders(("owner", owner))
      .withMethod(POST).send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        ActorbaseCollection(connection.username, collectionName)(connection, scheme) // stub owner
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
      .addHeaders(("owner", collection.owner))
      .withMethod(POST).send() // control response and add payload to post
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
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
  @throws(classOf[WrongCredentialsExc])
  @throws(classOf[InternalErrorExc])
  def dropCollections: Unit = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections" withMethod DELETE send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Credentials privilege level does not meet criteria needed to perform this operation")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case _ =>
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
  def dropCollections(collections: String*): Unit = {
    collections.foreach { collectionName =>
      val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collectionName withMethod DELETE send()
      response.statusCode match {
        case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
        case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
        case _ =>
      }
    }
  }

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
  def importFromFile(path: String)(owner: String = connection.username): Unit = {
    try {
      val json = Source.fromFile(path).getLines.mkString
      val mapObject = JSON.parseFull(json).get.asInstanceOf[Map[String, Any]]
      val collectionName = mapObject.get("collection").getOrElse("NoName")
      val buffer = mapObject.get("map").get.asInstanceOf[Map[String, Any]]
      buffer map { x =>
        val response = requestBuilder
          .withCredentials(connection.username, connection.password)
          .withUrl(uri + "/collections/" + collectionName + "/" + x._1)
          .withBody(serialize2byteArray(x._2))
          .addHeaders(("owner", owner))
          .withMethod(POST).send()
        response.statusCode match {
          case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
          case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
          case _ =>
        }
      }
    } catch {
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
  def exportToFile(path: String)(owner: String = connection.username): Unit = listCollections map (getCollection(_, owner).export(path))

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
    var users = List.empty[String]
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" withMethod GET send()
    response.statusCode match {
      case Unauthorized | Forbidden => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
      case Error => throw InternalErrorExc("There was an internal server error, something wrong happened")
      case OK =>
        response.body map { r =>
          JSON.parseFull(r) map { p =>
            val mapObject = p.asInstanceOf[Map[String, List[String]]]
            mapObject get "list" map (users :::= _)
          }
        }
        users
      case _ => List()
    }
  }

  /**
    * Shutdown the connection with the server
    */
  def logout() : Unit = ???

}
