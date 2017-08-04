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

import org.jlang.kt_config.ConfigException
import org.jlang.kt_config.ConfigReader
import org.testng.Assert
import org.testng.annotations.Test


class ConfigReaderTest {

    @Test
    fun testEmptyConfig() {
        val map1 = ConfigReader("").read().toMap()
        Assert.assertEquals(map1.size, 0)

        val map2 = ConfigReader(" ").read().toMap()
        Assert.assertEquals(map2.size, 0)

        val map3 = ConfigReader("   ").read().toMap()
        Assert.assertEquals(map3.size, 0)

        val map4 = ConfigReader("\n").read().toMap()
        Assert.assertEquals(map4.size, 0)

        val map5 = ConfigReader("\n\n\n").read().toMap()
        Assert.assertEquals(map5.size, 0)
    }

    @Test
    fun testSimpleConfig_getDefault() {
        val map1 = ConfigReader("user = 'john.doe'").read().toMap()
        Assert.assertEquals(map1.size, 1)
        Assert.assertEquals(map1.get("user"), "john.doe")
        Assert.assertEquals(map1.getOrDefault("user", "arthur.dent"), "john.doe")

        Assert.assertEquals(map1.get("x"), null)
        Assert.assertEquals(map1.getOrDefault("x", null), null)
        Assert.assertEquals(map1.getOrDefault("x", "arthur.dent"), "arthur.dent")
    }

    @Test
    fun testSimpleConfig_1item_DoubleQuotedValues() {
        val map1 = ConfigReader("""user = "john.doe"""").read().toMap()
        Assert.assertEquals(map1.size, 1)
        Assert.assertEquals(map1.get("user"), "john.doe")

        val map2 = ConfigReader("""  user    =    "john.doe"    """).read().toMap()
        Assert.assertEquals(map2.size, 1)
        Assert.assertEquals(map2.get("user"), "john.doe")

        val map3 = ConfigReader("\n\n\nuser = \"john.doe\"\n\n\n").read().toMap()
        Assert.assertEquals(map3.size, 1)
        Assert.assertEquals(map3.get("user"), "john.doe")

        val map4 = ConfigReader("""x.user = "john.doe"""").read().toMap()
        Assert.assertEquals(map4.size, 1)
        Assert.assertEquals(map4.get("x.user"), "john.doe")

        val map5 = ConfigReader("""x.y.user = "john.doe"""").read().toMap()
        Assert.assertEquals(map5.size, 1)
        Assert.assertEquals(map5.get("x.y.user"), "john.doe")

        val map6 = ConfigReader("""user = "j'o'h'n.d'o'e"""").read().toMap()
        Assert.assertEquals(map6.size, 1)
        Assert.assertEquals(map6.get("user"), "j'o'h'n.d'o'e")
    }


    @Test
    fun testSimpleConfig_1item_SingleQuotedValues() {
        val map1 = ConfigReader("user = 'john.doe'").read().toMap()
        Assert.assertEquals(map1.size, 1)
        Assert.assertEquals(map1.get("user"), "john.doe")

        val map2 = ConfigReader("  user    =    'john.doe'    ").read().toMap()
        Assert.assertEquals(map2.size, 1)
        Assert.assertEquals(map2.get("user"), "john.doe")

        val map3 = ConfigReader("\n\n\nuser = 'john.doe'\n\n\n").read().toMap()
        Assert.assertEquals(map3.size, 1)
        Assert.assertEquals(map3.get("user"), "john.doe")

        val map4 = ConfigReader("x.user = 'john.doe'").read().toMap()
        Assert.assertEquals(map4.size, 1)
        Assert.assertEquals(map4.get("x.user"), "john.doe")

        val map5 = ConfigReader("x.y.user = 'john.doe'").read().toMap()
        Assert.assertEquals(map5.size, 1)
        Assert.assertEquals(map5.get("x.y.user"), "john.doe")

        val map6 = ConfigReader("""user = 'j"o"h"n.d"o"e'""").read().toMap()
        Assert.assertEquals(map6.size, 1)
        Assert.assertEquals(map6.get("user"), "j\"o\"h\"n.d\"o\"e")
    }

    @Test
    fun testSimpleConfig_2item() {
        val map1 = ConfigReader("host = 'foo.org' \n port = '8000'").read().toMap()
        Assert.assertEquals(map1.size, 2)
        Assert.assertEquals(map1.get("host"), "foo.org")
        Assert.assertEquals(map1.get("port"), "8000")

        val map2 = ConfigReader("x.host = 'foo.org' \n x.y.port = '8000'").read().toMap()
        Assert.assertEquals(map2.size, 2)
        Assert.assertEquals(map2.get("x.host"), "foo.org")
        Assert.assertEquals(map2.get("x.y.port"), "8000")

        val map3 = ConfigReader("x.y.host = 'foo.org' \n\n x.port = '8000'").read().toMap()
        Assert.assertEquals(map3.size, 2)
        Assert.assertEquals(map3.get("x.y.host"), "foo.org")
        Assert.assertEquals(map3.get("x.port"), "8000")
    }

    @Test
    fun testSimpleConfig_DuplicateItem() {
        val map1 = ConfigReader("user = 'john.doe' \n user = 'arthur.dent'").read().toMap()
        Assert.assertEquals(map1.size, 1)
        Assert.assertEquals(map1.get("user"), "arthur.dent")
    }

    @Test
    fun testEmptySection() {
        val map1 = ConfigReader("section1 { }").read().toMap()
        Assert.assertEquals(map1.size, 0)

        val map2 = ConfigReader("section1 { } \n section2 { }").read().toMap()
        Assert.assertEquals(map2.size, 0)

        val map3 = ConfigReader("section1 { } \n section2 { } \n section1 { }").read().toMap()
        Assert.assertEquals(map3.size, 0)
    }

    @Test
    fun testSimpleSection() {
        val config = """section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |}
                     """.trimMargin()

        val map = ConfigReader(config).read().toMap()

        Assert.assertEquals(map.size, 2)
        Assert.assertEquals(map.get("section1.host"), "foo.org")
        Assert.assertEquals(map.get("section1.port"), "8000")
    }

    @Test
    fun testComplexWithDefinition() {
        val config = """def home = "/foo/org"
                       |
                       |user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        val map = ConfigReader(config).read().toMap()
        Assert.assertEquals(map.size, 4)
        Assert.assertEquals(map.get("user"), "john.doe")
        Assert.assertEquals(map.get("section1.host"), "foo.org")
        Assert.assertEquals(map.get("section1.port"), "8000")
        Assert.assertEquals(map.get("section1.path"), "/foo/org/abc")
    }

    @Test
    fun testComplexWithDefinition2() {
        val config = """def home = "/foo/org"
                       |def x1 = "foo"
                       |def x2 = "org"
                       |
                       |user = "john.doe"
                       |section1 {
                       |   host = "${'$'}{x1}.${'$'}{x1}.${'$'}{x2}"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        val map = ConfigReader(config).read().toMap()

        Assert.assertEquals(map.size, 4)
        Assert.assertEquals(map.get("user"), "john.doe")
        Assert.assertEquals(map.get("section1.host"), "foo.foo.org")
        Assert.assertEquals(map.get("section1.port"), "8000")
        Assert.assertEquals(map.get("section1.path"), "/foo/org/abc")
    }

    @Test
    fun testComplexWithDefinition3() {
        val config = """def home = "/foo/org"
                       |def x1 = "foo"
                       |def x2 = "org"
                       |
                       |section1.user = "john.doe"
                       |section1 {
                       |   host = "${'$'}{x1}.${'$'}{x1}.${'$'}{x2}"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        val map = ConfigReader(config).read().toMap()

        Assert.assertEquals(map.size, 4)
        Assert.assertEquals(map.get("section1.user"), "john.doe")
        Assert.assertEquals(map.get("section1.host"), "foo.foo.org")
        Assert.assertEquals(map.get("section1.port"), "8000")
        Assert.assertEquals(map.get("section1.path"), "/foo/org/abc")
    }

    @Test
    fun testComplex_Deep() {
        val config = """a {
                       |  b {
                       |    c {
                       |      d {
                       |        e {
                       |          f {
                       |            g {
                       |              x = "1"
                       |            }
                       |          }
                       |        }
                       |      }
                       |    }
                       |  }
                       |}
                     """.trimMargin()

        val map = ConfigReader(config).read().toMap()

        Assert.assertEquals(map.size, 1)
        Assert.assertEquals(map["a.b.c.d.e.f.g.x"], "1")
    }

    @Test
    fun testComplexWithDefinition_Stream() {
        val config = """def home = "/foo/org"
                       |
                       |user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        val map = ConfigReader.create(config.byteInputStream()).read().toMap()

        Assert.assertEquals(map.size, 4)
        Assert.assertEquals(map.get("user"), "john.doe")
        Assert.assertEquals(map.get("section1.host"), "foo.org")
        Assert.assertEquals(map.get("section1.port"), "8000")
        Assert.assertEquals(map.get("section1.path"), "/foo/org/abc")
    }

    @Test
    fun testComplexWithDefinition_SubConfig_Map() {
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

        val map = ConfigReader(config).read().toMap()
        Assert.assertEquals(map.size, 4)

        val map2 = ConfigReader(config).read().getSubConfig("section1", "section2").toMap()
        Assert.assertEquals(map2.size, 4)
        Assert.assertEquals(map2.get("user"), "john.doe")
        Assert.assertEquals(map2.get("host"), "foo.org")
        Assert.assertEquals(map2.get("port"), "8000")
        Assert.assertEquals(map2.get("path"), "/foo/org/abc")

        val map3 = ConfigReader(config).read().getSubConfig("section1").toMap()
        Assert.assertEquals(map3.size, 1)
        Assert.assertEquals(map3.get("user"), "john.doe")

        val map4 = ConfigReader(config).read().getSubConfig("section2").toMap()
        Assert.assertEquals(map4.size, 3)
        Assert.assertEquals(map4.get("host"), "foo.org")
        Assert.assertEquals(map4.get("port"), "8000")
        Assert.assertEquals(map4.get("path"), "/foo/org/abc")

        val map5 = ConfigReader(config).read().getSubConfig("xxxx").toMap()
        Assert.assertEquals(map5.size, 0)

        val map6 = ConfigReader(config).read().getSubConfig("").toMap()
        Assert.assertEquals(map6.size, 0)

        val map7 = ConfigReader(config).read().getSubConfig().toMap()
        Assert.assertEquals(map7.size, 0)
    }

    @Test
    fun testComplexWithDefinition_SubConfig_Map_MergePrecedence() {
        val config = """def home = "/foo/org"
                       |
                       |section1 {
                       |   port = "1000"
                       |}
                       |section2 {
                       |   port = "2000"
                       |}
                     """.trimMargin()

        val map = ConfigReader(config).read().toMap()
        Assert.assertEquals(map.size, 2)

        val map2 = ConfigReader(config).read().getSubConfig("section1", "section2").toMap()
        Assert.assertEquals(map2.size, 1)
        Assert.assertEquals(map2.get("port"), "2000")

        val map3 = ConfigReader(config).read().getSubConfig("section2", "section1").toMap()
        Assert.assertEquals(map3.size, 1)
        Assert.assertEquals(map3.get("port"), "1000")
    }

    @Test
    fun testComplexWithDefinition_Properties() {
        val config = """def home = "/foo/org"
                       |
                       |user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        val props = ConfigReader(config).read().toProperties()
        Assert.assertEquals(props.size, 4)
        Assert.assertEquals(props.getProperty("user"), "john.doe")
        Assert.assertEquals(props.getProperty("section1.host"), "foo.org")
        Assert.assertEquals(props.getProperty("section1.port"), "8000")
        Assert.assertEquals(props.getProperty("section1.path"), "/foo/org/abc")
    }

    @Test
    fun testComplexWithDefinition_Config() {
        val config = """def home = "/foo/org"
                       |
                       |user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/foo/org/abc")
        Assert.assertEquals(cfg.getOrDefault("user", null), "john.doe")
        Assert.assertEquals(cfg.getOrDefault("user", "arthur.dent"), "john.doe")
        Assert.assertEquals(cfg.getOrDefault("x", null), null)
        Assert.assertEquals(cfg.getOrDefault("x", "arthur.dent"), "arthur.dent")
    }

    @Test
    fun testComplexWithDefinition_User() {
        val config = """def home = "/foo/org"
                       |
                       |user = "john.doe"
                       |section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        println(System.getProperties())
        val cfg = ConfigReader(config, hashMapOf("home" to "/extra/org")).read()
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/extra/org/abc")
    }

    @Test
    fun testComplexWithDefinition_System() {
        val config = """section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "${'$'}{system.user.dir}/abc"
                       |}
                     """.trimMargin()

        val userDir = System.getProperty("user.dir")
        val cfg = ConfigReader(config).read()
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), userDir + "/abc")
    }

    @Test
    fun testComplexWithDefinition_Comments() {
        val config = """# adahdhk ##
                       |def home = "/foo/org"        # adahdhk
                       |
                       |# adahdhk
                       | # adahdhk
                       |user = "john.doe"            # adahdhk
                       |section1 {                   # adahdhk
                       |   host = "foo.org"          # adahdhk
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc" # adahdhk
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config, hashMapOf("home" to "/extra/org")).read()
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/extra/org/abc")
    }

    @Test
    fun testComplexWithTrailingSpaces() {
        val config = """section1 {
                       |   host = "foo.org"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config + "   ").read()
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
    }

    @Test
    fun testComplexWithTrailingComment() {
        val config = """section1 {
                       |   host = "foo.org"
                       |}  #
                     """.trimMargin()

        val cfg = ConfigReader(config).read()
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testComplexWithTrailingChars() {
        val config = """section1 {
                       |   host = "foo.org"
                       |}  a
                     """.trimMargin()

        ConfigReader(config).read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testUnresolvedDefinition() {
        val config = """section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc"
                       |}
                     """.trimMargin()

        ConfigReader(config).read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidConfigItem_MissingEqualsAndValue() {
        ConfigReader("user ").read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidConfigItem_MissingValue() {
        ConfigReader("user = ").read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidConfigItem_MissingEquals() {
        ConfigReader("user john").read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidString_NotQuoted() {
        ConfigReader("user = john").read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidString_InvalidQuoting() {
        ConfigReader("user = 'john").read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidString_lf() {
         ConfigReader("user = 'john\n.doe'").read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidString_cr() {
        ConfigReader("user = 'john\r.doe'").read().toMap()
    }

    @Test
    fun testValidString_tab() {
        ConfigReader("user = 'john\t.doe'").read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidSection_MissingClose() {
        val config = """section1 {
                       |   host = "foo.org"
                       |
                     """.trimMargin()

        ConfigReader(config).read().toMap()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testBadEOF() {
        val config = """section1 {
                       |   host = "foo.org"
                       |} xxxx
                     """.trimMargin()

        ConfigReader(config).read().toMap()
    }

}