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
object ActorbaseClient {
  lazy val client = new NingWSClient(new AsyncHttpClientConfig.Builder().build)
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
  override def send(request: Request) : Future[Response] = {
    createResponse(request).map {
      response => Response(response.status, Some(response.body))
    }
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def createResponse(request: Request): Future[WSResponse] = {
    val wsRequest: WSRequest = ActorbaseClient.client.url(request.uri).withHeaders("Cache-Control" -> "no-cache")
    request.method match {
      case GET    => wsRequest.get
      case POST   => wsRequest.post(request.body.get)
      case PUT    => wsRequest.put(request.body.get)
      case DELETE => wsRequest.delete
    }
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  override def shutdown(): Unit = ActorbaseClient.client.close
}
