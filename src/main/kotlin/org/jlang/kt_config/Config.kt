package jlang.kt_config

import java.util.*


interface Config {
    fun isEmpty(): Boolean

    fun hasPath(path: String): Boolean

    fun get(path: String): String

    fun getList(path: String): List<String>

    fun getString(path: String): String

    fun getBoolean(path: String): Boolean

    fun getInt(path: String): Int

    fun getLong(path: String): Long

    fun getFloat(path: String): Float

    fun getDouble(path: String): Double

    fun toMap(): Map<String,String>

    fun toProperties(): Properties

    fun getSubConfig(vararg sections: String): Config

}