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

import org.scalatest._
import org.scalatest.concurrent._

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseDriverSpec extends FlatSpec with ScalaFutures with Matchers {

  /**
    * Basic test for http request, should be launched only with
    * an Actorbase-Server instance listening
    *
    * @param
    * @return
    * @throws
    */
  "find HTTP request for a key" should "response with a future containing the requested key" in {
    val driver = new ActorbaseDriver("127.0.0.1")

    val findResponse = driver.find("ciao")

    whenReady(findResponse) { response =>
      response.body.get should be ("""{
  "response": "ciao"
}""")
    }
  }

  /**
    * Basic test for https request, should be launched only with
    * an Actorbase-Server instance listening
    *
    * @param
    * @return
    * @throws
    */
//   "find HTTPS request for a key" should "response with a future containing the requested key" in {
//     val driver = new ActorbaseDriver("127.0.0.1") with SSLClient

//     val findResponse = driver.find("ciao")

//     whenReady(findResponse) { response =>
//       response.body.get should be ("""{
//   "response": "ciao"
// }""")
//     }
//   }
}
