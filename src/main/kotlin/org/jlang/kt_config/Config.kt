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

import java.util.Properties


/**
 * Holds the parsed configuration. A configuration is immutable and holds the
 * configuration as key / value pairs.
 *
 * A configuration:
 * `    common {`
 * `      user = "john.doe"`
 * `    }`
 * `    test {`
 * `      host = "foo.org"`
 * `      port = "8000"`
 * `    }`
 * `    uat {`
 * `      host = "foo.org"`
 * `      port = "9000"`
 * `    }`
 *
 * is parsed to the key / value pairs:
 * `    common.user = "john.doe"`
 * `    test.host = "foo.org"`
 * `    test.port = "8000"`
 * `    uat.host = "foo.org"`
 * `    uat.port = "9000"`
 *
 * a `getSubConfig("common", "uat")` returns a subset of configuration values
 * for the section "common" and "uat":
 * `    common.user = "john.doe"`
 * `    uat.host = "foo.org"`
 * `    uat.port = "9000"`
 */
class Config(val map: MutableMap<String,String> = LinkedHashMap()) {

    fun toMap(): Map<String,String> = LinkedHashMap(map)

    fun toProperties(): Properties = Properties().apply { map.forEach { k,v -> setProperty(k,v) } }

    fun getSubConfig(vararg sections: String): Config {
        if (sections.isEmpty()) return Config()

        return Config().also { subConfig ->
            sections.forEach { path ->
                val subPath = trimPath(path) + '.'
                map.filter{ entry -> entry.key.startsWith(subPath) }
                   .forEach{ k,v -> subConfig.put(k.substring(subPath.length), v) }
            }
        }
    }

    fun get(key: String): String? = map[key]

    fun getOrDefault(key: String, defaultValue: String?): String? = map.getOrDefault(key, defaultValue)

    private fun put(key: String, value: String): Unit { map.put(key, value) }

    private fun trimPath(path: String) = path.trimEnd { it == '.' }
}