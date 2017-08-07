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

import org.jlang.kt_config.impl.*
import org.testng.Assert
import org.testng.annotations.Test


class CoreTest {

    @Test
    fun testPath() {
        Assert.assertEquals(composePath("", "y"), "y")
        Assert.assertEquals(composePath("x", "y"), "x.y")
        Assert.assertEquals(composePath("x", 100), "x.100")
        Assert.assertEquals(composePath("x", "y", 100), "x.y.100")

        Assert.assertEquals(splitPath("x"), listOf("x"))
        Assert.assertEquals(splitPath("x.y"), listOf("x", "y"))
        Assert.assertEquals(splitPath("x.y.z"), listOf("x", "y", "z"))

        Assert.assertFalse(isListPath("x.y.z"))
        Assert.assertTrue(isListPath("x.size"))
        Assert.assertTrue(isListPath("x.1"))
        Assert.assertTrue(isListPath("x.10"))
    }

}