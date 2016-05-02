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

import scala.collection.mutable.ListBuffer

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
case class ActorbaseCollection private (val owner: String,
  var collectionName: String,
  var data: ListBuffer[ActorbaseObject] = new ListBuffer[ActorbaseObject]()) extends Serializer with Connector {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def insert(kv: ActorbaseObject): Unit = {
    if(!data.contains(kv)) {
      data += kv
      client.send(requestBuilder withUrl "https://127.0.0.1:9999/collections/" + collectionName + "/" + kv.getKey withBody serialize2byteArray(kv.getValue) withMethod POST)
    }
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def insert(kv: Tuple2[String, Any]): Unit = this.insert(ActorbaseObject(kv))

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def remove(key: String): Unit = {
    for(k <- data)
      if(k.getKey == key) {
        data -= k
        client.send(requestBuilder withUrl "https://127.0.0.1:9999/collections/" + collectionName + "/" + key withMethod DELETE)
      }
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def remove(kv: ActorbaseObject): Unit = this.remove(kv.getKey)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def find = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def find(key: String) = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def drop = ???

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
  def foreach(f: (ActorbaseObject) => Unit): Unit = data.foreach(f)

}
