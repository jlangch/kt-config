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


class MultiValueConfigTest {

    @Test
    fun testString() {
        val config = """x {
                       |   c_arr_1 = [ ]
                       |   c_arr_2 = [ "foo1" ]
                       |   c_arr_3 = [ "foo1", "foo2" ]
                       |   c_arr_4 = [ "foo1", "foo2", "foo3" ]
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.getList("x.c_arr_1"), ArrayList<String>())
        Assert.assertEquals(cfg.getList("x.c_arr_2"), listOf("foo1"))
        Assert.assertEquals(cfg.getList("x.c_arr_3"), listOf("foo1", "foo2"))
        Assert.assertEquals(cfg.getList("x.c_arr_4"), listOf("foo1", "foo2", "foo3"))
    }

    @Test
    fun testInt() {
        val config = """x {
                       |   c_arr_1 = [ ]
                       |   c_arr_2 = [ "10" ]
                       |   c_arr_3 = [ "10", "20" ]
                       |   c_arr_4 = [ "10", "20", "30" ]
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.getIntList("x.c_arr_1"), ArrayList<Int>())
        Assert.assertEquals(cfg.getIntList("x.c_arr_2"), listOf(10))
        Assert.assertEquals(cfg.getIntList("x.c_arr_3"), listOf(10, 20))
        Assert.assertEquals(cfg.getIntList("x.c_arr_4"), listOf(10, 20, 30))
    }

    @Test
    fun testLong() {
        val config = """x {
                       |   c_arr_1 = [ ]
                       |   c_arr_2 = [ "10" ]
                       |   c_arr_3 = [ "10", "20" ]
                       |   c_arr_4 = [ "10", "20", "30" ]
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        Assert.assertEquals(cfg.getLongList("x.c_arr_1"), ArrayList<Int>())
        Assert.assertEquals(cfg.getLongList("x.c_arr_2"), listOf(10L))
        Assert.assertEquals(cfg.getLongList("x.c_arr_3"), listOf(10L, 20L))
        Assert.assertEquals(cfg.getLongList("x.c_arr_4"), listOf(10L, 20L, 30L))
    }

    @Test
    fun testFloat() {
        val config = """x {
                       |   c_arr_1 = [ ]
                       |   c_arr_2 = [ "10.01" ]
                       |   c_arr_3 = [ "10.01", "20.02" ]
                       |   c_arr_4 = [ "10.01", "20.02", "30.03" ]
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        val fl1 = cfg.getFloatList("x.c_arr_1")
        Assert.assertTrue(fl1.isEmpty())

        val fl2 = cfg.getFloatList("x.c_arr_2")
        Assert.assertEquals(fl2[0], 10.01F, 0.0001F)

        val fl3 = cfg.getFloatList("x.c_arr_3")
        Assert.assertEquals(fl3[0], 10.01F, 0.0001F)
        Assert.assertEquals(fl3[1], 20.02F, 0.0001F)

        val fl4 = cfg.getFloatList("x.c_arr_4")
        Assert.assertEquals(fl4[0], 10.01F, 0.0001F)
        Assert.assertEquals(fl4[1], 20.02F, 0.0001F)
        Assert.assertEquals(fl4[2], 30.03F, 0.0001F)
    }

    @Test
    fun testDouble() {
        val config = """x {
                       |   c_arr_1 = [ ]
                       |   c_arr_2 = [ "10.01" ]
                       |   c_arr_3 = [ "10.01", "20.02" ]
                       |   c_arr_4 = [ "10.01", "20.02", "30.03" ]
                       |}
                     """.trimMargin()

        val cfg = ConfigReader(config).read()

        val fl1 = cfg.getDoubleList("x.c_arr_1")
        Assert.assertTrue(fl1.isEmpty())

        val fl2 = cfg.getDoubleList("x.c_arr_2")
        Assert.assertEquals(fl2[0], 10.01, 0.0001)

        val fl3 = cfg.getDoubleList("x.c_arr_3")
        Assert.assertEquals(fl3[0], 10.01, 0.0001)
        Assert.assertEquals(fl3[1], 20.02, 0.0001)

        val fl4 = cfg.getDoubleList("x.c_arr_4")
        Assert.assertEquals(fl4[0], 10.01, 0.0001)
        Assert.assertEquals(fl4[1], 20.02, 0.0001)
        Assert.assertEquals(fl4[2], 30.03, 0.0001)
    }
}