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

import java.util.ArrayDeque
import java.util.Deque


class ConfigMapBuilder {
    private val config: MutableMap<String,String> = LinkedHashMap()
    private var stack: Deque<String> = ArrayDeque()


    fun pushPath(path: String): Unit = stack.push(path)

    fun popPath(): String = stack.pop()

    fun put(key: String, value: String): Unit {
        config[concatPath(currPath(), key)] = value
    }

    fun get(): Map<String,String>  = config

    private fun currPath(): String = stack.reversed().joinToString(".")

    private fun concatPath(base: String, path: String): String {
        return if (base.isEmpty()) path else base + "." + path
    }
}
