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

import scala.language.implicitConversions

/**
  * Base abstract class for ActorbaseParams, extends this to add custom
  * parameter types
  */
sealed abstract class ActorbaseParams

/**
  * Map-like parameter type
  */
case class ActorbaseMap(pair: (String, Any)) extends ActorbaseParams

object ActorbaseObject {

  def apply(params: ActorbaseParams*): ActorbaseObject = {
    var actorbaseMap = Map[String, Any]()
    for (p <- params) {
      p match {
        case ActorbaseMap(t) => actorbaseMap += t
      }
    }
    new ActorbaseObject(actorbaseMap)
  }
  implicit def pair2ActorbaseMap(t: (String, Any)) = ActorbaseMap(t)
}

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseObject(elems: Map[String, Any]) extends Serializer {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def +[B1 >: Any](kv: (String, B1)): Map[String,B1] = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def -(key: String): Map[String,Any] = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def get(key: String): Option[Any] = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def iterator: Iterator[(String, Any)] = ???

}
