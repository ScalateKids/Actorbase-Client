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

package com.actorbase.driver

import com.actorbase.driver.client.Connector
import com.actorbase.driver.client.api.RestMethods._
import com.actorbase.driver.client.api.RestMethods.Status._

import scala.util.parsing.json._
import java.net.URI

object ActorbaseDriver extends Connector {

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def apply: ActorbaseServices = new ActorbaseServices() with Connector

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def apply(username: String = "anonymous",
    password: String = "Actorb4se",
    address: String = "127.0.0.1",
    port: Int = 9999,
    ssl: Boolean = false): ActorbaseServices = new ActorbaseServices(address, port) with Connector

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def apply(url: String): ActorbaseServices with Connector = {
    val uri = new URI(url)
    implicit val scheme = uri.getScheme
    val credentials = uri.getUserInfo.split(":")
    val request = requestBuilder withCredentials(credentials(0), credentials(1)) withUrl scheme + "://" + uri.getHost + ":" + uri.getPort + "/auth/" + credentials(0) withBody credentials(1).getBytes withMethod POST send()
    // request.statusCode match {

    // }
    var response = ""
    request.body map (x => response = x.asInstanceOf[String]) getOrElse (response = "None1")
    println(response)
    if (response == "Admin")
      new ActorbaseServices(uri.getHost, uri.getPort) with ActorbaseAdminServices
    else new ActorbaseServices(uri.getHost, uri.getPort) with Connector
  }

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def authenticate(username: String, password: String, address: String = "127.0.0.1", port: Int = 9999, ssl: Boolean = false): ActorbaseServices  = ???

  /**
    * Insert description here
    *
    * @param
    * @return
    * @throws
    */
  def authenticate(uri: String): ActorbaseServices = ???

}
