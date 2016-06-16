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

import scala.util.parsing.json.JSON
import scala.language.implicitConversions

import scalaj.http.HttpResponse

/**
  * This module contains all object and class useful to build
  * HTTP request
  *
  */

object RestMethods {

  /**
    * Class representing a generic HTTP request
    *
    */
  sealed abstract class Method(name: String)

  /**
    * Object representing a GET request
    *
    */
  case object GET extends Method("GET")

  /**
    * Object representing a POST method
    *
    */
  case object POST extends Method("POST")

  /**
    * Object representing a PUT request
    *
    */
  case object PUT extends Method("PUT")

  /**
    * Object representing a DELETE request
    *
    */
  case object DELETE extends Method("DELETE")

  /**
    * Simple class representing an HTTP request, formed by all common fields of
    * a request
    *
    */
  case class Request(method: Method, uri: String, user: String, password: String, headers: (String, String) = ("",""), body: Option[Array[Byte]] = None)

  /**
    * Simple class representing an HTTP response
    *
    */
  case class Response(statusCode: Int, body: Option[String])

  /**
    * Companion object of case class Response
    *
    */
  case object Response {

    /**
      * Implicit conversion method, return the body of a Response as a Map
      *
      * TODO:
      * Basic and meaningless implementation, still need a lot of improvements
      *
      * @param response The Response object to convert
      * @return a Map[String, List[String]] representing a JSON object
      */
    implicit def toMap(response: Response) : Map[String, List[String]] = {
      JSON.parseFull(response.body.getOrElse("None")).get.asInstanceOf[Map[String, List[String]]]
    }

    /**
      * Implicit conversion method, return a Response from a WSResponse (playWS! response type)
      *
      * @param HttpResponse The HttpResponse[T] object to convert
      * @return a Response object containing statusCode and body of the HttpResponse
      */
    implicit def HttpResponseToResponse(HttpResponse: HttpResponse[String]) : Response = Response(HttpResponse.statusCode, Some(HttpResponse.body.asInstanceOf[String]))
  }

  /**
    * Status object, contains the most common return codes of HTTP request
    *
    */
  object Status {
    val OK = 200
    val BadRequest = 400
    val Unauthorized = 401
    val Forbidden = 403
    val NotFound = 404
    val Error = 500
  }
}
