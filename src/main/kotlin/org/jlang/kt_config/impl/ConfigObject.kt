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

import org.jlang.kt_config.*
import java.util.Properties
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.isEmpty
import kotlin.collections.listOf


class ConfigObject(val map: MutableMap<String,String> = LinkedHashMap()) {

    fun toMap(): Map<String,String> = LinkedHashMap(map)

    fun toProperties(): Properties = Properties().apply { map.forEach { k,v -> setProperty(k,v) } }

    fun getSubConfig(section: String): ConfigObject = getSubConfig(listOf(section))

    fun getSubConfig(sections: List<String>): ConfigObject {
        if (sections.isEmpty()) return ConfigObject()

        return ConfigObject().also { subConfig ->
            sections.forEach { path ->
                val subPath = trimPath(path) + '.'
                map.filter{ entry -> entry.key.startsWith(subPath) }
                   .forEach{ k,v -> subConfig.put(k.substring(subPath.length), v) }
            }
        }
    }

    fun isEmpty(): Boolean = map.isEmpty()

    fun hasPath(path: String): Boolean = hasValuePath(path) || hasListPath(path)

    fun get(path: String): String {
        if (hasValuePath(path)) {
            return map[path]!!
        }
        else {
            if (hasListPath(path)) {
                throw ConfigException(
                        "The configuration value $path does not exist." +
                                "But there is a list value at the requested path")
            }
            else {
                throw ConfigException(
                        "The configuration value $path does not exist")
            }
        }
    }

    fun getList(path: String): List<String> {
        if (hasValuePath(path)) {
            return listOf<String>(map[path]!!)
        }
        else if (hasListPath(path)) {
            val size = map[composePath(path, "size")]!!.toInt()

            return ArrayList<String>().apply {
                for(ii in 1..size) { add(map[composePath(path, ii)]!!) }
            }
        }
        else {
            throw ConfigException(
                    "The configuration value $path does not exist")
        }
    }

    private fun hasValuePath(path: String): Boolean = map.containsKey(path)

    private fun hasListPath(path: String): Boolean = map.containsKey(composePath(path, "size"))

    private fun put(path: String, value: String): Unit { map.put(path, value) }

    private fun trimPath(path: String) = path.trimEnd { it == '.' }
}