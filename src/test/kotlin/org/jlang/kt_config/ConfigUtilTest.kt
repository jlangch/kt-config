/*
 * Copyright 2017 JLang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jlang.kt_config

import org.testng.Assert
import org.testng.annotations.Test


class ConfigUtilTest {

    @Test
    fun testToMap() {
        val config = """user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "/foo/abc"
                       |}
                     """.trimMargin()

        val map = ConfigReader.create(config.byteInputStream()).read().toMap()

        Assert.assertEquals(map.size, 4)
        Assert.assertEquals(map.get("user"), "john.doe")
        Assert.assertEquals(map.get("section1.host"), "foo.org")
        Assert.assertEquals(map.get("section1.port"), "8000")
        Assert.assertEquals(map.get("section1.path"), "/foo/abc")
    }

    @Test
    fun testToProperties() {
        val config = """user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "/foo/abc"
                       |}
                     """.trimMargin()

        val props = ConfigReader.create(config.byteInputStream()).read().toProperties()

        Assert.assertEquals(props.getProperty("user"), "john.doe")
        Assert.assertEquals(props.getProperty("section1.host"), "foo.org")
        Assert.assertEquals(props.getProperty("section1.port"), "8000")
        Assert.assertEquals(props.getProperty("section1.path"), "/foo/abc")
    }

}