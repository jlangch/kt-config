package org.jlang.kt_config.impl

import org.jlang.kt_config.Config
import org.jlang.kt_config.ConfigException
import org.jlang.kt_config.impl.ConfigObject
import java.util.*


class ConfigImpl(private val cfgObj: ConfigObject) : Config {

    override fun isEmpty(): Boolean = cfgObj.isEmpty()

    override fun hasPath(path: String): Boolean = cfgObj.hasPath(path)

    override fun get(path: String): String = cfgObj.get(path)

    override fun getList(path: String): List<String> = cfgObj.getList(path)

    override fun getString(path: String): String = cfgObj.get(path)

    override fun getBoolean(path: String): Boolean {
        val value = cfgObj.get(path)
        return when {
            value.equals("true", true) -> true
            value.equals("false", true) -> false
            value.equals("yes", true) -> true
            value.equals("no", true) -> false
            value.equals("on", true) -> true
            value.equals("off", true) -> false
            value.equals("enabled", true) -> true
            value.equals("disabled", true) -> false
            value.equals("active", true) -> true
            value.equals("inactive", true) -> false
            else -> throw ConfigException(
                    "Invalid boolean property ${value}. Use one of (true|false)," +
                            "(yes|no), (on|off), (enabled|disabled), (active|inactive)")
        }
    }

    override fun getInt(path: String): Int = getString(path).toInt()

    override fun getLong(path: String): Long = getString(path).toLong()

    override fun getFloat(path: String): Float = getString(path).toFloat()

    override fun getDouble(path: String): Double = getString(path).toDouble()

    override fun toMap(): Map<String,String> = cfgObj.toMap()

    override fun toProperties(): Properties = cfgObj.toProperties()

    override fun getSubConfig(vararg sections: String): Config =
            ConfigImpl(cfgObj.getSubConfig(sections.toList()))

}