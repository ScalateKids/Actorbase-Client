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

class ActorbaseBanner() {
  /**
    * Method that returns an introductive banner for the application.
    *
    * @return a String representing the banner of the application
    */
  def getBanner() : String = {
    var banner = """
                   #    ___   ________________  ____  ____  ___   _____ ______
                   #   /   | / ____/_  __/ __ \/ __ \/ __ )/   | / ___// ____/
                   #  / /| |/ /     / / / / / / /_/ / __  / /| | \__ \/ __/
                   # / ___ / /___  / / / /_/ / _, _/ /_/ / ___ |___/ / /___
                   #/_/  |_\____/ /_/  \____/_/ |_/_____/_/  |_/____/_____/
                   #""".stripMargin('#')

    val version = "version: 1.0.0"
    val un = System.getProperty("user.name")
    val os = System.getProperty("os.name")
    val ov = System.getProperty("os.version")
    val ar = System.getProperty("os.arch")
    val jv = System.getProperty("java.version")
    banner += s"\nActorbase CLI $version\n$un@$os $ov on $ar, Java version: $jv\n"
    banner += "Welcome to Actorbase version 1.0.0\nType help for more information.\n"
    banner.toString() + "\n"
  }
}
