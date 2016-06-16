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

import com.actorbase.driver.client.Connector
import com.actorbase.driver.ActorbaseDriver.Connection

import scala.collection.immutable.TreeMap
import scala.collection.generic.FilterMonadic

case class ActorbaseCollectionMap private
  (var data: TreeMap[String, ActorbaseCollection])(implicit val conn: Connection, implicit val scheme: String = "http://") extends Connector {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def find(keys: String*): ActorbaseCollection = {
    var coll = new TreeMap[String, Any]()
    data map {collection => collection._2.find(keys:_*).foreach(kv => coll += (kv._1 -> kv._2))}
    ActorbaseCollection("anonymous", "findResults", coll)(conn, scheme)
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def drop(collections: String*): Unit = {
    // TODO: exceptions check
    collections.foreach { collection =>
      data.get(collection).get.drop
      data -= collection
    }
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
  def foreach(f: ((String, ActorbaseCollection)) => Unit): Unit = data.foreach(f)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def withFilter(f: ((String, ActorbaseCollection)) => Boolean): FilterMonadic[(String, ActorbaseCollection), TreeMap[String, ActorbaseCollection]] = data.withFilter(f)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  override def toString: String = data.mkString

}
