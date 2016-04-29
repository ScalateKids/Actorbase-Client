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

import scalaj.http._

import com.actorbase.driver.client.RestMethods._

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseClient extends Client {

  /**
    * Send method, send a Request object to the Actorbase server listening
    * and return a Response object
    *
    * @param request a Request reference, contains all HTTP request details
    * @return an object of type Response, containing the status of the response
    * and the body as Option[String]
    * @throws
    */
  override def send(request: Request): Response = {
    val response = request.method match {
      case GET    => Http(request.uri).option(HttpOptions.readTimeout(5000)).asString
      case POST   => Http(request.uri).postData(request.body.get).option(HttpOptions.readTimeout(5000)).asString
      case PUT    => Http(request.uri).postData(request.body.get).method("PUT").option(HttpOptions.readTimeout(5000)).asString
      case DELETE => Http(request.uri).method("DELETE").option(HttpOptions.readTimeout(5000)).asString
    }
    Response(response.code, Some(response.body.asInstanceOf[String]))
  }

  /**
    * Shutdown the connection with the server closing the client
    *
    * @param
    * @return
    * @throws
    */
  override def shutdown(): Unit = println("Shutdown")
}
