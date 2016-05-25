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

import scala.pickling.Defaults._

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.jackson.JsonMethods._

trait Serializer {

  /**
    * Serialization method. Converts an object of type Any to an array of bytes
    *
    * @param o object of type Any designated for conversion to Byte[Array]
    * @return an Array[Byte] type
    * @throws
    */
  def serialize2byteArray(o: Any): Array[Byte] = {
    import scala.pickling.binary._
    o.pickle.value
  }

  /**
    * Serialization method. Converts an object of type Any to a JSON string
    *
    * @param o object of type Any designated for conversion to Byte[Array]
    * @return a String object in JSON format
    * @throws
    */
  def serialize2JSON(o: Any): String = {
    import scala.pickling.json._
    o.pickle.value
  }

  /**
    * Serialization method. Converts an object of type Any to a JSON string
    *
    * @param o object of type AnyRef designated for conversion to Byte[Array]
    * @return a String object in JSON format
    * @throws
    */
  def serialize2JSON4s(o: AnyRef): String = {
    implicit val formats = Serialization.formats(NoTypeHints)
    pretty(parse(write(o)))
  }

  // def serialize2JSONSpray(o: AnyRef): String = {
  //   o.toJson.prettyPrint
  // }

  /**
    * Deserialization method. Converts an object of type Array[Byte] to a
    * refernce of type Any
    *
    * @param bytes an array of bytes designated for conversion
    * @return a reference to the object deserialized
    * @throws
    */
  def deserializeFromByteArray(bytes: Array[Byte]): AnyVal = {
    import scala.pickling.binary._
    bytes.unpickle[AnyVal]
  }

  def deserializeDebugger(bytes: Array[Byte]): Any = {
    import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
    val in = new ObjectInputStream(new ByteArrayInputStream(bytes))
    in.readObject().asInstanceOf[Any]
  }

  /**
    * Deserialization method. Converts an object of type Array[Byte] to a
    * reference of type Any
    *
    * @param json a String object in format JSON designated for conversion
    * @return a reference to the object deserialized
    * @throws
    */
  def deserializeFromJSON(json: String): Any = {
    import scala.pickling.json._
    json.unpickle[Any]
  }

}
