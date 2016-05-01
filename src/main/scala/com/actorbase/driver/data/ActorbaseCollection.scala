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

import com.actorbase.driver.ActorbaseDriver

import scala.collection.mutable.ListBuffer

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
case class ActorbaseCollection private (val client: ActorbaseDriver,
  val owner: String,
  var collectionName: String,
  var data: ListBuffer[ActorbaseObject]) {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def insert(kv: ActorbaseObject) = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def remove(key: String) = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def remove(o: ActorbaseObject) = ???

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
