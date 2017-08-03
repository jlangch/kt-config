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


enum class TokenType {
    EOF, IDENTIFIER, PATH, EQUALS, LEFT_BRACKET, RIGHT_BRACKET, STRING, ANY
}


class Position(val row: Int = 1, val col: Int = 1) {
    override fun toString(): String = "($row, $col)"
}


class Token(val data: String = "", val type: TokenType, val pos: Position) {
    fun isType(vararg types: TokenType): Boolean = types.any { it == type }

    fun isNotType(vararg types: TokenType): Boolean = types.all { it != type }

    override fun toString(): String = "\"$data\" ($type) at $pos"
}


class Character(val char: Char?, val pos: Position) {
    fun eof() = char == null

    fun isChar(vararg ch: Char): Boolean = ch.any { it == char }

    fun isNotChar(vararg ch: Char): Boolean = ch.all { it != char }

    override fun toString(): String {
        return when (char) {
            null -> "<eof> at $pos"
            '\n' -> "<lf> at $pos"
            '\r' -> "<cr> at $pos"
            '\t' -> "<tab> at $pos"
            else -> "$char at " + pos
        }
    }
}


class StringReader(private val text: String) {
    private val TAB_WIDTH = 4
    private var cursor = 0
    private var textPos = Position()

    fun readNext(): Character = Character(nextChar(), textPos).also { c -> updatePosition(c) }

    private fun nextChar(): Char? = if (cursor < text.length) text[cursor++] else null

    private fun updatePosition(ch: Character): Unit {
        textPos = incPosition(textPos, ch.char)
    }
    private fun incPosition(pos: Position, ch: Char?): Position {
        return when (ch) {
                null -> pos
                '\n' -> Position(pos.row + 1, 1)
                '\r' -> pos
                '\t' -> Position(pos.row, pos.col + TAB_WIDTH)
                else -> Position(pos.row, pos.col + 1)
        }
    }
}
