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


class ConfigBuilder {
    private val config = LinkedHashMap<String,ConfigValue>()
    private var pathStack: Deque<String> = ArrayDeque()


    fun pushPath(path: String): Unit = pathStack.push(path)

    fun popPath(): String = pathStack.pop()

    fun add(cfg: ConfigValue): Unit {
        val c = cfg.rebase(composePath(currPath(), cfg.path))
        config.put(c.path, c)
    }

    fun get(): Map<String,ConfigValue> = config

    private fun currPath(): String = pathStack.reversed().joinToString(".")
}
