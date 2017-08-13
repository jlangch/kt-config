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
import kotlin.collections.LinkedHashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.filter
import kotlin.collections.forEach


class ConfigObject(private val map: Map<String,ConfigValue> = LinkedHashMap()) {
    val cfgMap = LinkedHashMap(map)

    fun toMap(): Map<String,String> {
        return cfgMap.values.fold(
                    LinkedHashMap<String,String>(),
                    { map,value -> map.putAll(value.toMap()); map } )
     }

    fun toProperties(): Properties =
        Properties().also { props -> toMap().forEach { k,v -> props.setProperty(k,v) } }


    fun getSubConfig(sections: List<String>): ConfigObject {
        return ConfigObject().also { subConfig ->
            sections.map { subPath -> subPath + '.' }
                    .forEach { subPath ->
                        cfgMap.values
                                .filter{ v -> v.path.startsWith(subPath) }
                                .forEach{ v -> subConfig.put(v.rebase(v.path.removePrefix(subPath))) }
                    }
        }
    }

    fun copy(): ConfigObject = ConfigObject(LinkedHashMap(cfgMap))

    fun size(): Int = cfgMap.size

    fun isEmpty(): Boolean = cfgMap.isEmpty()

    fun hasPath(path: String): Boolean = hasValuePath(path) || hasListPath(path)

    fun get(path: String): String {
        if (hasValuePath(path)) {
            return cfgMap[path]!!.value
        }
        else {
            throw ConfigException(
                    "The configuration value $path does not exist")
        }
    }

    fun getList(path: String): List<String> {
        if (hasPath(path)) {
            return cfgMap[path]!!.values
        }
        else {
            throw ConfigException(
                    "The configuration values $path does not exist")
        }
    }

    fun merge(config: ConfigObject): ConfigObject {
        return copy().apply { putAll(config.cfgMap.values) }
    }

    override fun toString(): String =
            toMap().entries
                   .map { entry -> "${entry.key} = ${entry.value}" }
                   .joinToString("\n")


    private fun hasValuePath(path: String): Boolean {
        return cfgMap[path]?.singleValue ?: false
    }

    private fun hasListPath(path: String): Boolean  {
        return cfgMap[path]?.multiValue ?: false
    }

    private fun put(value: ConfigValue): Unit { cfgMap.put(value.path, value) }

    private fun putAll(values: Collection<ConfigValue>): Unit { values.forEach { put(it) } }

}