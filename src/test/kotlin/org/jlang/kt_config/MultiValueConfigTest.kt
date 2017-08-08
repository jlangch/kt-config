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
}