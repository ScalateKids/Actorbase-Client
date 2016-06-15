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

import org.scalatest.FunSuiteLike
import spray.http.{ ContentType,  HttpEntity }

import com.actorbase.driver.ActorbaseServerMock

/**
  * Insert description here
  *
  * @param
  * @return
  * @throws
  */
class ActorbaseDriverSpec extends WordSpec with Matchers{

  ActorbaseServerMock.startMock

  /*
   * TS.DEF3 viene verificato che dovrà essere fornito un driver Scala per interfacciarsi con il database
   * TU.DEF3.1.3
   */
  "ActorbaseDriver instance" should {
    "throw WrongCredentialsExc while trying to authenticate a non-existant user" in {
      an [WrongCredentialsExc] should be thrownBy ActorbaseDriver("http://noexists:Actorb4se@127.0.0.1:8766")
    }

    val driver = ActorbaseDriver("http://admin:Actorb4se@127.0.0.1:8766")
    
    /*
     * TS.DEF3.1 & Viene verificato che il \gloss{driver} dovrà permettere 
     * di effettuare l'autenticazione all'interno del sistema.
     */
    "create an instance driver and authenticate to the server" in {
      assert(driver != None)  
    }


    "make http request test" in {
      import scalaj.http._
      val res: HttpResponse[String] = Http("http://127.0.0.1:8766/testscalaj").asString
      //println(res.code + " " + res.body)
      res.code should be(200)
    }
    // }

    // to be *scommented* when the feature will return something
     "ask for insert item" in {
       val response = driver.insertTo("testCollection", false, ("testItem" -> "testPayload"))
       println(response)
       response.code should be(200)
     }

    /* 
     * TU.DEF3.2 Si verifica che Il \gloss{driver} dovrà permettere l'esecuzione 
     * di comandi per poter eseguire operazioni sulle \gloss{collezioni} 
     */
    

    /*
     * TS.DEF3.2.2 & Viene verificato che il driver dovrà permettere di elencare
     * i nomi delle collezioni presenti all’interno del database
     */
    // it should {
    "ask for list collections" in {
      val response = driver.listCollections
      //println(response)
      assert(response.size == 0)
    }
    // }


    /*  
     * TS.DEF3.2.1 & Viene verificato che il driver dovrà permettere la creazione di una nuova \gloss{collezione}
     */
    // it should {
    "ask for creating a collection" in {
      val response = driver.addCollection("testCollection")
      assert(response.collectionName == "testCollection")
    }
    // }

    /*  TODO
     *  TU.DEF3.2.1.2 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di nome \gloss{collezione} già censito durante la procedura di creazione
     */

     /* TODO
      * TU.DEF3.2.3.2 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di inserimento nome \gloss{collezione} da cancellare non esistente all'interno del sistema
      */

    /* TODO
      TU.DEF3.2.4.3 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di inserimento di un nome \gloss{collezione} da modificare non esistente all'interno del sistema durante la procedura di modifica nome \gloss{collezione} &   & DEF3.2.4.3    \\
  
      TU.DEF3.2.4.4 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di inserimento di un nuovo nome \gloss{collezione} già censito a sistema durante la procedura di modifica nome \gloss{collezione} &   & DEF3.2.4.4    \\
  
      TU.DEF3.2.5.3 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di inserimento nome \gloss{collezione} a cui aggiungere un \gloss{collaboratore} non esistente all'interno del sistema &   & DEF3.2.5.3    \\
  
      TU.DEF3.2.5.4 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di inserimento username non presente all'interno del sistema durante la procedura di aggiunta \gloss{collaboratore} a \gloss{collezione} &   & DEF3.2.5.4    \\
  
      TU.DEF3.2.6.3 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di inserimento nome \gloss{collezione} a cui rimuovere un \gloss{collaboratore} non esistente all'interno del sistema &   & DEF3.2.6.3    \\
    */

    /*
     * TU.DEF3.2.7.3 Si verifica che il driver dovrà esportare tutto il contenuto del 
     * sistema in caso di inserimento di una lista nomi \gloss{collezioni} vuota
     */
    "ask for all the database collections" in {
      import com.actorbase.driver.data.ActorbaseCollectionMap
      val response = driver.getCollections
      assert(response.getClass == ActorbaseCollectionMap) //TODO fails
    }

    /*
     * TS.DEF3.2.7 & Viene verificato che il \gloss{driver} dovrà permettere di 
     * esportare \gloss{collezioni} su file \gloss{JSON}
     */
    // it should {
    "ask for a collection and for an export to file" in {
      val response = driver.getCollection("testCollection")
      assert(response.collectionName == "testCollection")
    }
    // }

// TODO fails perchè non c'è case OK in driver?
    /*
     * TS.DEF3.2.3 & Viene verificato che il \gloss{driver} dovrà 
     * permettere di cancellare una o più \gloss{collezioni}
     */
    // it should {
    "ask for deleting a collection" in {
      //val response = driver.dropCollections("testCollection")
      noException should be thrownBy(driver.dropCollections("testCollection"))
    }
    // }

    /*
     * TS.DEF3.2.4 & Viene verificato che il \gloss{driver} dovrà 
     * permettere di modificare il nome delle {collezioni}
     */

    /*
     * TS.DEF3.2.5 & Viene verificato che il \gloss{driver} dovrà permettere di 
     * aggiungere \gloss{collaboratori} ad una \gloss{collezione} del sistema
     */
     /*"add a contributor to a collection" in {
        noException should be thrownBy(driver.addContributor(""))
     }*/

    /*
     * TS.DEF3.2.6 & Viene verificato che il \gloss{driver} dovrà permettere 
     * di rimuovere un \gloss{collaboratore} da una \gloss{collezione} del sistema
     */


    /*** ITEM PART ***/

    /*
     * TU.DEF3.3 & Si verifica che Il \gloss{driver} dovrà permettere di eseguire comandi per poter eseguire operazioni sugli items
     */

    /*
    TU.DEF3.3.1.2.2 & Si verifica che Il \gloss{driver} dovrà lanciare un eccezione in caso il file \gloss{JSON} non sia presente nel filesystem secondo \gloss{path} specificato &   & DEF3.3.1.2.2    \\
    TU.DEF3.3.1.2.3 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di inserimento di un \gloss{path} che punta ad un file \gloss{JSON} non correttamente formato &   & DEF3.3.1.2.3    \\
    TU.DEF3.3.1.4 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione in caso di inserimento di un \gloss{item} con \gloss{flag} di sovrascrittura non attivo e una chiave già esistente all'interno della \gloss{collezione} &   & DEF3.3.1.4    \\
    TU.DEF3.3.2.3 & Si verifica che Il \gloss{driver} dovrà lanciare un'eccezione nel caso di inserimento di un nome \gloss{collezione} non esistente all'interno del sistema durante la procedura di cancellazione \gloss{item} 
    */

    /*
     * TS.DEF3.3.1 & Viene verificato che il \gloss{driver} dovrà permettere di 
     * inserire un nuovo \gloss{item}
     * TS.DEF3.3.1.1 & Viene verificato che il \gloss{driver} dovrà permettere di 
     * inserire un nuovo \gloss{item} specificandone gli attributi
     * TS.DEF3.4 & Viene verificato che il \gloss{driver} dovrà permettere di effettuare 
     * ricerche su una o più \gloss{collezioni} all'interno del sistema
     */
    "ask for a single item" in {
      //val response = driver.find("testItem", "testCollection")
      noException should be thrownBy(driver.find("testItem", "testCollection"))
    }

    "ask for inserting one item without overwriting" in {
      //val response = driver.insertTo("testCollection", false, ("testItem" ->"testPayload"))
      noException should be thrownBy(driver.insertTo("testCollection", false, ("testItem" ->"testPayload")))
    }
    

    "ask for inserting one item allowing overwriting" in {
      //val response = driver.insertTo("testCollection", true, ("testItem" ->"testPayload"))
      noException should be thrownBy(driver.insertTo("testCollection", true, ("testItem" ->"testPayload")))
    }
    
    /*
     * TS.DEF3.3.2 & Viene verificato che il \gloss{driver} dovrà permettere di cancellare uno o più \gloss{item} dal sistema 
     */
    "ask for deleting one item" in {
      val response = driver.removeFrom("testCollection", "testItem")
      //assert(response.getClass() == ActorbaseObject)
    }

    /*
     * TS.DEF3.3.1.2 & Viene verificato che il \gloss{driver} dovrà 
     * permettere di inserire nuovi \gloss{item} da file \gloss{JSON}
     */
//manca case ok nel driver
    "import items from file" in {
      noException should be thrownBy(driver.importFromFile("src/test/resources/importTest.json"))
    }


    /*    ITERABLE RESPONSE PART      */

    /*
     * TS.DEF3.8 & Viene verificato che il \gloss{Driver} dovrà strutturare 
     * i dati in output in maniera navigabile
     */
    /*
     * TU.OBF3.8.1 & Si verifica che Il \gloss{Driver} dovrà poter restituire sequenze di \gloss{collezioni} navigabili
     */
    "return sequence of iterable collections" in {
      val response = driver.getCollections
    //  assert(response.count == 0)
    } 

    /* 
     * TU.OBF3.8.2 & Si verifica che Il \gloss{Driver} dovrà poter restituire \gloss{collezioni} navigabili & OK   & OBF3.8.2    \\
     */
    "return an iterable collection" in {
      val response = driver.getCollection("testNavigableCollection")
      assert(response.count == 0)
    } 

    /*
     * TU.OBF3.8.3 & Si verifica che Il \gloss{Driver} dovrà poter restituire \gloss{item}  & OK   & OBF3.8.3    \\
     */
    "return an item" in {
      noException should be thrownBy(driver.find("testItem", "testCollection"))
    }     

    /*              USERS PART          */

    /*
     * TU.OBF3.6 si verifica che Il driver dovrà permettere di effettuare operazioni di 
     * gestione degli utente all'interno del sistema da parte di un utente amministratore
     */  


    /*
     * TS.DEF3.6.1 & Viene verificato che il \gloss{driver} dovrà permettere 
     * a utenti amministratori di aggiungere un nuovo utente al sistema
     */

    /*
     * TS.DEF3.6.2 & Viene verificato che il \gloss{driver} dovrà 
     * permettere a utenti amministratori di rimuovere un utente dal sistema
     */

    /*
     * TS.DEF3.6.3 & Viene verificato che il \gloss{driver} dovrà permettere a utenti 
     * amministratori di effettuare il reset della password ad un utente all'interno del sistema
     */ 

    /*
     * TS.DEF3.7 & Viene verificato che il \gloss{driver} dovrà permettere di 
     * modificare la propria password  
     */

    /*
     * TS.DEF3.5 viene verificato che il \gloss{driver} dovrà permettere di effettuare il logout dal sistema
     */ 
    "logout from the system" in {
      noException should be thrownBy(driver.logout)
    }
  }
}
