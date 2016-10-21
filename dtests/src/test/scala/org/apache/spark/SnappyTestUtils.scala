/*
 * Copyright (c) 2016 SnappyData, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package org.apache.spark

import java.io.{PrintWriter, File}
import java.net.URL
import javax.tools.JavaFileObject

import org.apache.spark.TestUtils.JavaSourceFromString
import org.apache.spark.sql.SnappyContext
import org.apache.spark.sql.collection.{Utils => Utility}


object SnappyTestUtils {

  private val SOURCE = JavaFileObject.Kind.SOURCE

  def verifyClassOnExecutors(snc: SnappyContext, className: String,
                             version: String, count: Int, pw: PrintWriter): Unit = {
    val countInstances = Utility.mapExecutors(snc,
      () => {
        if (SnappyTestUtils.loadClass(className, version)) {
          Seq(1).iterator
        } else Iterator.empty
      }).count

    assert(countInstances == count)
    pw.println("Class is available on all executors : numExecutors having the class loaded is same as numServers in test = " + countInstances + " class version: " + version);
    }


  def getJavaSourceFromString(name: String, code: String): JavaSourceFromString = {
    new JavaSourceFromString(name, code)
  }


  def createCompiledClass(className: String,
                          destDir: File,
                          sourceFile: JavaSourceFromString,
                          classpathUrls: Seq[URL] = Seq()): File = {

    TestUtils.createCompiledClass(className, destDir, sourceFile, classpathUrls)

  }

  def createJarFile(files: Seq[File],
                    tempDir: String
                    ) = {
    val jarFile = new File(tempDir, "testJar-%s.jar".format(System.currentTimeMillis()))
    TestUtils.createJar(files, jarFile)
    jarFile.getName
  }

  @throws[ClassNotFoundException]
  def loadClass(className: String,
                version: String = ""): Boolean = {
    val catchExpectedException: Boolean = version.isEmpty
    val loader = Thread.currentThread().getContextClassLoader
    assert(loader != null)
    try {
      val fakeClass = loader.loadClass(className).newInstance()
      assert(fakeClass != null)
      assert(fakeClass.toString.equals(version))
      true
    } catch {
      case cnfe: ClassNotFoundException =>
        if (!catchExpectedException) throw cnfe
        else false
    }
  }

}
