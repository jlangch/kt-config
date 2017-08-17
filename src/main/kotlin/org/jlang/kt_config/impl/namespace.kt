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
    EOF,        // end-of-file
    IDENTIFIER, // abc
    PATH,       // abc.def.ghi
    STRING,     // "..." or '...'
    ANY,        // anything
    EQUALS,     // '='
    LBRACE,     // '{'
    RBRACE,     // '}'
    LBRACK,     // '['
    RBRACK,     // ']'
    COMMA       // ','
}


class Position(val row: Int = 1, val col: Int = 1) {
    override fun toString(): String = "($row, $col)"
}


class Token(val type: TokenType, val data: String, val pos: Position) {
    constructor(type: TokenType, ch: Character)
            : this(type, ch.char.toString(), ch.pos)

    companion object {
        fun eof(pos: Position): Token = Token(TokenType.EOF, "", pos)
    }

    fun isType(vararg types: TokenType): Boolean = types.any { it == type }

    fun isNotType(vararg types: TokenType): Boolean = types.all { it != type }

    override fun toString(): String = "\"$data\" ($type) at $pos"
}


data class Character(val char: Char?, val pos: Position) {
    fun eof() = char == null

    fun isEscapeChar(): Boolean = char == '\\'

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


class StringReader(private val text: String): Iterator<Character> {
    private val TAB_WIDTH = 4
    private var cursor = 0
    private var textPos = Position()

    override fun hasNext(): Boolean = true

    override fun next(): Character = Character(nextChar(), textPos).also { c -> updatePosition(c) }

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

fun composePath(base: String, key: String, index: Int): String {
    return composePath(base, composePath(key, index))
}

fun composePath(key: String, index: Int): String {
    return composePath(key, index.toString())
}

fun composePath(base: String, path: String): String {
    return if (base.isEmpty()) path else base + "." + path
}

fun splitPath(path: String): List<String> = path.split('.')

fun isListPath(path: String): Boolean {
    val numberRegex = Regex("^[0-9]+${'$'}")
    val tail = splitPath(path).last()
    return tail == "size" || numberRegex.matches(tail)
}

fun getPrefixedEnvironmentVariables(prefix: String): Map<String,String> =
        System.getenv()
                .filterValues { v ->  v != null }
                .mapKeys { entry -> prefix + "." + entry.key }

fun getPrefixedSystemProperties(prefix: String): Map<String,String> =
        System.getProperties()
                .filterValues { v ->  v != null }
                .mapValues { entry -> entry.value.toString() }
                .mapKeys { entry -> prefix + "." + entry.key }
