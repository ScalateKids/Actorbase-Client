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
    * Find keys inside the collection map
    *
    * @param keys a vararg of String
    * @return ActorbaseCollection containing results of the query
    */
  def find(keys: String*): ActorbaseCollection = {
    var (coll, contr) = (new TreeMap[String, Any](), Map.empty[String, Boolean])
    data map { collection =>
      collection._2.find(keys:_*).foreach(kv => coll += (kv._1 -> kv._2))
      contr ++= collection._2.contributors
    }
    ActorbaseCollection(conn.username, "findResults", contr, coll)(conn, scheme)
  }

  /**
    * Drop collections from the collection map
    *
    * @param collections a vararg of String representing a sequence of collections
    * @return no return value
    */
  def drop(collections: String*): Unit = {
    collections.foreach { collection =>
      data get collection map (x => x.drop)
      data -= collection
    }
  }

  /**
    * Count the number of collections
    */
  def count: Int = data.size

  /**
    * Foreach method, applies a function f to all elements of this map.
    *
    * @param f the function that is applied for its side-effect to every element.
    * The result of function f is discarded.
    * @return no return value
    */
  def foreach(f: ((String, ActorbaseCollection)) => Unit): Unit = data.foreach(f)

  /**
    * Creates a non-strict filter of this traversable collection.
    *
    * @param p the predicate used to test elements.
    * @return an object of class WithFilter, which supports map, flatMap, foreach,
    * and withFilter operations. All these operations apply to those elements of
    * this traversable collection which satisfy the predicate p.
    */
  def withFilter(f: ((String, ActorbaseCollection)) => Boolean): FilterMonadic[(String, ActorbaseCollection), TreeMap[String, ActorbaseCollection]] = data.withFilter(f)

  /**
    * Converts this collection to a string.
    *
    * @param
    * @return a string representation of this collection. By default this string
    * consists of a JSON containing the colleciton name, the owner and items
    */
  override def toString: String = {
    var ret = ""
    data.foreach {
      case (k, v) => ret += "\n" + v.toString + "\n"
    }
    ret
  }

}
