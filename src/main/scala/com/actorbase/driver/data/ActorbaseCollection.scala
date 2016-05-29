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
import java.io.{File, PrintWriter}

import scala.collection.immutable.TreeMap
import scala.collection.generic.FilterMonadic

// import spray.json._
// import DefaultJsonProtocol._

// object MyJsonProtocol extends DefaultJsonProtocol {
//   implicit val connection = ActorbaseDriver.Connection("127.0.0.1", 9999)
//   implicit val actorbaseCollectionFormat = jsonFormat3(ActorbaseCollection.apply)
// }

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
case class ActorbaseCollection
  (val owner: String, var collectionName: String,
    var data: TreeMap[String, Any] = new TreeMap[String, Any]())(implicit val conn: Connection, implicit val scheme: String)
    extends Connector {

  val uri: String = scheme + conn.address + ":" + conn.port

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def rename(newName: String): Boolean = ???

  /**
    * Insert an arbitrary variable number of key-value tuple to the collection
    * reflecting local changes to remote collection on server-side
    *
    * @param kv a vararg Tuple2 of type (String, Any)
    * @return
    * @throws
    */
  def insert(kv: (String, Any)*): ActorbaseCollection = {
    for((k, v) <- kv) {
      if(!data.contains(k)) {
        data += (k -> v)
        requestBuilder withCredentials("admin", "actorbase") withUrl scheme + conn.address  + ":" + conn.port + "/collections/" + collectionName + "/" + k withBody serialize2byteArray(v) withMethod POST send()
      }
    }
    ActorbaseCollection(owner, collectionName, data)
  }

  /**
    * Insert a new key-value tuple, representing an ActorbaseObject to the
    * collection reflecting local changes to remote collection on server-side
    *
    * @param kv an ActorbaseObject parameter representing a key/value pair
    * @return
    * @throws
    */
  def insert[A >: Any](kv: ActorbaseObject[A]): ActorbaseCollection = this.insert(kv.toSeq:_*)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def update(kv: (String, Any)*): ActorbaseCollection = {
    for ((k, v) <- kv) {
      if(!data.contains(k)) {
        data -= k
        data += (k -> v)
        requestBuilder withCredentials("admin", "actorbase") withUrl uri + "/collections/" + collectionName + "/" + k withBody serialize2byteArray(v) withMethod PUT send()
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
    * @return
    * @throws
    */
  def remove(keys: String*): ActorbaseCollection = {
    keys.foreach { key =>
      if(data.contains(key)) {
        data -= key
        requestBuilder withCredentials("admin", "actorbase") withUrl uri + "/collections/" + collectionName + "/" + key withMethod DELETE send()
      }
    }
    ActorbaseCollection(owner, collectionName, data)
  }

  /**
    * Remove a key-value tuple, representing an ActorbaseObject from the
    * collection reflecting local changes to remote collection on server-side
    *
    * @param kv an ActorbaseObject parameter representing a key/value pair
    * @return
    * @throws
    */
  def remove[A >: Any](kv: ActorbaseObject[A]): ActorbaseCollection = this.remove(kv.keys.toSeq:_*)

  /**
    * Return all the contents of the collection in an ActorbaseObject
    *
    * @return an object of type ActorbaseObject
    * @throws
    */
  def find[A >: Any]:ActorbaseObject[A] = ActorbaseObject(data)

  /**
    * Find an arbitrary number of elements inside the collection, returning an
    * ActorbaseObject
    *
    *
    * @param keys a vararg String representing a sequence of keys to be retrieved
    * @return an object of type ActorbaseObject
    * @throws
    */
  def find[A >: Any](keys: String*): ActorbaseObject[A] = {
    var coll = TreeMap[String, Any]().empty
    keys.foreach { key =>
      if (data.contains(key))
        coll += data.filterKeys(_ == key).head
    }
    ActorbaseObject(coll.toMap)
  }

  /**
    * Find an element inside the collection, returning an ActorbaseObject
    * representing the key/value pair
    *
    * @param key a String representing the key associated to the value to be retrieved
    * @return an object of type ActorbaseObject
    * @throws
    */
  def findOne[A >: Any](key: String): Option[ActorbaseObject[A]] = {
    if (data.contains(key))
      Some(ActorbaseObject(key -> data.get(key).get))
    else None
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def addContributor(username: String): Boolean = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def removeContributor(username: String): Boolean = ???

  /**
    * Drop the entire collection, reflecting the local change to remote on
    * server-side
    *
    * @param
    * @return
    * @throws
    */
  def drop: Boolean = {
    data = data.empty
    val response = requestBuilder withCredentials("admin", "actorbase") withUrl uri + "/collections/" + collectionName withMethod DELETE send()
    if (response.statusCode == 200) true
    else false
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
