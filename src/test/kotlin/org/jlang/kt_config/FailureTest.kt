package org.jlang.kt_config

import org.testng.annotations.Test


class FailureTest {

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidLineBreakInSingleQuotedString() {
        ConfigReader("x = ' \n '").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testInvalidLineBreakInDoubleQuotedString() {
        ConfigReader("x = \" \n \"").read()
    }

    @Test(expectedExceptions = arrayOf(ConfigException::class))
    fun testSectionWithInvalidTrailingChars() {
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