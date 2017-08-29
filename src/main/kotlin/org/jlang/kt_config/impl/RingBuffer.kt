package org.jlang.kt_config.impl


class RingBuffer<T>(private val capacity: Int) {
    private val buffer = ArrayList<T?>()
    private var writePos: Int = 0
    private var available: Int = 0;

    init {
        if (capacity < 1) {
            throw RuntimeException("A ring buffer capacity must be greater than 0")
        }
        reset()
    }

    fun reset(): Unit {
        writePos = 0
        available = 0
        buffer.clear()
        repeat(capacity, { buffer.add(null) })
    }

    fun empty(): Boolean = available == 0

    fun full(): Boolean = available == capacity

    operator fun get(offset: Int): T = peek(offset)

    fun peek(offset: Int = 0): T = buffer[readSlot(offset)]!!

    fun take(): T  = buffer[readSlot(0)]!!.also { available-- }

    fun add(element: T): Unit {
        if (full()) throw RuntimeException("The ring buffer is full")

        buffer[writePos] = element
        available++
        writePos = rollPosition(writePos + 1)
    }

    private fun readSlot(offset: Int): Int {
        if (empty()) {
            throw RuntimeException("The ring buffer is empty")
        }
        if (offset < 0 || offset >= available) {
            throw RuntimeException("Ring buffer offset $offset out of bounds")
        }

        return rollPosition(rollPosition(writePos - available) + offset)
    }

    private fun rollPosition(pos: Int): Int {
        return when {
            pos >= capacity -> pos - capacity
            pos < 0 -> pos + capacity
            else -> pos
        }
    }
}
