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

package com.actorbase.cli

import org.scalatest._
import akka.actor.ActorSystem
import spray.http.{ ContentType,  HttpEntity }

/**
  * Module containing all specifications for testing CLI components.
  * All types of specifications and tests related to the CLI component
  * should be added here.
  *
  * @param
  * @return
  * @throws
  */
object CLISpecs {

  /**
    * Basic unit-testing class
    *
    * @param
    * @return
    * @throws
    */
  abstract class CLIUnitSpec extends FlatSpec with Matchers{

    implicit val system = ActorSystem()

    import com.netaporter.precanned.dsl.fancy._

    val actorbaseMockServices = httpServerMock(system).bind(9994).block

    actorbaseMockServices expect post and path("/auth/admin") and respond using entity ( HttpEntity (
      string = "Admin"
    )) end()

    actorbaseMockServices expect post and path("/auth/wrongUsername") and respond using entity ( HttpEntity (
      string = "None"
    )) end()

    actorbaseMockServices expect get and path("/listcollection") and respond using status(200) end()

    /**
      * Collections routes
      */
    //actorbaseMockServices expect get and path("/collections/testCollection") and respond using status(200) end()
    actorbaseMockServices expect get and path("/collections/testCollection") and respond using entity ( HttpEntity (
      string = """{ "collectionName" : "testCollection", "data" : { }, "owner" : "" }"""
    )) and status(200) end()
    actorbaseMockServices expect post and path("/collections/testCollection") and respond using status(200) end()
    actorbaseMockServices expect delete and path("/collections/testCollection") and respond using status(200) end()

    /**
      * collaborator routes
      */
    actorbaseMockServices expect get and path("contributors/testCollection") and respond using status(200) end()
    actorbaseMockServices expect get and path("contributors/notExistingCollection") and respond using status(500) end()
    actorbaseMockServices expect post and path("contributors/testCollection/read") and respond using status(200) end()


    actorbaseMockServices expect get and path("/collections/testCollection/testItem") and respond using status(200) end()
    actorbaseMockServices expect post and path("/collections/testCollection/testItem") and respond using status(200) end()

    actorbaseMockServices expect delete and path("/collections/NotExistingCollection/testItem") and respond using status(500) end()

    actorbaseMockServices expect put and path("/collections/testCollection/testItem") and respond using status(200) end()
    actorbaseMockServices expect delete and path("/collections/testCollection/testItem") and respond using status(200) end()

  }


}
