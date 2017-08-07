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
import java.net.URL
import java.util.Properties
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.listOf

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


class ConfigObject(private val map: Map<String,ConfigValue> = LinkedHashMap()) {
    val cfgMap = LinkedHashMap(map)

    companion object Factory {
        fun create(simpleMap: Map<String,String>): ConfigObject {
            // TODO
            return ConfigObject()
        }
    }

    fun toMap(): Map<String,String> {
        val simpleMap = LinkedHashMap<String,String>()
        cfgMap.values.forEach{ simpleMap.putAll(it.toMap()) }
        return simpleMap
    }


    fun toProperties(): Properties {
        val props = Properties()
        toMap().forEach { k,v -> props.setProperty(k,v) }
        return props
    }

    fun getSubConfig(sections: List<String>): ConfigObject {
        if (sections.isEmpty()) return ConfigObject()

        return ConfigObject().also { subConfig ->
            sections.forEach { path ->
                val subPath = path + '.'
                cfgMap.values
                        .filter{ v -> v.path.startsWith(subPath) }
                        .forEach{ v -> subConfig.put(v.rebase(v.path.substring(subPath.length))) }
            }
        }
    }

    fun copy(): ConfigObject = ConfigObject.create(LinkedHashMap(toMap()))

    fun isEmpty(): Boolean = cfgMap.isEmpty()

    fun hasPath(path: String): Boolean = hasValuePath(path) || hasListPath(path)

    fun get(path: String): String {
        if (hasValuePath(path)) {
            return cfgMap.get(path)!!.value
        }
        else {
            throw ConfigException(
                    "The configuration value $path does not exist")
        }
    }

    fun getList(path: String): List<String> {
        if (hasPath(path)) {
            return cfgMap.get(path)!!.values
        }
        else {
            throw ConfigException(
                    "The configuration values $path does not exist")
        }
    }

    fun merge(config: ConfigObject): ConfigObject {
        val mergedConfig = copy()

        return mergedConfig
    }

    override fun toString(): String =
        // TODO
        cfgMap.entries
           .map { entry -> "$entry.key = $entry.value" }
           .joinToString("\n")


    private fun hasValuePath(path: String): Boolean {
        return cfgMap.get(path)?.singleValue ?: false
    }

    private fun hasListPath(path: String): Boolean  {
        return cfgMap.get(path)?.multiValue ?: false
    }

    private fun put(value: ConfigValue): Unit { cfgMap.put(value.path, value) }

}