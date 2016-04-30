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


import play.api.{Configuration, Environment, Mode}
import play.api.libs.ws.WSConfigParser
import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient, NingWSClientConfigParser}
import com.typesafe.config.ConfigFactory
import scala.concurrent.Future

import com.actorbase.driver.client.RestMethods._

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
trait SSLClient extends Client {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  abstract override def initClient : NingWSClient  = {
    val configuration = Configuration(ConfigFactory.load("application.conf"))
    val environment = Environment.simple(new java.io.File("./src/main/resources"), Mode.Dev)
    val parser = new WSConfigParser(configuration, environment)
    val clientConfig = parser.parse()
    val ningParser = new NingWSClientConfigParser(clientConfig, configuration, environment)
    val ningClientConfig = ningParser.parse()
    val builder = new NingAsyncHttpClientConfigBuilder(ningClientConfig)
    val asyncHttpClientConfig = builder.build()
    val client = new NingWSClient(asyncHttpClientConfig)
    client
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  abstract override def send(request: Request): Future[Response] = {
    super.send(request)
  }

}
