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

package com.actorbase.driver.client

import org.scalatest._

//import com.actorbase.driver.DriverSpecs.DriverUnitSpec
import com.actorbase.driver.ActorbaseDriver
import com.actorbase.driver.client.api.RequestBuilder
import com.actorbase.driver.client.api.RestMethods._
import com.actorbase.driver.exceptions.{ MalformedFileExc, WrongCredentialsExc }

import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec

import akka.actor.ActorSystem
import org.scalatest.FunSuiteLike
import spray.http.{ ContentType,  HttpEntity }


/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseDriverSpec extends WordSpec with Matchers{

  implicit val system = ActorSystem()
  //val driver = ActorbaseDriver("127.0.0.1", 8080, false)
  //val driver = ActorbaseDriver("http://admin:Actorb4se@127.0.0.1:8080")

  //  addServlet(classOf[ActorbaseServerMock], "/*")

  import com.netaporter.precanned.dsl.fancy._

  val actorbaseMockServices = httpServerMock(system).bind(8766).block


  actorbaseMockServices expect get and path("/testscalaj") and respond using status(300) end()

  actorbaseMockServices expect post and path("/auth/admin") and respond using entity ( HttpEntity (
    // contentType = ContentType(`text/plain`, `UTF-8`),
    string = "Admin"
  )) end()

  actorbaseMockServices expect post and path("/auth/noexists") and respond using entity ( HttpEntity (
    // contentType = ContentType(`text/plain`, `UTF-8`),
    string = "None"
  )) end()

  actorbaseMockServices expect get and path("/listcollection") and respond using status(200) end()

  actorbaseMockServices expect get and path("/collections/testCollection") and respond using entity ( HttpEntity (
    string = """{ "collection" : "testCollection", "map" : { }, "owner" : "" }"""
  )) and status(200) end()

  actorbaseMockServices expect post and path("/collections/testCollection") and respond using status(200) end()
  actorbaseMockServices expect delete and path("/collections/testCollection") and respond using status(200) end()

  actorbaseMockServices expect get and path("/collections/testCollection/testItem") and respond using status(200) end()
  actorbaseMockServices expect post and path("/collections/testCollection/testItem") and respond using status(200) end()
  actorbaseMockServices expect put and path("/collections/testCollection/testItem") and respond using status(200) end()
  actorbaseMockServices expect delete and path("/collections/testCollection/testItem") and respond using status(200) end()

  "ActorbaseDriver instance" should {
    "throw WrongCredentialsExc while trying to authenticate a non-existant user" in {
      an [WrongCredentialsExc] should be thrownBy ActorbaseDriver("http://noexists:Actorb4se@127.0.0.1:8766")
    }

    val driver = ActorbaseDriver("http://admin:Actorb4se@127.0.0.1:8766")


    "make http request test" in {
      import scalaj.http._

      val res: HttpResponse[String] = Http("http://127.0.0.1:8766/testscalaj").asString
      //println(res.code + " " + res.body)
      res.code should be(300)
    }
    // }

    // to be *scommented* when the feature will return something
    /*it should {
     "ask for insert item" in {
     val response = driver.insertTo("testCollection", false, ("testItem" -> "testPayload"))
     println(response)
     response.code should be(200)
     }
     }*/

    // it should {
    "ask for list collections" in {
      val response = driver.listCollections
      //println(response)
      assert(response.size == 0)
    }
    // }


    // it should {
    "ask for creating a collection" in {
      val response = driver.addCollection("testCollection")
      assert(response.collectionName == "testCollection")
    }
    // }

    // it should {
    "ask for a collection and for an export to file" in {
      val response = driver.getCollection("testCollection")
      assert(response.collectionName == "testCollection")
    }
    // }

    // it should {
    "ask for deleting a collection" in {
      val response = driver.dropCollections("testCollection")
      println(response)
    }
    // }



    // it should {
    "ask for a single item" in {
      val response = driver.find("testItem", "testCollection")
      //assert(response.getClass() == ActorbaseObject)
    }
    // }

    // it should {
    "ask for inserting one item without overwriting" in {
      val response = driver.insertTo("testCollection", false, ("testItem" ->"testPayload"))
      //assert(response.getClass() == ActorbaseObject)
    }
    // }

    // it should {
    "ask for inserting one item allowing overwriting" in {
      val response = driver.insertTo("testCollection", true, ("testItem" ->"testPayload"))
      //assert(response.getClass() == ActorbaseObject)
    }
    // }

    // it should {
    "ask for deleting one item" in {
      val response = driver.removeFrom("testCollection", "testItem")
      //assert(response.getClass() == ActorbaseObject)
    }
    // }


    /*
     it should {
     "authenticate to the actorsystem" in {

     }
     }*/

    /*
     it should {
     "ask for a password change" in {

     }
     }*/


    // it should {
    "import items from file" in {
      val response = driver.importFromFile("src/test/resources/importTest.json")
      assert( response == true )
    }
  }
}
