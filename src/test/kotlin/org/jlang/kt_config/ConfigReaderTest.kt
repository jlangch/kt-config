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
        val cfg1 = ConfigReader("").read()
        Assert.assertTrue(cfg1.isEmpty())
        Assert.assertEquals(cfg1.size(), 0)

        val cfg2 = ConfigReader(" ").read()
        Assert.assertTrue(cfg2.isEmpty())
        Assert.assertEquals(cfg2.size(), 0)

        val cfg3 = ConfigReader("   ").read()
        Assert.assertTrue(cfg3.isEmpty())
        Assert.assertEquals(cfg3.size(), 0)

        val cfg4 = ConfigReader("\n").read()
        Assert.assertTrue(cfg4.isEmpty())
        Assert.assertEquals(cfg4.size(), 0)

        val cfg5 = ConfigReader("\n\n\n").read()
        Assert.assertTrue(cfg5.isEmpty())
        Assert.assertEquals(cfg5.size(), 0)
    }

    @Test
    fun testSimpleConfig_Path() {
        val cfg1 = ConfigReader("user = 'john.doe'").read()

        Assert.assertEquals(cfg1.size(), 1)
        Assert.assertTrue(cfg1.hasPath("user"))
        Assert.assertFalse(cfg1.hasPath("x"))
    }

    @Test
    fun testSimpleConfig_1item_DoubleQuotedValues() {
        val cfg1 = ConfigReader("""user = "john.doe"""").read()
        Assert.assertEquals(cfg1.size(), 1)
        Assert.assertEquals(cfg1.get("user"), "john.doe")

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
        val cfg1 = ConfigReader("user = 'john.doe'").read()
        Assert.assertEquals(cfg1.size(), 1)
        Assert.assertEquals(cfg1.get("user"), "john.doe")

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

    @Test
    fun testSimpleConfig_2item() {
        val cfg1 = ConfigReader("host = 'foo.org' \n port = '8000'").read()
        Assert.assertEquals(cfg1.size(), 2)
        Assert.assertEquals(cfg1.get("host"), "foo.org")
        Assert.assertEquals(cfg1.get("port"), "8000")

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
        val cfg1 = ConfigReader("user = 'john.doe' \n user = 'arthur.dent'").read()
        Assert.assertEquals(cfg1.size(), 1)
        Assert.assertEquals(cfg1.get("user"), "arthur.dent")
    }

    @Test
    fun testEmptySection() {
        val cfg1 = ConfigReader("section1 { }").read()
        Assert.assertEquals(cfg1.size(), 0)

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
    fun testComplex_Nested() {
        val config = """a {
                       |   a1 = "a 1"
                       |   a2 = "a 2"
                       |
                       |   b {
                       |     b1 = "b 1"
                       |     b2 = "b 2"
                       |     c.c1 = "c 1"
                       |   }
                       |
                       |   b.b3 = "b 3"
                       |}
                       |
                       |a.b.b4 = "b 4"
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.size(), 7)
        Assert.assertEquals(cfg.get("a.a1"), "a 1")
        Assert.assertEquals(cfg.get("a.a2"), "a 2")
        Assert.assertEquals(cfg.get("a.b.b1"), "b 1")
        Assert.assertEquals(cfg.get("a.b.b2"), "b 2")
        Assert.assertEquals(cfg.get("a.b.b3"), "b 3")
        Assert.assertEquals(cfg.get("a.b.b4"), "b 4")
        Assert.assertEquals(cfg.get("a.b.c.c1"), "c 1")
    }

    @Test
    fun testComplex_DeepNested() {
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

}