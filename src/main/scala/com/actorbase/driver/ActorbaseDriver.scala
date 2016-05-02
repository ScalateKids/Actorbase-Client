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
import com.actorbase.driver.data.{ActorbaseCollection, ActorbaseObject, Serializer}

import scala.util.parsing.json._
import scala.collection.mutable.ListBuffer

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseDriver(address: String, port: Int = 9999) extends Serializer with Connector {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def listCollections : Response = client.send(
    requestBuilder withUrl "https://" + address + ":" + port + "/collectionlist" withMethod GET)

  /** TEST METHODS */

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def find : Response = {
    client.send(
      requestBuilder
        .withUrl("https://" + address + ":" + port + "/collections")
        .withMethod(GET))
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def find(key: String, collection: String = "") : Response = {
    val path =
      if(!collection.isEmpty) "/" + collection + "/" + key
      else "/" + key
    client.send(
      requestBuilder
        .withUrl("https://" + address + ":" + port + "/collections/dummy" + path)
        .withMethod(GET))
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  // def insert(key: String, collection: String = "", json: String = "") : Response = {
  //   val path =
  //     if(!collection.isEmpty) "/" + collection + "/" + key
  //     else "/" + key
  //   client.send(
  //     requestBuilder
  //       .withUrl("https://" + address + ":" + port + "/collections" + path)
  //       .withBody(json)
  //       .withMethod(POST)
  //   )
  // }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def delete(key: String, collection: String = ""): Response = {
    val path =
      if(!collection.isEmpty) "/" + collection + "/" + key
      else "/" + key
    client.send(
      requestBuilder
        .withUrl("https://" + address + ":" + port + "/collections" + path)
        .withMethod(DELETE))
  }

  /** ALTERNATIVE */

  /**
    * Return a list of collections, consider an object
    * ActorbaseList[ActorbaseCollection]
    *
    * @param
    * @return
    * @throws
    */
  def getCollections: List[ActorbaseCollection] = ???

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
    var buffer: ListBuffer[ActorbaseObject] = new ListBuffer[ActorbaseObject]()
    val response = client.send(requestBuilder withUrl "https://" + address + ":" + port + "/collections/" + collectionName + "/" withMethod GET)
    if(response.statusCode == OK) {
      val mapObject = JSON.parseFull(response.body.get).get.asInstanceOf[Map[String, Any]]
      val collectionName = mapObject.get("collection").getOrElse("NoName")
      for((k, v) <- mapObject.get("map").get.asInstanceOf[Map[String, List[Double]]]) {
        val byteArray = v.map(_.toByte).toArray
        buffer += ActorbaseObject(k -> deserializeFromByteArray(byteArray))
      }
    }
    ActorbaseCollection("owner", collectionName, buffer)
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def addCollection(collectionName: String): ActorbaseCollection = {
    val collection = ActorbaseCollection("owner", collectionName)
    collection.insert(ActorbaseObject("ciao" -> ActorbaseObject("ciao" -> "sono interno")))
    collection
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def addCollection(collection: ActorbaseCollection): ActorbaseCollection = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def dropCollections: Boolean = {
    val response = client.send(
      requestBuilder withUrl "https://" + address + ":" + port + "/collections" withMethod DELETE)
    if(response != OK)
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
    val response = client.send(
      requestBuilder withUrl "https://" + address + ":" + port + "/collections/" + collectionName withMethod DELETE)
    if(response.statusCode != OK)
      false
    else true
  }

  /**
    * Shutdown the connection with the server
    */
  def logout() : Unit = println("logout")

}
