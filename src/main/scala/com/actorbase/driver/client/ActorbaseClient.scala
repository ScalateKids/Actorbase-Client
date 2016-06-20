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

package com.actorbase.driver.client

import scalaj.http.{Http, HttpOptions}

import com.actorbase.driver.client.api.RestMethods._

/**
  * This class have the responsibility of the communication
  * with the HTTP interface of the server side of Actorbase
  *
  */
class ActorbaseClient extends Client {

  private val client = Http
  private val options = createClientOptions

  /**
    * Add connection options to the scalaj-http client Object
    *
    * @return a sequence of HttpOption representing options to be applied to the
    * connection object
    */
  override def createClientOptions: Seq[HttpOptions.HttpOption] = Seq(HttpOptions.readTimeout(60000))

  /**
    * Send method, send a Request object to the Actorbase server listening
    * and return a Response object
    *
    * @param request a Request reference, contains all HTTP request details
    * @return an object of type Response, containing the status of the response
    * and the body as Option[String]
    */
  override def send(request: Request): Response = {
    val response = request.method match {
      case GET    => Http(request.uri).auth(request.user, request.password).header(request.headers._1, request.headers._2).options(options).asString
      case POST   => Http(request.uri).auth(request.user, request.password).header(request.headers._1, request.headers._2).postData(request.body.getOrElse("None")).options(options).asString
      case PUT    => Http(request.uri).auth(request.user, request.password).header(request.headers._1, request.headers._2).postData(request.body.getOrElse("None")).method("PUT").options(options).asString
      case DELETE => Http(request.uri).auth(request.user, request.password).header(request.headers._1, request.headers._2).method("DELETE").options(options).asString
    }
    Response(response.code, Some(response.body.asInstanceOf[String]))
  }
}
