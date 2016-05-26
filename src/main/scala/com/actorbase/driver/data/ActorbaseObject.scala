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

import com.actorbase.driver.client.Serializer

case object ActorbaseObject {
  def apply[B >: Any](kv: (String, B)*): ActorbaseObject[B] = ActorbaseObject(kv.toMap)
}

/**
  * Map Scala class for ActorbaseObject - proxies an existing ActorbaseObject on
  * the server. Allow to navigate and modify contents to be added to the system.
  *
  */
case class ActorbaseObject[B >: Any](elems: Map[String, B]) extends Map[String, B] with Serializer {

  /**
    * Get an Option[A], used to retrieve a value inside the object by specifying
    * its type
    *
    * @param key a String representing the key associated to the value to be retrieved
    * @return Option[A] an Option containing the value of type A
    * @throws
    */
  def as[A](key: String): Option[A] = elems.get(key) map (x => Some(x.asInstanceOf[A])) getOrElse None

  /**
    * Override of the method + of the Map trait for Scala, add a key-value to
    * the ActorbaseObject
    *
    * @param kv a key-value pair of type String - Any
    * @return an object of type ActorbaseObject, representing a proxy object of the system
    * @throws
    */
  override def +[B1 >: B](kv: (String, B1)): ActorbaseObject[B1] = ActorbaseObject[B1](elems + (kv._1 -> kv._2))

  /**
    * Override of the method + of the Map trait for Scala, add key-values from
    * the ActorbaseObject
    *
    * @param elem1 a String-Any pair
    * @param elem2 a String-Any pair
    * @param elemss multiple String-Any pairs
    * @return an object of type ActorbaseObject representing a proxy of an object to be added to the system
    * @throws
    */
  override def +[B1 >: B](elem1: (String, B1), elem2: (String, B1), elemss: (String, B1)*): ActorbaseObject[B1] =
    ActorbaseObject[B1](elems + (elem1._1 -> elem1._2) + (elem2._1 -> elem2._2) ++ elemss.toMap)

  /**
    * Override of the method - of the Map trait for Scala, remove a key-value to
    * the ActorbaseObject
    *
    * @param key a String representing a key associated to the value of the item designed for removal
    * @return an object of type ActobaseObject representing a proxy of an existing object desgned for removal from the system
    * @throws
    */
  override def -(key: String): ActorbaseObject[Any] = ActorbaseObject(elems - key)

  /**
    * Override of the method - of the Map trait for Scala, remove multiple key-values from
    * the ActorbaseObject
    *
    * @param key1 a String representing a key associated to the value of the item designed for removal
    * @param key2 a String representing a key associated to the value of the item designed for removal
    * @param keys a sequence of String representing multiple keys associated to the values of the items designed for removal
    * @return an object of type ActobaseObject representing a proxy of an existing object desgned for removal from the system
    * @throws
    */
  override def -(key1: String, key2: String, keys: String*): ActorbaseObject[Any] = ActorbaseObject(elems - key1 - key2 -- keys.toSeq)

  /**
    * Override of the method get of the trait Map for Scala, retrieve an
    * Option[Any] containing the value associated to the given key
    *
    * @param key a String representing a key associated to a value inside the system
    * @return an Option[Any] containing the value associated to the given key
    * @throws
    */
  override def get(key: String): Option[Any] = elems.get(key)

  /**
    * Override of the method iterator of the trait Map for Scala, get an
    * iterator instance
    *
    * @return an instance of Iterator[String, Any], allow to iterate thorugh the elements of the object
    * @throws
    */
  override def iterator: Iterator[(String, Any)] = elems.iterator

  /**
    * Override of the method toString, give a JSON representation of the ActorbaseObject
    *
    * @return a String representing the ActorbaseObject JSON formatted
    * @throws
    */
  override def toString: String = serialize2JSON4s(this)

}
