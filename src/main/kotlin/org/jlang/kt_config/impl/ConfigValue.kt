package org.jlang.kt_config.impl


class ConfigValue(val path: String, val values: List<String>, val singleValue: Boolean) {

    companion object Factory {
        fun single(path: String, value: String) : ConfigValue {
            return ConfigValue(path, listOf(value), true)
        }
        fun multi(path: String, values: List<String>) : ConfigValue {
            return ConfigValue(path, values, false)
        }
    }

    val multiValue = !singleValue

    val value: String = if (values.isEmpty()) "" else values.first()

    fun toMap(): Map<String,String> {
        if (singleValue) {
            return mapOf( path to value )
        }
        else {
            val map = LinkedHashMap<String, String>()
            map.put(composePath(path, "size"), values.size.toString())
            values.withIndex().forEach { map.put(composePath(path, it.index+1), it.value) }
            return map
        }
    }

    fun rebase(path: String) = ConfigValue(path, this.values, this.singleValue)
}