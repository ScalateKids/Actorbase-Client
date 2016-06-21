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

package com.actorbase.cli.views

import com.actorbase.driver.ActorbaseDriver

class ActorbasePrompt(connectionInfo: ActorbaseDriver.Connection) extends PromptProvider{

  private val os = System.getProperty("os.name")
  /**
    * Method that returns a string representing the prompt.
    *
    * @return a String representing the prompt of the Actorbase application
    */
  override def getPrompt: String = {
    val addr = connectionInfo.address
    val user = connectionInfo.username
    val prompt =     user + "@" + addr + "$~: "
    os match {
      case nix if (nix.contains("Linux") || nix.contains("Darwin") || nix.contains("Windows")) => s"\u001B[1m" + prompt + "\u001B[0m"
      case _ => prompt
    }
  }
}
