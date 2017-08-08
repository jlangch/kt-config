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
import java.io.StringReader


class ConfigTest {

    @Test
    fun testCreateFromStream() {
        val config = """user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader.create(config.byteInputStream()).read()

        Assert.assertEquals(cfg.size(), 3)
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
    }

    @Test
    fun testCreateFromStreamCharset() {
        val config = """user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader.create(config.byteInputStream(), Charsets.UTF_8).read()

        Assert.assertEquals(cfg.size(), 3)
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
    }

    @Test
    fun testCreateFromReader() {
        val config = """user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader.create(StringReader(config)).read()

        Assert.assertEquals(cfg.size(), 3)
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
    }

    @Test
    fun testCreateFromClasspath() {
        val cfg = ConfigReader.create(
                        "jlang/kt_config/test.config",
                        this.javaClass.getClassLoader()
                    ).read()

        Assert.assertEquals(cfg.size(), 3)
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
    }

    @Test
    fun testEmptyConfig() {
         val cfg = ConfigReader.empty()

        Assert.assertTrue(cfg.isEmpty())
        Assert.assertEquals(cfg.size(), 0)
    }

    @Test
    fun testConfigToMap() {
        val config = """user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "/foo/abc"
                       |}
                     """.trimMargin()

        val map = ConfigReader(config).read().toMap()

        Assert.assertEquals(map.size, 4)
        Assert.assertEquals(map.get("user"), "john.doe")
        Assert.assertEquals(map.get("section1.host"), "foo.org")
        Assert.assertEquals(map.get("section1.port"), "8000")
        Assert.assertEquals(map.get("section1.path"), "/foo/abc")
    }

    @Test
    fun testConfigToProperties() {
        val config = """user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "/foo/abc"
                       |}
                     """.trimMargin()

        val props = ConfigReader(config).read().toProperties()

        Assert.assertEquals(props.getProperty("user"), "john.doe")
        Assert.assertEquals(props.getProperty("section1.host"), "foo.org")
        Assert.assertEquals(props.getProperty("section1.port"), "8000")
        Assert.assertEquals(props.getProperty("section1.path"), "/foo/abc")
    }

    @Test
    fun testConfigCopy() {
        val config = """user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "/foo/abc"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read().copy()

        Assert.assertEquals(cfg.size(), 4)
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/foo/abc")
    }

    @Test
    fun testSubConfig() {
        val config = """def home = "/foo/org"
                       |
                       |section1 {
                       |   user = "john.doe"
                       |}
                       |section2 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()
        Assert.assertEquals(cfg.size(), 4)

        val cfg2 = ConfigReader(config).read().getSubConfig("section1", "section2")
        Assert.assertEquals(cfg2.size(), 4)
        Assert.assertEquals(cfg2.get("user"), "john.doe")
        Assert.assertEquals(cfg2.get("host"), "foo.org")
        Assert.assertEquals(cfg2.get("port"), "8000")
        Assert.assertEquals(cfg2.get("path"), "/foo/org/abc")

        val cfg3 = ConfigReader(config).read().getSubConfig("section1")
        Assert.assertEquals(cfg3.size(), 1)
        Assert.assertEquals(cfg3.get("user"), "john.doe")

        val cfg4 = ConfigReader(config).read().getSubConfig("section2")
        Assert.assertEquals(cfg4.size(), 3)
        Assert.assertEquals(cfg4.get("host"), "foo.org")
        Assert.assertEquals(cfg4.get("port"), "8000")
        Assert.assertEquals(cfg4.get("path"), "/foo/org/abc")

        val cfg5 = ConfigReader(config).read().getSubConfig("xxxx")
        Assert.assertEquals(cfg5.size(), 0)

        val cfg6 = ConfigReader(config).read().getSubConfig("")
        Assert.assertEquals(cfg6.size(), 0)
    }

    @Test
    fun testConfigMerge() {
        val config1 = """user = "john.doe"
                        |user1 = "john.doe.1"
                        |section1 {
                        |   s11 = "11"
                        |   s12 = "12"
                        |}
                        |section2 {
                        |   s21 = "21"
                        |   s22 = "22"
                        |}
                      """.trimMargin()

        val config2 = """user = "john.doe--2"
                        |user2 = "john.doe.2"
                        |section1 {
                        |   s11 = "11--2"
                        |   s12 = "12--2"
                        |}
                        |section3 {
                        |   s31 = "31"
                        |   s32 = "32"
                        |}
                      """.trimMargin()

        val cfg1 = ConfigReader(config1).read()
        val cfg2 = ConfigReader(config2).read()
        val cfg = cfg1.merge(cfg2)

        Assert.assertEquals(cfg.size(), 9)
        Assert.assertEquals(cfg.get("user"), "john.doe--2")
        Assert.assertEquals(cfg.get("user1"), "john.doe.1")
        Assert.assertEquals(cfg.get("user2"), "john.doe.2")
        Assert.assertEquals(cfg.get("section1.s11"), "11--2")
        Assert.assertEquals(cfg.get("section1.s12"), "12--2")
        Assert.assertEquals(cfg.get("section2.s21"), "21")
        Assert.assertEquals(cfg.get("section2.s22"), "22")
        Assert.assertEquals(cfg.get("section3.s31"), "31")
        Assert.assertEquals(cfg.get("section3.s32"), "32")
    }

}