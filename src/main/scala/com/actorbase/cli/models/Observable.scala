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
  * @author Scalatekids
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.cli.models

import com.actorbase.cli.views.Observer

import scala.collection.mutable.ListBuffer

/**
  * A trait of the ActorbaseCLI.
  * This trait describe de design pattern and keep update model status
  */
trait Observable {

  private var observers : ListBuffer[Observer] = new ListBuffer[Observer]
  private var state : String = ""
  /**
    * change the state of the trait
    * @param s a String that rappresent the new state to set
    **/
  def setState(s: String): Unit = state = s

  /**
    * return the state of the trait
    * @return a string with the state of the trait
    **/
  def getState : String = state
  /**
    * return the state of the trait
    * @param observer add class to the list of observed class
    **/
  def attach(observer: Observer) : Unit = observers :+= observer
  /**
    * return the state of the trait
    * @param observer remove class to the list of observed class
    **/
  def detach(observer: Observer) : Unit = observers -= observer
  /**
    * recall an update on all observers
    **/
  def notifyAllObservers() : Unit = {
    for(observer <- observers)
      observer.update(this)
  }

}
