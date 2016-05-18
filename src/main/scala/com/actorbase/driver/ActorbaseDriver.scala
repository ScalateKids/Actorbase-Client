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
import com.actorbase.driver.data.{ActorbaseCollection, ActorbaseCollectionMap, Serializer}

import scala.util.parsing.json._
import scala.collection.immutable.TreeMap

object ActorbaseDriver {

  def apply(): ActorbaseDriver = new ActorbaseDriver("127.0.0.1", 9999)

  def apply(address: String): ActorbaseDriver = new ActorbaseDriver(address, 9999)

  def apply(address: String, port: Int): ActorbaseDriver = new ActorbaseDriver(address, port)

  case class Connection(address: String, port: Int)

}

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseDriver(address: String = "127.0.0.1", port: Int = 9999) extends Serializer with Connector {

  implicit val connection = ActorbaseDriver.Connection(address, port)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def changePassword(newpassword: String): Boolean

  /**
    * Return a list of collection name stored remotely on the server
    *
    * @param
    * @return a List[String] contained the collection names
    * @throws
    */
  def listCollections : List[String] = {
    val response =
      requestBuilder withUrl "https://" + address + ":" + port + "/collectionlist" withMethod GET send()
    if(response.statusCode == OK)
      JSON.parseFull(response.body.get).get.asInstanceOf[List[String]]
    else List()
  }

  /**
    * Return a list of collections, consider an object ActorbaseCollectionMap
    *
    * @param
    * @return an ActorbaseCollectionMap containing a map of collections
    * @throws
    */
  def getCollections: ActorbaseCollectionMap = ???

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
    var buffer: TreeMap[String, Any] = new TreeMap[String, Any]()
    val response = requestBuilder withUrl "https://" + address + ":" + port + "/collections/" + collectionName + "/" withMethod GET send()
    if (response.statusCode == OK) {
      val mapObject = JSON.parseFull(response.body.get).get.asInstanceOf[Map[String, Any]]
      val collectionName = mapObject.get("collection").getOrElse("NoName")
      for ((k, v) <- mapObject.get("map").get.asInstanceOf[Map[String, List[Double]]]) {
        val byteArray = v.map(_.toByte).toArray
        buffer += (k -> deserializeFromByteArray(byteArray))
      }
    }
    ActorbaseCollection("user", collectionName, buffer)
  }

  /**
    * Add a collection on server side of Actorbase
    *
    * @param collectionName a String representing the collection name to be retrieved from the server
    * @return an ActorbaseCollection representing a collection stored on Actorbase
    * @throws
    */
  def addCollection(collectionName: String): ActorbaseCollection = {
    val response = requestBuilder withUrl "https://" + address + ":" + port + "/collections/" + collectionName withMethod POST send() // control response
    ActorbaseCollection("user", collectionName) // stub owner
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
      requestBuilder withUrl "https://" + address + ":" + port + "/collections/" + collection.collectionName withMethod POST send() // control response and add payload to post
    collection
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def dropCollections: Boolean = {
    val response = requestBuilder withUrl "https://" + address + ":" + port + "/collections" withMethod DELETE send()
    if(response.statusCode != OK)
      false
    else true
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def dropCollection(collectionName: String): Boolean = {
    val response = requestBuilder withUrl "https://" + address + ":" + port + "/collections/" + collectionName withMethod DELETE send()
    if(response.statusCode != OK)
      false
    else true
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def importFromFile(path: String): Boolean = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def exportToFile(path: String): Boolean = ???

  /**
    * Shutdown the connection with the server
    */
  def logout() : Unit = println("logout")

}
