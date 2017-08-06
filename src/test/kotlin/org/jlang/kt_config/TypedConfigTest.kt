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


class TypedConfigTest {

    @Test
    fun testString() {
        val config = """x {
                       |   c_string = "foo.org"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

         Assert.assertEquals(cfg.getString("x.c_string"), "foo.org")
    }

    @Test
    fun testBoolean() {
        val config = """x {
                       |   c_bool_true = "true"
                       |   c_bool_false = "false"
                       |   c_bool_yes = "yes"
                       |   c_bool_no = "no"
                       |   c_bool_on = "on"
                       |   c_bool_off = "off"
                       |   c_bool_enabled = "enabled"
                       |   c_bool_disabled = "disabled"
                       |   c_bool_active = "active"
                       |   c_bool_inactive = "inactive"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertTrue(cfg.getBoolean("x.c_bool_true"))
        Assert.assertFalse(cfg.getBoolean("x.c_bool_false"))
        Assert.assertTrue(cfg.getBoolean("x.c_bool_yes"))
        Assert.assertFalse(cfg.getBoolean("x.c_bool_no"))
        Assert.assertTrue(cfg.getBoolean("x.c_bool_on"))
        Assert.assertFalse(cfg.getBoolean("x.c_bool_off"))
        Assert.assertTrue(cfg.getBoolean("x.c_bool_enabled"))
        Assert.assertFalse(cfg.getBoolean("x.c_bool_disabled"))
        Assert.assertTrue(cfg.getBoolean("x.c_bool_active"))
        Assert.assertFalse(cfg.getBoolean("x.c_bool_inactive"))
    }

    @Test
    fun testInt() {
        val config = """x {
                       |   c_int_1 = "100"
                       |   c_int_2 = "-100"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.getInt("x.c_int_1"), 100)
        Assert.assertEquals(cfg.getInt("x.c_int_2"), -100)
    }

    @Test
    fun testLong() {
        val config = """x {
                       |   c_long_1 = "100"
                       |   c_long_2 = "-100"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.getLong("x.c_long_1"), 100L)
        Assert.assertEquals(cfg.getLong("x.c_long_2"), -100L)
    }

    @Test
    fun testFloat() {
        val config = """x {
                       |   c_float_1 = "100.0"
                       |   c_float_2 = "-100.0"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.getFloat("x.c_float_1"), 100.0F, 0.00001F)
        Assert.assertEquals(cfg.getFloat("x.c_float_2"), -100.0F, 0.00001F)
    }

    @Test
    fun testDouble() {
        val config = """x {
                       |   c_double_1 = "100.0"
                       |   c_double_2 = "-100.0"
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.getDouble("x.c_double_1"), 100.0, 0.00001)
        Assert.assertEquals(cfg.getDouble("x.c_double_2"), -100.0, 0.00001)
    }

}