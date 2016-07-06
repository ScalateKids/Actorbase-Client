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

package com.actorbase.driver.client

import java.io.{ ByteArrayOutputStream, ObjectOutputStream }
import java.util.Base64
import java.net.URLEncoder

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.jackson.JsonMethods._


/**
  * Trait to add serializing and deserializing capabilities to all class
  * extending or mixing this, support different genres of serialization and
  * deserialization, including array of bytes and JSON
  */
trait Serializer {

  def urlEncode(s: String): String = URLEncoder.encode(s, "UTF-8")

  def toBase64(in: Array[Byte]): String = Base64.getUrlEncoder.encodeToString(in)

  def toBase64FromString(in: String): String = toBase64(in.getBytes("UTF-8"))

  /**
    * Serialization method. Converts an object of type Any to an array of bytes
    *
    * @param o object of type Any designated for conversion to Byte[Array]
    * @return an Array[Byte] type
    * @throws
    */
  def serialize(o: Any): String = {
    val bos = new ByteArrayOutputStream()
    var oos = new ObjectOutputStream(bos)
    oos.writeObject(o)
    val bytes = bos.toByteArray()
    oos.close()
    bos.close()
    toBase64(bytes)
  }

  /**
    * Serialization method. Converts an object of type Any to a JSON string
    *
    * @param o object of type AnyRef designated for conversion to Byte[Array]
    * @return a String object in JSON format
    * @throws
    */
  def toJSON(o: AnyRef): String = {
    implicit val formats = Serialization.formats(NoTypeHints)
    pretty(parse(write(o)))
  }

}
