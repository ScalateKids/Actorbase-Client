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

package com.actorbase.driver.client.api

import scala.language.implicitConversions

import com.actorbase.driver.client.api.RestMethods._
import com.actorbase.driver.client.ActorbaseClient

/**
  * Builder class, used to create HTTP request to communicate with
  * the server side of Actorbase
  *
  */
case class RequestBuilder(
  method: Option[Method],
  url: Option[String],
  user: Option[String],
  password: Option[String],
  headers: (String, String),
  body: Option[Array[Byte]]) {

  /**
    * Add a method of connection, it can be either GET, POST, PUT or DELETE, it
    * defines the verb of a RESTful communication
    *
    * @param method a Method type that define an HTTP call, behavioring like a
    * verb in a RESTful communication session
    * @return an Instance of the class RequestBuilder
    */
  def withMethod(method: Method): RequestBuilder = copy(method = Some(method))

  /**
    * Add the url of the resource to be reached by the connection, generally it
    * represents the location of the server
    *
    * @param url a String representing the URL of the server on the domain
    * @return an Instance of the class RequestBuilder
    */
  def withUrl(url: String): RequestBuilder = copy(url = Some(url))

  /**
    * Add credentials to the request, formed by an username and a password
    *
    * @param uname a String representing the username of the user
    * @param pass a String representing the password associated to the
    * username of the user
    * @return an Instance of the class RequestBuilder
    */
  def withCredentials(uname: String, pass: String): RequestBuilder = copy(user = Some(uname), password = Some(pass))

  /**
    * Add the body of the request, containing an array of bytes. Can be
    * anything.
    *
    * @param body an Array[Byte] representing the payload to be added
    * to the request
    * @return an Instance of the class RequestBuilder
    */
  def withBody(body: Array[Byte]): RequestBuilder = copy(body = Some(body))

  /**
    * Aappend an additional path to the request
    *
    * @param path a String representing the new path to be appended
    * @return an Instance of the class RequestBuilder
    */
  def addPath(path: String): RequestBuilder = {
    val s = url.get.toString
    val slash = if (s.endsWith("/")) "" else "/"
    copy(url = Some(s + slash + path))
  }

  /**
    * Add headers to the request, represented by a String-String pair
    *
    * @param hs a tuple2[String, String] representing an header in form of key-value pair
    * @return an Instance of the class RequestBuilder
    */
  def addHeaders(hs: (String, String)) = copy(headers = hs)

  /**
    * Convert the current object of type RequestBuilder to an object of type
    * Request
    *
    * @return an instance of the class Request with all parts added
    */
  def toRequest: Request = {
    Request(method.get, url.get, user.get, password.get, headers, body)
  }

  /**
    * Converts a vararg of headers to a Map of string as key and list of strings
    * as value
    *
    * @param hs a vararg of string-string pairs
    * @return a Map[String, List[String]] of headers
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
  * Companion object of the class RequestBuilder, contains utility methods and
  * implicit converters
  */
object RequestBuilder {

  val emptyBuilder = RequestBuilder(None, None, None, None, ("", ""), None)

  /**
    * Apply method
    *
    * @return an instance of the class RequestBuilder
    */
  def apply(): RequestBuilder = emptyBuilder

  /**
    * Implicit converter, converts the RequestBuilder object into a Request
    *
    * @param builder an instance of a RequestBuilder class
    * @return an Instance of the class Request with all parts added
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
