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
  *
  * @author Scalatekids TODO DA CAMBIARE
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.driver

import com.netaporter.precanned.dsl.fancy._
import spray.http.{ ContentType,  HttpEntity }
import akka.actor.ActorSystem

object ActorbaseServerMock {

  implicit val system = ActorSystem()

  def startMock: Unit = {
    val actorbaseMockServices = httpServerMock(system).bind(8766).block

    actorbaseMockServices expect get and path("/testscalaj") and respond using status(200) end()

    actorbaseMockServices expect post and path("/auth/admin") and respond using entity ( HttpEntity (
      // contentType = ContentType(`text/plain`, `UTF-8`),
      string = "Admin"
    )) end()

    actorbaseMockServices expect post and path("/auth/noexists") and respond using entity ( HttpEntity (
      // contentType = ContentType(`text/plain`, `UTF-8`),
      string = "None"
    )) end()

    // list collection
    actorbaseMockServices expect get and path("/listcollection") and respond using status(200) end()

    // collection routes
    actorbaseMockServices expect get and path("/collections/testCollection/") and respond using entity ( HttpEntity (
      string = """{ "collection" : "testCollection", "map" : { }, "owner" : "" }"""
    )) and status(200) end()
    actorbaseMockServices expect post and path("/collections/testCollection") and respond using status(200) end()
    actorbaseMockServices expect delete and path("/collections/testCollection") and respond using status(200) end()

    // items routes
    actorbaseMockServices expect get and path("/collections/testCollection/testItem") and respond using status(200) end()
    actorbaseMockServices expect post and path("/collections/testCollection/testItem") and respond using status(200) end()
    actorbaseMockServices expect put and path("/collections/testCollection/testItem") and respond using status(200) end()
    actorbaseMockServices expect delete and path("/collections/testCollection/testItem") and respond using status(200) end()

    // collaborator routes
    actorbaseMockServices expect get and path("contributors/testCollection") and respond using status(200) end()
    actorbaseMockServices expect post and path("contributors/testCollection/read") and respond using status(200) end()
    /*  actorbaseMockServices expect put and path("/collections/testCollection/testItem") and respond using status(200) end()
      actorbaseMockServices expect delete and path("/collections/testCollection/testItem") and respond using status(200) end()
    */
    // other routes
    actorbaseMockServices expect get and path("/collections/testNavigableCollection/") and respond using entity ( HttpEntity (
      string = """{ "collection" : "testCollection", "map" : { "key" -> "palyload" }, "owner" : "" }"""
    )) and status(200) end()
  }
}

