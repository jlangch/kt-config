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


class LargeConfigTest {

    @Test
    fun testLargeDotConfig() {
        val SIZE = 5000

        val cfg = ConfigReader(createDotConfig(SIZE)).read()

        Assert.assertEquals(cfg.size(), SIZE)
    }

    @Test
    fun testLargeSectionConfig() {
        val SIZE = 5000

        val cfg = ConfigReader(createSectionConfig(SIZE)).read()

        Assert.assertEquals(cfg.size(), SIZE * 3)
    }


    private fun createDotConfig(size: Int): String {
        return StringBuilder()
                    .apply {
                        for(ii in 1..size) {
                            append("""key_$ii = "value_$ii\n"""")
                        }
                    }
                    .toString()
    }

    private fun createSectionConfig(size: Int): String {
        return StringBuilder()
                .apply {
                    for(ii in 1..size) {
                        append("section_$ii {\n")
                        append("""   key_1_$ii = "value_1_$ii\n"""")
                        append("""   key_2_$ii = "value_2_$ii\n"""")
                        append("""   key_3_$ii = "value_3_$ii\n"""")
                        append("}\n")
                    }
                }
                .toString()
    }
}