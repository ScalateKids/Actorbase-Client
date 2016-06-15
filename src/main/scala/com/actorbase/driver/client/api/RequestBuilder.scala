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

package com.actorbase.driver.client.api

import scala.language.implicitConversions

import com.actorbase.driver.client.api.RestMethods._
import com.actorbase.driver.client.ActorbaseClient

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
case class RequestBuilder(
  method: Option[Method],
  url: Option[String],
  user: Option[String],
  password: Option[String],
  headers: (String, String),
  body: Option[Array[Byte]]) {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def withMethod(method: Method): RequestBuilder = copy(method = Some(method))

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def withUrl(url: String): RequestBuilder = copy(url = Some(url))

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def withCredentials(uname: String, pass: String): RequestBuilder = copy(user = Some(uname), password = Some(pass))

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def withBody(body: Array[Byte]): RequestBuilder = copy(body = Some(body))

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def addPath(path: String): RequestBuilder = {
    val s = url.get.toString
    val slash = if (s.endsWith("/")) "" else "/"
    copy(url = Some(s + slash + path))
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def addHeaders(hs: (String, String)) = copy(headers = hs)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def toRequest: Request = {
    Request(method.get, url.get, user.get, password.get, headers, body)
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def toHeaders(hs: (String, String)*): Map[String, List[String]] = {
    hs.foldRight(Map[String, List[String]]()) {
      case ((name, value), hm) =>
        val listValue = if (value == "") List() else List(value)
        val list = hm.get(name).map(listValue ++ _) getOrElse (listValue)
        hm + (name -> list)
    }
  }
}

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
object RequestBuilder {

  val emptyBuilder = RequestBuilder(None, None, None, None, ("", ""), None)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def apply(): RequestBuilder = emptyBuilder

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  implicit def toRequest(builder: RequestBuilder): Request = builder.toRequest

  /**
    * Implicit class conversion, permit the use of method send with an implicit
    * parameter in scope
    */
  implicit class AutoRequestBuilder(builder: RequestBuilder) {
    def send()(implicit client: ActorbaseClient): Response = {
      client.send(builder)
    }
  }

}
