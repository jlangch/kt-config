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

import org.jlang.kt_config.ConfigException


class Lexer(private val reader: StringReader) {
    private var lookaheadChar: Character = reader.readNext()

    fun nextToken(): Token {
        while (!lookaheadChar.eof()) {
            val pos = lookaheadChar.pos
            when {
                lookaheadChar.isChar('{') -> {
                    consume()
                    return Token("{", TokenType.LEFT_BRACKET, pos)
                }
                lookaheadChar.isChar('}') -> {
                    consume()
                    return Token("}", TokenType.RIGHT_BRACKET, pos)
                }
                lookaheadChar.isChar('=') -> {
                    consume()
                    return Token("=", TokenType.EQUALS, pos)
                }
                lookaheadChar.isChar('"') -> return readStringToken('"')
                lookaheadChar.isChar('\'') -> return readStringToken('\'')
                isCommentChar(lookaheadChar.char) -> consumeCommentToEOL()
                isWhitespaceChar(lookaheadChar.char) -> consumeWhitespaces()
                isIdentifierStartChar(lookaheadChar.char) -> return readIdentifierOrPathToken()
                else -> return readAnyTokenToEOL() // let the parser decide about the error
            }
        }

        return Token("", TokenType.EOF, lookaheadChar.pos)
    }

    private fun consume(): Character = reader.readNext().also { lookaheadChar = it }

    private fun consumeWhitespaces(): Unit {
        while (isWhitespaceChar(lookaheadChar.char)) consume()
    }

    private fun consumeCommentToEOL(): Unit {
        consume()
        while (lookaheadChar.isNotChar('\n', '\r') && !lookaheadChar.eof()) consume()
    }

    private fun readStringToken(quote: Char): Token {
        val sb = StringBuilder()
        val startPos = lookaheadChar.pos

        consume() // leading double quote

        while (!lookaheadChar.eof() && lookaheadChar.isNotChar(quote)) {
            if (lookaheadChar.isChar('\n', '\r')) {
                throw ConfigException(
                        "Invalid string character a position $lookaheadChar.pos "
                                + "A string must contain characters like '\\n' and '\\r'")
            }
            sb.append(lookaheadChar.char)
            consume()
        }

        if (lookaheadChar.eof()) {
            throw ConfigException("Unexpected EOF within a string at position $startPos")
        } else {
            consume() // trailing double quote
            return Token(sb.toString(), TokenType.STRING, startPos)
        }
    }

    private fun readIdentifierOrPathToken(): Token {
        val sb = StringBuilder()
        val startPos = lookaheadChar.pos

        val idToken: Token = readIdentifier()
        if (isDotChar(lookaheadChar.char)) {
            sb.append(idToken.data)
            sb.append('.')
            consume()
            while(isIdentifierStartChar(lookaheadChar.char)) {
                val idToken: Token = readIdentifier()
                sb.append(idToken.data)
                if (isDotChar(lookaheadChar.char)) {
                    sb.append('.')
                    consume()
                }
                else {
                    break
                }
            }
            if (sb.endsWith('.')) {
                throw ConfigException("Invalid Path at position $startPos. The Path ends with a '.'")
            }
            return Token(sb.toString(), TokenType.PATH, startPos)
        }
        else {
            return idToken
        }
    }

    private fun readIdentifier(): Token {
        val sb = StringBuilder()
        val startPos = lookaheadChar.pos

        sb.append(lookaheadChar.char)
        consume() // first identifier char

        while (!lookaheadChar.eof() && isIdentifierChar(lookaheadChar.char)) {
            sb.append(lookaheadChar.char)
            consume()
        }

        return Token(sb.toString(), TokenType.IDENTIFIER, startPos)
    }

    private fun readAnyTokenToEOL(): Token {
        val sb = StringBuilder()
        val startPos = lookaheadChar.pos

        sb.append(lookaheadChar.char)
        consume() // first any char

        while (!lookaheadChar.eof() && isAnyChar(lookaheadChar.char)) {
            sb.append(lookaheadChar.char)
            consume()
        }

        return Token(sb.toString(), TokenType.ANY, startPos)
    }

    private fun isDotChar(ch: Char?): Boolean = (ch == '.')

    private fun isCommentChar(ch: Char?): Boolean = (ch == '#')

    private fun isWhitespaceChar(ch: Char?): Boolean {
        return when (ch) {
            ' ', '\n', '\r', '\t' -> true
            else -> false
        }
    }

    private fun isIdentifierStartChar(ch: Char?): Boolean {
        return when (ch) {
            in 'a'..'z' -> true
            in 'A'..'Z' -> true
            else -> false
        }
    }

    private fun isIdentifierChar(ch: Char?): Boolean {
        return when (ch) {
            in 'a'..'z' -> true
            in 'A'..'Z' -> true
            in '0'..'9' -> true
            '_' -> true
            else -> false
        }
    }

    private fun isAnyChar(ch: Char?): Boolean = !isWhitespaceChar(ch)
}