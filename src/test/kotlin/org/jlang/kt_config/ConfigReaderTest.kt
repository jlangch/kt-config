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


class ConfigReaderTest {

    @Test
    fun testEmptyConfig() {
        val map1 = ConfigReader("").read()
        Assert.assertEquals(map1.size(), 0)

        val cfg2 = ConfigReader(" ").read()
        Assert.assertEquals(cfg2.size(), 0)

        val cfg3 = ConfigReader("   ").read()
        Assert.assertEquals(cfg3.size(), 0)

        val cfg4 = ConfigReader("\n").read()
        Assert.assertEquals(cfg4.size(), 0)

        val cfg5 = ConfigReader("\n\n\n").read()
        Assert.assertEquals(cfg5.size(), 0)
    }

    @Test
    fun testSimpleConfig_Path() {
        val map1 = ConfigReader("user = 'john.doe'").read()
 
        Assert.assertTrue(map1.hasPath("user"))
        Assert.assertFalse(map1.hasPath("x"))
    }

    @Test
    fun testSimpleConfig_1item_DoubleQuotedValues() {
        val map1 = ConfigReader("""user = "john.doe"""").read()
        Assert.assertEquals(map1.size(), 1)
        Assert.assertEquals(map1.get("user"), "john.doe")

        val cfg2 = ConfigReader("""  user    =    "john.doe"    """).read()
        Assert.assertEquals(cfg2.size(), 1)
        Assert.assertEquals(cfg2.get("user"), "john.doe")

        val cfg3 = ConfigReader("\n\n\nuser = \"john.doe\"\n\n\n").read()
        Assert.assertEquals(cfg3.size(), 1)
        Assert.assertEquals(cfg3.get("user"), "john.doe")

        val cfg4 = ConfigReader("""x.user = "john.doe"""").read()
        Assert.assertEquals(cfg4.size(), 1)
        Assert.assertEquals(cfg4.get("x.user"), "john.doe")

        val cfg5 = ConfigReader("""x.y.user = "john.doe"""").read()
        Assert.assertEquals(cfg5.size(), 1)
        Assert.assertEquals(cfg5.get("x.y.user"), "john.doe")

        val cfg6 = ConfigReader("""user = "j'o'h'n.d'o'e"""").read()
        Assert.assertEquals(cfg6.size(), 1)
        Assert.assertEquals(cfg6.get("user"), "j'o'h'n.d'o'e")
    }


    @Test
    fun testSimpleConfig_1item_SingleQuotedValues() {
        val map1 = ConfigReader("user = 'john.doe'").read()
        Assert.assertEquals(map1.size(), 1)
        Assert.assertEquals(map1.get("user"), "john.doe")

        val cfg2 = ConfigReader("  user    =    'john.doe'    ").read()
        Assert.assertEquals(cfg2.size(), 1)
        Assert.assertEquals(cfg2.get("user"), "john.doe")

        val cfg3 = ConfigReader("\n\n\nuser = 'john.doe'\n\n\n").read()
        Assert.assertEquals(cfg3.size(), 1)
        Assert.assertEquals(cfg3.get("user"), "john.doe")

        val cfg4 = ConfigReader("x.user = 'john.doe'").read()
        Assert.assertEquals(cfg4.size(), 1)
        Assert.assertEquals(cfg4.get("x.user"), "john.doe")

        val cfg5 = ConfigReader("x.y.user = 'john.doe'").read()
        Assert.assertEquals(cfg5.size(), 1)
        Assert.assertEquals(cfg5.get("x.y.user"), "john.doe")

        val cfg6 = ConfigReader("""user = 'j"o"h"n.d"o"e'""").read()
        Assert.assertEquals(cfg6.size(), 1)
        Assert.assertEquals(cfg6.get("user"), "j\"o\"h\"n.d\"o\"e")
    }

    @Test
    fun testEscapedStrings() {
        Assert.assertEquals(ConfigReader("""x = ' \' '""").read().get("x"), " ' ")
        Assert.assertEquals(ConfigReader("""x = ' \" '""").read().get("x"), " \" ")
        Assert.assertEquals(ConfigReader("""x = ' \n '""").read().get("x"), " \n ")
        Assert.assertEquals(ConfigReader("""x = ' \t '""").read().get("x"), " \t ")

        Assert.assertEquals(ConfigReader("""x = " \' """").read().get("x"), " ' ")
        Assert.assertEquals(ConfigReader("""x = " \" """").read().get("x"), " \" ")
        Assert.assertEquals(ConfigReader("""x = " \n """").read().get("x"), " \n ")
        Assert.assertEquals(ConfigReader("""x = " \t """").read().get("x"), " \t ")
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidLineBreakInString_1() {
        ConfigReader("x = ' \n '").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidLineBreakInString_2() {
        ConfigReader("x = \" \n \"").read()
    }

    @Test
    fun testSimpleConfig_2item() {
        val map1 = ConfigReader("host = 'foo.org' \n port = '8000'").read()
        Assert.assertEquals(map1.size(), 2)
        Assert.assertEquals(map1.get("host"), "foo.org")
        Assert.assertEquals(map1.get("port"), "8000")

        val cfg2 = ConfigReader("x.host = 'foo.org' \n x.y.port = '8000'").read()
        Assert.assertEquals(cfg2.size(), 2)
        Assert.assertEquals(cfg2.get("x.host"), "foo.org")
        Assert.assertEquals(cfg2.get("x.y.port"), "8000")

        val cfg3 = ConfigReader("x.y.host = 'foo.org' \n\n x.port = '8000'").read()
        Assert.assertEquals(cfg3.size(), 2)
        Assert.assertEquals(cfg3.get("x.y.host"), "foo.org")
        Assert.assertEquals(cfg3.get("x.port"), "8000")
    }

    @Test
    fun testSimpleConfig_DuplicateItem() {
        val map1 = ConfigReader("user = 'john.doe' \n user = 'arthur.dent'").read()
        Assert.assertEquals(map1.size(), 1)
        Assert.assertEquals(map1.get("user"), "arthur.dent")
    }

    @Test
    fun testEmptySection() {
        val map1 = ConfigReader("section1 { }").read()
        Assert.assertEquals(map1.size(), 0)

        val cfg2 = ConfigReader("section1 { } \n section2 { }").read()
        Assert.assertEquals(cfg2.size(), 0)

        val cfg3 = ConfigReader("section1 { } \n section2 { } \n section1 { }").read()
        Assert.assertEquals(cfg3.size(), 0)
    }

    @Test
    fun testSimpleSection() {
        val config = """section1 {
                       |   host = "foo.org"
                       |   port = "8000"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.size(), 2)
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
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

        val cfg = ConfigReader(config).read()
        Assert.assertEquals(cfg.size(), 4)
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/foo/org/abc")
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

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.size(), 4)
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/foo/org/abc")
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

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.size(), 4)
        Assert.assertEquals(cfg.get("section1.user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/foo/org/abc")
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

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.size(), 1)
        Assert.assertEquals(cfg.get("a.b.c.d.e.f.g.x"), "1")
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

        val cfg = ConfigReader.create(config.byteInputStream()).read()

        Assert.assertEquals(cfg.size(), 4)
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/foo/org/abc")
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

        val cfg = ConfigReader(config).read()
        Assert.assertEquals(cfg.size(), 2)

        val cfg2 = ConfigReader(config).read().getSubConfig("section1", "section2")
        Assert.assertEquals(cfg2.size(), 1)
        Assert.assertEquals(cfg2.get("port"), "2000")

        val cfg3 = ConfigReader(config).read().getSubConfig("section2", "section1")
        Assert.assertEquals(cfg3.size(), 1)
        Assert.assertEquals(cfg3.get("port"), "1000")
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
    }

    @Test
    fun testComplexWithDefinition_User() {
        val config = """user = "john.doe"
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
                       |#
                       |# adahdhk
                       | # adahdhk
                       |user = "john.doe"            # adahdhk
                       |section1 {                   # adahdhk
                       |   host = "foo.org"          # adahdhk
                       |   port = "8000"
                       |   path = "${'$'}{home}/abc" # adahdhk
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()
        Assert.assertEquals(cfg.get("user"), "john.doe")
        Assert.assertEquals(cfg.get("section1.host"), "foo.org")
        Assert.assertEquals(cfg.get("section1.port"), "8000")
        Assert.assertEquals(cfg.get("section1.path"), "/foo/org/abc")
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

        ConfigReader(config).read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidConfigItem_MissingEqualsAndValue() {
        ConfigReader("user ").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidConfigItem_MissingValue() {
        ConfigReader("user = ").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidConfigItem_MissingEquals() {
        ConfigReader("user john").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidString_NotQuoted() {
        ConfigReader("user = john").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidString_InvalidQuoting() {
        ConfigReader("user = 'john").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidString_lf() {
         ConfigReader("user = 'john\n.doe'").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidString_cr() {
        ConfigReader("user = 'john\r.doe'").read()
    }

    @Test
    fun testValidString_tab() {
        ConfigReader("user = 'john\t.doe'").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidSection_MissingClose() {
        val config = """section1 {
                       |   host = "foo.org"
                       |
                     """.trimMargin()

        ConfigReader(config).read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testBadEOF() {
        val config = """section1 {
                       |   host = "foo.org"
                       |} xxxx
                     """.trimMargin()

        ConfigReader(config).read()
    }

}