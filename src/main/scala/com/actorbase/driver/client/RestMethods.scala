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

package com.actorbase.driver.client

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */

object RestMethods {
  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  sealed abstract class Method(name: String)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  case object GET extends Method("GET")

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  case object POST extends Method("POST")

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  case object PUT extends Method("PUT")

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  case object DELETE extends Method("DELETE")

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  case class Request(method: Method, uri: String, headers: Map[String, List[String]] = Map(), body: Option[String] = None)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  case class Response(statusCode: Int, body: Option[String])

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  object Status {
    val OK = 200
    val Created = 201
    val Accepted = 202
    val BadRequest = 400
    val Unauthorized = 401
    val PaymentRequired = 402
    val Forbidden = 403
    val NotFound = 404
  }
}