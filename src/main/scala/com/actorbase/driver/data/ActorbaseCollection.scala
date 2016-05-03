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

import com.actorbase.driver.client.Connector
import com.actorbase.driver.client.api.RestMethods._

import scala.collection.immutable.TreeMap
import scala.collection.JavaConversions

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
case class ActorbaseCollection private (val owner: String,
  var collectionName: String,
  var data: TreeMap[String, Any] = new TreeMap[String, Any]()) extends Serializer with Connector {

  /**
    * Insert an arbitrary variable number of key-value tuple to the collection
    * reflecting local changes to remote collection on server-side
    *
    * @param kv a vararg Tuple2 of type (String, Any)
    * @return
    * @throws
    */
  def insert(kv: Tuple2[String, Any]*): Unit = {
    for((k, v) <- kv) {
      if(!data.contains(k)) {
        data += (k -> v)
        client.send(requestBuilder withUrl "https://127.0.0.1:9999/collections/" + collectionName + "/" + k withBody serialize2byteArray(v) withMethod POST)
      }
    }
  }

  /**
    * Insert a new key-value tuple, representing an ActorbaseObject to the
    * collection reflecting local changes to remote collection on server-side
    *
    * @param kv an ActorbaseObject parameter representing a key/value pair
    * @return
    * @throws
    */
  def insert(kv: ActorbaseObject): Unit = this.insert((kv.getKey -> kv.getValue))

  /**
    * Remove an arbitrary variable number of key-value tuple from the collection
    * reflecting local changes to remote collection on server-side
    *
    * @param keys a vararg String representing a sequence of keys to be removed
    * from the collection
    * @return
    * @throws
    */
  def remove(keys: String*): Unit = {
    for(key <- keys) {
      if(data.contains(key)) {
        data -= key
        client.send(requestBuilder withUrl "https://127.0.0.1:9999/collections/" + collectionName + "/" + key withMethod DELETE)
      }
    }
  }

  /**
    * Remove a key-value tuple, representing an ActorbaseObject from the
    * collection reflecting local changes to remote collection on server-side
    *
    * @param kv an ActorbaseObject parameter representing a key/value pair
    * @return
    * @throws
    */
  def remove(kv: ActorbaseObject): Unit = this.remove(kv.getKey)

  /**
    * Find an arbitrary number of elements inside the collection, returning a
    * new ActorbaseCollection
    *
    * @param keys a vararg String representing a sequence of keys to be retrieved
    * @return an object of type ActorbaseCollection
    * @throws
    */
  def find(keys: String*): ActorbaseCollection = {
    val collection: TreeMap[String, Any] = data filter keys.contains
    ActorbaseCollection(owner, collectionName, collection)
  }

  /**
    * Find an element inside the collection, returning an ActorbaseObject
    * representing the key/value pair
    *
    * @param keys a String representing the key associated to the value to be retrieved
    * @return an object of type ActorbaseObject
    * @throws
    */
  def findOne(key: String): ActorbaseObject = {
    val actorbaseObject =
      if (data.contains(key))
        ActorbaseObject(key -> data.get(key).get)
      else ActorbaseObject(None)
    actorbaseObject
  }

  /**
    * Drop the entire collection, reflecting the local change to remote on
    * server-side
    *
    * @param
    * @return
    * @throws
    */
  def drop = {
    data.empty
    client.send(requestBuilder withUrl "https://127.0.0.1:9999/collections/" + collectionName withMethod DELETE)
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
  def foreach(f: ((String, Any)) => Unit): Unit = data.foreach(f)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  override def toString: String = serialize2JSON4s(this)

}
