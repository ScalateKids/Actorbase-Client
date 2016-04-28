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

import play.api.libs.ws._
import play.api.libs.ws.ning._

import play.api.libs.ws.ning.NingWSClient
import play.api.libs.ws.{WSResponse, WSRequest}

import com.ning.http.client.AsyncHttpClientConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.actorbase.driver.client.RestMethods._

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
object ActorbaseClient extends ActorbaseClient {

  lazy val client = initClient()

}

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseClient extends Client {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  override def initClient() : NingWSClient  = {
    val builder = new AsyncHttpClientConfig.Builder()
    val client = new NingWSClient(builder.build)
    client
  }

  /**
    * Send method, send a Request object to the Actorbase server listening
    * and return a Future[Response] containing the Response object
    *
    * @param request a Request reference, contains all HTTP request details
    * @return a Future of type Response, containing the status of the response
    * and the body as Option[String]
    * @throws
    */
  override def send(request: Request) : Future[Response] = {
    getHttpResponse(request).map {
      response => Response(response.status, Some(response.body.asInstanceOf[Array[Byte]]))
    }
  }

  /**
    * Send a Request object to the Actorbase server listening and return a
    * Future[WSResponse] (an object of the library PlayWS!)
    *
    * @param request a Request reference, contains all HTTP request details
    * @return a Future of type WSResponse, containing all headers, body and
    * status of the response from the server
    * @throws
    */
  def getHttpResponse(request: Request): Future[WSResponse] = {
    val wsRequest: WSRequest = ActorbaseClient.client.url(request.uri).withHeaders("Cache-Control" -> "no-cache")
    request.method match {
      case GET    => wsRequest.get
      case POST   => wsRequest.post(request.body.get)
      case PUT    => wsRequest.put(request.body.get)
      case DELETE => wsRequest.delete
    }
  }

  /**
    * Shutdown the connection with the server closing the client
    *
    * @param
    * @return
    * @throws
    */
  override def shutdown(): Unit = ActorbaseClient.client.close
}
