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
import com.actorbase.driver.exceptions.{ MalformedFileExc, WrongCredentialsExc }
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
    */
  @throws(classOf[WrongCredentialsExc])
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
    */
  @throws(classOf[WrongCredentialsExc])
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
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseDriver (val connection: ActorbaseDriver.Connection) (implicit val scheme: String = "http://") extends Connector {

  val uri: String = scheme + connection.address + ":" + connection.port

  // implicit val conn = connection
  // implicit val sche = scheme

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def authenticate(username: String, password: String): ActorbaseDriver = {
    ActorbaseDriver(username, password, connection.address, connection.port)
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def insertTo(collection: String, update: Boolean, kv: (String, Any)*): Unit = {
    kv.foreach {
      case (k, v) =>
        if (!update)
          requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collection + "/" + k withBody serialize2byteArray(v) withMethod POST send()
        else
          requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collection + "/" + k withBody serialize2byteArray(v) withMethod PUT send()
    }
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def insertTo[A >: Any](collection: String, update: Boolean, kv: ActorbaseObject[A]): Unit = this.insertTo(collection, update, kv.toSeq:_*)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def removeFrom(collection: String, keys: String*): Boolean = {
    keys.foreach { key =>
      requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collection + "/" + key withMethod DELETE send()
    }
    true // must be checked / exceptions
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def find[A >: Any](key: String, collections: String*): ActorbaseObject[A] = {
    var buffer = Map.empty[String, Any]
    collections.foreach { collectionName =>
      val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collectionName + "/" + key withMethod GET send()
      if (response.statusCode == OK)
        response.body map { content =>
          JSON.parseFull(content) map { jc =>
            buffer ++= Map(jc.asInstanceOf[Map[String, List[Double]]].transform((k, v) => deserializeFromByteArray(v.map(_.toByte).toArray)).toArray:_*)
          }
          // buffer ++= Map(JSON.parseFull(content).get.asInstanceOf[Map[String, List[Double]]].transform((k, v) => deserializeFromByteArray(v.map(_.toByte).toArray)).toArray:_*)
        } getOrElse (Map[String, Any]().empty)
    }
    ActorbaseObject(buffer)
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def changePassword(newpassword: String): Unit = {
    requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/private/" + connection.username withBody serialize2byteArray(newpassword)  withMethod POST send()
  }

  /**
    * Return a list of collection name stored remotely on the server
    *
    * @param
    * @return a List[String] contained the collection names
    * @throws
    */
  def listCollections : List[String] = {
    var collections = List.empty[String]
    val response =
      requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/listcollections" withMethod GET send()
    if(response.statusCode == OK) {
      response.body map { r =>
        JSON.parseFull(r) map { p =>
          val mapObject = p.asInstanceOf[Map[String, List[String]]]
          mapObject get "list" map (collections :::= _)
        }
      }
      collections
    }
    else List()
  }

  /**
    * Return a list of collections, consider an object ActorbaseCollectionMap
    *
    * @param
    * @return an ActorbaseCollectionMap containing a map of collections
    * @throws
    */
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
    * @return an object of type ActorbaseCollection, traversable with foreach,
    * containing a list of ActorbaseObject, representing key/value type object
    * of Actorbase
    * @throws
    */
  def getCollection(collectionName: String): ActorbaseCollection = {
    var buffer: TreeMap[String, Any] = TreeMap[String, Any]().empty
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collectionName + "/" withMethod GET send()
    if (response.statusCode == OK) {
      val mapObject = JSON.parseFull(response.body.get).get.asInstanceOf[Map[String, Any]]
      mapObject get "map" map { m =>
        buffer = TreeMap(m.asInstanceOf[Map[String, List[Double]]].transform((k, v) => deserializeFromByteArray(v.map(_.toByte).toArray)).toArray:_*)
      }
      // buffer = TreeMap(mapObject.get("map").get.asInstanceOf[Map[String, List[Double]]].transform((k, v) => deserializeFromByteArray(v.map(_.toByte).toArray)).toArray:_*)
    }
    ActorbaseCollection("anonymous", collectionName, buffer)(connection, scheme)
  }

  /**
    * Add a collection on server side of Actorbase
    *
    * @param collectionName a String representing the collection name to be retrieved from the server
    * @return an ActorbaseCollection representing a collection stored on Actorbase
    * @throws
    */
  def addCollection(collectionName: String): ActorbaseCollection = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collectionName withMethod POST send() // control response
    ActorbaseCollection(connection.username, collectionName)(connection, scheme) // stub owner
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def addCollection(collection: ActorbaseCollection): ActorbaseCollection = {
    val response =
      requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collection.collectionName withMethod POST send() // control response and add payload to post
    collection
  }

  /**
    * Wipe out the entire database by dropping every collection inside the
    * system
    *
    * @param
    * @return
    * @throws
    */
  def dropCollections: Boolean = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections" withMethod DELETE send()
    if(response.statusCode != OK)
      false
    else true
  }

  /**
    * Drop one or more specified collections from the database, silently fail in
    * case of no match of the specified collections
    *
    * @param collections a vararg of String, represents a sequence of collections to be removed from the system
    * @return Unit, no return value
    * @throws WrongCredentialsExc in case of unathorized section reply from the system
    */
  @throws(classOf[WrongCredentialsExc])
  def dropCollections(collections: String*): Boolean = {
    collections.foreach { collectionName =>
      val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collectionName withMethod DELETE send()
      response.statusCode match {
        case 401 | 403 => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
        case _ => // all ok
      }
    }
    true // to be checked / exc
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  @throws(classOf[MalformedFileExc])
  def importFromFile(path: String): Boolean = {
    try {
      val json = Source.fromFile(path).getLines.mkString
      val mapObject = JSON.parseFull(json).get.asInstanceOf[Map[String, Any]]
      val collectionName = mapObject.get("collection").getOrElse("NoName")
      val buffer = mapObject.get("map").get.asInstanceOf[Map[String, Any]]
      buffer map { x =>
        val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/collections/" + collectionName + "/" + x._1 withBody serialize2byteArray(x._2) withMethod POST send()
        response.statusCode match {
          case 401 | 403 => throw WrongCredentialsExc("Attempted a request without providing valid credentials")
          case _ => // all ok
        }
      } //getOrElse throw MalformedFileExc("Malformed json file")
    } catch {
      case nse: NoSuchElementException => throw MalformedFileExc("Malformed json file")
      case wce: WrongCredentialsExc => throw wce
      case mfe: MalformedFileExc => throw mfe
    }
    true
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def exportToFile(path: String): Boolean = {
    listCollections map (getCollection(_).export(path))
    true
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def addUser(username: String): Boolean = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" + username withMethod POST send()
    if (response.statusCode == OK) true
    else false
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def removeUser(username: String): Boolean = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" + username withMethod DELETE send()
    if (response.statusCode == OK) true
    else false
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def resetPassword(username: String): Boolean = {
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" + username withMethod PUT send()
    if (response.statusCode == OK) true
    else false
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def listUsers: List[String] = {
    var users = List.empty[String]
    val response = requestBuilder withCredentials(connection.username, connection.password) withUrl uri + "/users/" withMethod GET send()
    if(response.statusCode == OK) {
      response.body map { r =>
        JSON.parseFull(r) map { p =>
          val mapObject = p.asInstanceOf[Map[String, List[String]]]
          mapObject get "list" map (users :::= _)
        }
      }
      users
    }
    else List()
  }

  /**
    * Shutdown the connection with the server
    */
  def logout() : Unit = println("logout")

}
