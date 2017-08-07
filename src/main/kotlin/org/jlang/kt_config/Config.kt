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

import java.util.*


/**
 * Holds the configuration. A configuration is immutable and holds the
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
 * is parsed to the key/value pairs:
 * `    common.user = "john.doe"`
 * `    test.host = "foo.org"`
 * `    test.port = "8000"`
 * `    uat.host = "foo.org"`
 * `    uat.port = "9000"`
 *
 * a `getSubConfig("common", "uat")` returns a subset of configuration values
 * for the section "common" and "uat":
 * `    user = "john.doe"`
 * `    host = "foo.org"`
 * `    port = "9000"`
 */
interface Config {

    fun isEmpty(): Boolean

    fun hasPath(path: String): Boolean


    fun get(path: String): String

    fun getList(path: String): List<String>


    fun getString(path: String): String

    fun getStringList(path: String): List<String>

    fun getBoolean(path: String): Boolean

    fun getBooleanList(path: String): List<Boolean>

    fun getInt(path: String): Int

    fun getIntList(path: String): List<Int>

    fun getLong(path: String): Long

    fun getLongList(path: String): List<Long>

    fun getFloat(path: String): Float

    fun getFloatList(path: String): List<Float>

    fun getDouble(path: String): Double

    fun getDoubleList(path: String): List<Double>


    fun toMap(): Map<String,String>

    fun toProperties(): Properties

    fun getSubConfig(vararg sections: String): Config

    fun merge(config: Config): Config

    fun empty(): Config
}