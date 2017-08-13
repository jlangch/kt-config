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

import org.jlang.kt_config.Config
import org.jlang.kt_config.ConfigException
import java.math.BigDecimal
import java.util.*


val BOOL_PAIRS = listOf(
                    Pair("true", "false"),
                    Pair("yes", "no"),
                    Pair("on", "off"),
                    Pair("enabled", "disabled"),
                    Pair("active", "inactive"))


class ConfigImpl(private val cfgObj: ConfigObject) : Config {

    val boolValues = HashMap<String,Boolean>()

    init {
        BOOL_PAIRS.forEach {
            boolValues.put(it.first, true)
            boolValues.put(it.second, false)
        }
    }


    override fun size(): Int = cfgObj.size()

    override fun isEmpty(): Boolean = cfgObj.isEmpty()

    override fun hasPath(path: String): Boolean = cfgObj.hasPath(path)

    override fun get(path: String): String = cfgObj.get(path)

    override fun getList(path: String): List<String> = cfgObj.getList(path)

    override fun getString(path: String): String = cfgObj.get(path)

    override fun getStringList(path: String): List<String> = getList(path)

    override fun getBoolean(path: String): Boolean = toBoolean(cfgObj.get(path))

    override fun getBooleanList(path: String): List<Boolean> = getList(path).map { toBoolean(it) }

    override fun getInt(path: String): Int = getString(path).toInt()

    override fun getIntList(path: String): List<Int> = getList(path).map { it.toInt() }

    override fun getLong(path: String): Long = getString(path).toLong()

    override fun getLongList(path: String): List<Long> = getList(path).map { it.toLong() }

    override fun getFloat(path: String): Float = getString(path).toFloat()

    override fun getFloatList(path: String): List<Float> = getList(path).map { it.toFloat() }

    override fun getDouble(path: String): Double = getString(path).toDouble()

    override fun getDoubleList(path: String): List<Double> = getList(path).map { it.toDouble() }

    override fun getDecimal(path: String): BigDecimal = BigDecimal(getString(path))

    override fun getDecimalList(path: String): List<BigDecimal> = getList(path).map { BigDecimal(it) }

    override fun toMap(): Map<String,String> = cfgObj.toMap()

    override fun toProperties(): Properties = cfgObj.toProperties()

    override fun getSubConfig(vararg sections: String): Config =
            ConfigImpl(cfgObj.getSubConfig(sections.toList()))

    override fun merge(config: Config): Config {
        if (config is ConfigImpl) {
            return ConfigImpl(cfgObj.merge(config.cfgObj))
        }
        else {
            throw ConfigException("Invalid config type")
        }
    }

    override fun copy(): Config = ConfigImpl(cfgObj.copy())

    override fun toString(): String = cfgObj.toString()

    private fun toBoolean(value: String): Boolean =
        boolValues[value.toLowerCase()]
                ?: throw ConfigException("Invalid boolean value '$value'. Use one of $BOOL_PAIRS")

}