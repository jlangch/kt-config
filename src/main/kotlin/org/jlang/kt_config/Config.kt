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