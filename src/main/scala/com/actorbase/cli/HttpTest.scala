package com.actorbase.cli

import play.api.libs.ws.ning.NingWSClient
import com.ning.http.client.AsyncHttpClientConfig

import scala.concurrent.ExecutionContext.Implicits.global

object HttpTest extends App {

  val client = new NingWSClient(new AsyncHttpClientConfig.Builder().build)

  var input: String = ""

  while( input != "quit" ){
    input = readLine("> ")
    client
      .url("http://" + input)
      .withHeaders("Cache-Control" -> "no-cache")
      .get
      .map { response =>
      if(!(200 to 299).contains(response.status)) {
        sys.error(s"Error ${response.status} : ${response.body}")
      }
      println(s"OK:\n${response.body}")
      println(s"Content-Length: ${response.header("Content-Length").get}")
    }
  }
  client.close()
}
