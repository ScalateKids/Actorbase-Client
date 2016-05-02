package com.actorbase.driver.client

import com.actorbase.driver.client.SSLClient
import com.actorbase.driver.client.ActorbaseClient

class Connector (val ssl : SSLClient , val client : ActorbaseClient)
{
    lazy val sslClient : SSLClient = ssl
    lazy val actorbaseClient : ActorbaseClient = client 

}
