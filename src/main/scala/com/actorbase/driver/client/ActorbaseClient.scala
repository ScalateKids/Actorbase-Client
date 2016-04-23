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
import play.api.libs.ws.WSResponse
import com.ning.http.client.AsyncHttpClientConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseClient {

  val client = new NingWSClient(new AsyncHttpClientConfig.Builder().build)

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def send(request: Request) : Response = {
    Response(1, Some("ciao"))
  //   // val futureResponse = createResponse(request)
  //   // futureResponse onSuccess { case response => Response(response.status, Some(response.body)) }
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def createResponse(request: Request): Future[WSResponse] = {
    request.method match {
      case GET =>
        client
          .url(request.uri)
          .withHeaders("Cache-Control" -> "no-cache")
          .get

      // case POST =>
        // client
        //   .url(request.uri)
        //   .withHeaders("Cache-Control" -> "no-cache")
        //   .post _

      // case PUT =>
        // client
        //   .url(request.uri)
        //   .withHeaders("Cache-Control" -> "no-cache")
        //   .put _

      // case DELETE =>
        // client
        //   .url(request.uri)
        //   .withHeaders("Cache-Control" -> "no-cache")
        //   .delete
    }
  }
}
