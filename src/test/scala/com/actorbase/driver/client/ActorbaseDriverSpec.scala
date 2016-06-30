/***
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
import com.actorbase.driver.exceptions._

import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec

import org.scalatest.FunSuiteLike
import spray.http.{ ContentType,  HttpEntity }

import com.actorbase.driver.ActorbaseServerMock

/**
  * Class that contains all the tests for the driver package of Actorbase.
  * The tests specifically are checking if the driver make the right requests
  * to che Actorbase server when some methods are called.
  */
class ActorbaseDriverSpec extends WordSpec with Matchers {

  /**
    * Start an Actorbase mock server. this will simulate the responses that the
    * real Actorbase server instance would make
    */
  ActorbaseServerMock.startMock

  /**
    * This contains all the tests for the driver package.
    */
  "ActorbaseDriver instance" should {

    /**
      * TS.DEF3 viene verificato che dovrà essere fornito un driver Scala per interfacciarsi con il database
      * TU.DEF3.1.3
      */
    val driver = ActorbaseDriver("http://admin:Actorb4se@127.0.0.1:8766")

    /**
      * TS.DEF3.1 & Viene verificato che il driver dovrà permettere
      * di effettuare l'autenticazione all'interno del sistema.
      */
    "authenticate to the server during creation" in {
      assert(driver.connection.username == "admin" && driver.connection.password == "Actorb4se")
    }

    "throw WrongCredentialsExc while trying to authenticate a non-existant user" in {
      an [WrongCredentialsExc] should be thrownBy ActorbaseDriver("http://noexists:Actorb4se@127.0.0.1:8766")
    }

    "ask for insert item" in {
      noException should be thrownBy(driver.insertTo("testCollection", false, ("testItem" -> "testPayload"))())
    }

    /**
      * TU.DEF3.2 Si verifica che Il driver dovrà permettere l'esecuzione
      * di comandi per poter eseguire operazioni sulle collezioni
      */

    /**
      * TS.DEF3.2.2 & Viene verificato che il driver dovrà permettere di elencare
      * i nomi delle collezioni presenti all’interno del database
      */
    "ask for list collections" in {
      val response = driver.listCollections
      assert(response.size == 0)
    }

    /**
      * TS.DEF3.2.1 & Viene verificato che il driver dovrà permettere la creazione di una nuova collezione
      */
    "ask for creating a collection" in {
      val response = driver.addCollection("testCollection")
      assert(response.collectionName == "testCollection")
    }


    /**
      *  TU.DEF3.2.1.2 & Si verifica che Il driver dovrà lanciare un'eccezione in caso di nome
      *  collezione già censito durante la procedura di creazione
      */
    "throw an InternalErrorExc when trying to create a collection already inside the system" in {
      an [InternalErrorExc] should be thrownBy driver.addCollection("alreadyInside")
    }


    /**
      * TU.DEF3.2.3.2 & Si verifica che il driver dovrà lanciare un'eccezione in caso di inserimento
      * nome collezione da cancellare non esistente all'interno del sistema
      */
    "throw an InternalErrorExc when trying to remove a collection that does not exists" in {
      an [InternalErrorExc] should be thrownBy driver.dropCollections("notExistent")
    }

    /**
      * TU.DEF3.2.5.3 & Si verifica che il driver dovrà lanciare un'eccezione in caso di inserimento
      * nome collezione a cui aggiungere un collaboratore non esistente all'interno del sistema
      * TU.DEF3.2.6.3 & Si verifica che il driver dovrà lanciare un'eccezione in caso di inserimento
      * nome collezione a cui rimuovere un collaboratore non esistente all'interno del sistema
      */
    "throw an InternalErrorExc when trying to add or remove a contributor to a collection that does not exists" in{
      val response = driver.addCollection("testCollection")
      an [InternalErrorExc] should be thrownBy response.addContributor("contributorName", false)
    }

    /**
      * TU.DEF3.2.5.4 & Si verifica che il driver dovrà lanciare un'eccezione in caso di inserimento
      * username non presente all'interno del sistema durante la procedura di aggiunta collaboratore collezione
      */
    "throw an UndefinedCollectionExc when trying to add a contributor to a collection that does not exists" in{
      an [UndefinedCollectionExc] should be thrownBy driver.getCollection("testCollection2")
    }

    /**
      * TU.DEF3.2.7.3 Si verifica che il driver dovrà esportare tutto il contenuto del
      * sistema in caso di inserimento di una lista nomi collezioni vuota
      */
    "ask for all the database collections" in {
      val response = driver.getCollections
      assert(response.count == 0)
    }

    /**
      * TS.DEF3.2.7 & Viene verificato che il dovrà permettere di
      * esportare collezioni su file JSON
      */
    "ask for a collection and for an export to file" in {
      val response = driver.addCollection("testCollection")
      assert(response.collectionName == "testCollection")
    }

    /**
      * TS.DEF3.2.3 & Viene verificato che il driver dovrà
      * permettere di cancellare una o più collezioni
      */
    "ask for deleting a collection" in {
      noException should be thrownBy(driver.dropCollections("testCollection"))
    }

    /**
      * TS.DEF3.2.5 & Viene verificato che il driver dovrà permettere di
      * aggiungere collaboratori ad una collezione del sistema
      */
    /**
      "add a contributor to a collection" in {
      val response = driver.getCollection("contributorCollection")
      noException should be thrownBy(response.addContributor("username", false))
      } */

    /**
      * TS.DEF3.2.6 & Viene verificato che il driver dovrà permettere
      * di rimuovere un collaboratore da una collezione del sistema
      */
    /**
      "remove a contributor from a collection" in {
      val response = driver.getCollection("contributorCollection")
      noException should be thrownBy(response.removeContributor("username"))
      } */


    /*** ITEM PART ***/

    /**
      * TU.DEF3.3 & Si verifica che il driver dovrà permettere di eseguire comandi per poter
      * eseguire operazioni sugli items
      */

    /**
      * TU.DEF3.3.1.2.2 & Si verifica che Il driver dovrà lanciare un eccezione in caso
      * il file JSON non sia presente nel filesystem secondo path specificato
      */
    "should throw a FileNotFoundException exception while importing from a file that does not exists" in {
      an [java.io.FileNotFoundException] should be thrownBy(driver.importData("src/test/resources/filenotexisting.json"))
    }

    /**
      * TU.DEF3.3.1.2.3 & Si verifica che il driver dovrà lanciare un'eccezione in caso
      * di inserimento di un path che punta ad un file JSON non correttamente formato
      */
    "should throw a MalformedFileExc exception while importing from a file that does not exists" in {
      an [MalformedFileExc] should be thrownBy(driver.importData("src/test/resources/malformedFile.json"))
    }

    /**
      * TU.DEF3.3.1.4 & Si verifica che il driver dovrà lanciare un'eccezione in caso di
      * inserimento di un item con flag di sovrascrittura non attivo e una chiave
      * già esistente all'interno della collezione
      */


    /**
      * TU.DEF3.3.2.3 & Si verifica che il driver dovrà lanciare un'eccezione nel caso di
      * inserimento di un nome collezione non esistente all'interno del sistema durante la
      * procedura di cancellazione item
      */
    "ask throw a UndefinedCollection exception while trying to remove an item from a collection that does not exists" in {
      an [InternalErrorExc] should be thrownBy(driver.removeFrom("notExistent", "testItem"))()
    }

    /**
      * TS.DEF3.3.1 & Viene verificato che il driver dovrà permettere di
      * inserire un nuovo item
      * TS.DEF3.3.1.1 & Viene verificato che il driver dovrà permettere di
      * inserire un nuovo item specificandone gli attributi
      * TS.DEF3.4 & Viene verificato che il driver dovrà permettere di effettuare
      * ricerche su una o più collezioni all'interno del sistema
      */
    "ask for a single item" in {  // todo rivedere, il driver credo non debba lanciare una com.fasterxml.jackson.core.JsonParseException se l'item non c'è
                                  //val response = driver.find("testItem", "testCollection")
      noException should be thrownBy(driver.find("testItemToReturn", "testCollection"))
    }

    "ask for inserting one item without overwriting" in {
      //val response = driver.insertTo("testCollection", false, ("testItem" ->"testPayload"))
      noException should be thrownBy(driver.insertTo("testCollection", false, ("testItem" ->"testPayload")))()
    }


    "ask for inserting one item allowing overwriting" in {
      //val response = driver.insertTo("testCollection", true, ("testItem" ->"testPayload"))
      noException should be thrownBy(driver.insertTo("testCollection", true, ("testItem" ->"testPayload")))()
    }

    /**
      * TS.DEF3.3.2 & Viene verificato che il driver dovrà permettere di cancellare uno o più item dal sistema
      */
    "ask for deleting one item" in {
      val response = driver.removeFrom("testCollection", "testItem")()
      //assert(response.getClass() == ActorbaseObject)
    }

    /**
      * TS.DEF3.3.1.2 & Viene verificato che il driver dovrà
      * permettere di inserire nuovi item importandoli da file JSON
      */
    "import items from file" in {
      noException should be thrownBy(driver.importData("src/test/resources/importTest.json"))
    }


    /**    ITERABLE RESPONSE PART      **/

    /**
      * TS.DEF3.8 & Viene verificato che il Driver dovrà strutturare
      * i dati in output in maniera navigabile
      */
    /**
      * TU.OBF3.8.1 & Si verifica che Il Driver dovrà poter restituire sequenze di collezioni navigabili
      */
    "return sequence of iterable collections" in {
      val response = driver.getCollections
      assert(response.count == 0)
    }

    /**
      * TU.OBF3.8.2 & Si verifica che Il Driver dovrà poter restituire collezioni navigabili
      //  assert(response.count == 0)
      }*/

    /**
      * TU.OBF3.8.2 & Si verifica che Il Driver dovrà poter restituire collezioni navigabili
      */
    "return an iterable collection" in {
      val response = driver.getCollection("testNavigableCollection")
      assert(response.count == 1)
    }

    /**
      * TU.OBF3.8.3 & Si verifica che il driver dovrà poter restituire item
      */
    "return an item" in {
      noException should be thrownBy(driver.find("testItemToReturn", "testCollection"))
    }

    /**              USERS PART          **/

    /**
      * TU.OBF3.6 si verifica che il driver dovrà permettere di effettuare operazioni di
      * gestione degli utente all'interno del sistema da parte di un utente amministratore
      */
    /**
      * TS.DEF3.6.1 & Viene verificato che il driver dovrà permettere
      * a utenti amministratori di aggiungere un nuovo utente al sistema
      */
    "add a user to the system" in {
      noException should be thrownBy(driver.addUser("username"))
    }

    /**
      * TS.DEF3.6.2 & Viene verificato che il driver dovrà
      * permettere a utenti amministratori di rimuovere un utente dal sistema
      */
    "remove a user to the system" in {
      noException should be thrownBy(driver.removeUser("username"))
    }

    /**
      * TS.DEF3.6.3 & Viene verificato che il driver dovrà permettere a utenti
      * amministratori di effettuare il reset della password ad un utente all'interno del sistema
      */
    "reset a password of a user" in {
      noException should be thrownBy(driver.resetPassword("username"))
    }

    /**
      * TS.DEF3.7 & Viene verificato che il driver dovrà permettere di
      * modificare la propria password
      */
    "change the password of a user" in {
      noException should be thrownBy(driver.changePassword("newP4ssword"))
    }
  }
}
