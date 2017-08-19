package org.jlang.kt_config.impl


class Lookahead<T>(private val size: Int, private val reader: Iterator<T>) {

    private val buffer = RingBuffer<T>(size)

    // prime the lookahead buffer
    init { repeat(size, { buffer.put(reader.next()) }) }

    operator fun get(index: Int): T = buffer[index]

    fun consume(numTokens: Int = 1): Unit { repeat(numTokens, { shift() }) }

    private fun shift() {
        buffer.take()
        buffer.put(reader.next())
    }
}
