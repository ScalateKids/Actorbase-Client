package com.actorbase.cli

import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object HttpTest extends App {

  val timeout = 60000 * 10

  val builder = new AsyncHttpClientConfig.Builder()
  val client = new NingWSClient(builder.build())

  var input = "";
  while( input != "quit" ){
  	input = readLine()
  	if( input != "quit" ){
	  	val responseFuture: Future[WSResponse] = client.url("https://"+input).get()

		  val result = Await.result(responseFuture, Duration.Inf)
			
			println(result.body)
		}
  }
  
  client.close()

}