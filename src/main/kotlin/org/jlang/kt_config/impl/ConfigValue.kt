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

package org.jlang.kt_config.impl


class ConfigValue private constructor(
        val path: String,
        val values: List<String>,
        val singleValue: Boolean
) {

    companion object Factory {
        fun single(path: String, value: String) : ConfigValue {
            return ConfigValue(path, listOf(value), true)
        }
        fun multi(path: String, values: List<String>) : ConfigValue {
            return ConfigValue(path, values, false)
        }
    }

    val multiValue = !singleValue

    val value: String = if (values.isEmpty()) "" else values.first()

    fun toMap(): Map<String,String> {
        if (singleValue) {
            return mapOf( path to value )
        }
        else {
            return LinkedHashMap<String, String>().also { map ->
                map.put(composePath(path, "size"), values.size.toString())
                values.withIndex().forEach { map.put(composePath(path, it.index+1), it.value) }
            }
        }
    }

    fun rebase(path: String) = ConfigValue(path, this.values, this.singleValue)
}