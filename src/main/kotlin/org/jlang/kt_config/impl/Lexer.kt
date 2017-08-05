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
    private var lookahead: Character = reader.readNext()

    fun nextToken(): Token {
        while (!lookahead.eof()) {
            when {
                lookahead.isChar('{') -> return Token(TokenType.LBRACE, lookahead).also { consume() }
                lookahead.isChar('}') -> return Token(TokenType.RBRACE, lookahead).also { consume() }
                lookahead.isChar('[') -> return Token(TokenType.LBRACK, lookahead).also { consume() }
                lookahead.isChar(']') -> return Token(TokenType.RBRACK, lookahead).also { consume() }
                lookahead.isChar('=') -> return Token(TokenType.EQUALS, lookahead).also { consume() }
                lookahead.isChar(',') -> return Token(TokenType.COMMA, lookahead).also { consume() }
                lookahead.isChar('"') -> return readStringToken('"')
                lookahead.isChar('\'') -> return readStringToken('\'')
                isCommentChar(lookahead.char) -> consumeCommentToEOL()
                isWhitespaceChar(lookahead.char) -> consumeWhitespaces()
                isIdentifierStartChar(lookahead.char) -> return readIdentifierOrPathToken()
                else -> return readAnyTokenToEOL() // let the parser decide about the error
            }
        }

        return Token.eof(lookahead.pos)
    }

    private fun consume(): Character = reader.readNext().also { lookahead = it }

    private fun consumeWhitespaces(): Unit {
        while (isWhitespaceChar(lookahead.char)) consume()
    }

    private fun consumeCommentToEOL(): Unit {
        consume()
        while (lookahead.isNotChar('\n', '\r') && !lookahead.eof()) consume()
    }

    private fun readStringToken(quote: Char): Token {
        val sb = StringBuilder()
        val startPos = lookahead.pos

        consume() // leading quote

        while (!lookahead.eof() && lookahead.isNotChar(quote)) {
            if (lookahead.isChar('\n', '\r')) {
                throw ConfigException(
                        "A string must not be defined across lines. "
                                + "Position ${lookahead.pos}. Use a \\n instead.")
            }
            sb.append(if (lookahead.isEscapeChar()) readEscapedChar() else lookahead.char)
            consume()
        }

        if (lookahead.eof()) {
            throw ConfigException(
                    "Unexpected EOF within a string at position $startPos")
        }

        consume() // trailing quote

        return Token(TokenType.STRING, sb.toString(), startPos)
    }

    private fun readEscapedChar() : Char {
        val pos = lookahead.pos

        consume()
        when(lookahead.char) {
            'n'  -> return '\n'
            'r'  -> return '\r'
            't'  -> return '\t'
            '\'' -> return '\''
            '"'  -> return '"'
            else -> throw ConfigException(
                    "Invalid escaped character '\\${lookahead.char}' a "
                            + "position ${pos}. Supported escape characters are: "
                            + "\\n, \\r, \\t, \\', \\\"")

        }
    }

    private fun readIdentifierOrPathToken(): Token {
        val sb = StringBuilder()
        val startPos = lookahead.pos
        val idToken: Token = readIdentifier()

        if (isDotChar(lookahead.char)) {
            sb.append(idToken.data)
            sb.append('.')
            consume()
            while(isIdentifierStartChar(lookahead.char)) {
                val idToken: Token = readIdentifier()
                sb.append(idToken.data)
                if (isDotChar(lookahead.char)) {
                    sb.append('.')
                    consume()
                }
                else {
                    break
                }
            }
            if (sb.endsWith('.')) {
                throw ConfigException(
                        "Invalid path at position ${startPos}. Paths must not end with a '.'")
            }
            return Token(TokenType.PATH, sb.toString(), startPos)
        }
        else {
            return idToken
        }
    }

    private fun readIdentifier(): Token {
        val sb = StringBuilder()
        val startPos = lookahead.pos

        sb.append(lookahead.char)
        consume() // first identifier char

        while (!lookahead.eof() && isIdentifierChar(lookahead.char)) {
            sb.append(lookahead.char)
            consume()
        }

        return Token(TokenType.IDENTIFIER, sb.toString(), startPos)
    }

    private fun readAnyTokenToEOL(): Token {
        val sb = StringBuilder()
        val startPos = lookahead.pos

        sb.append(lookahead.char)
        consume() // first any char

        while (!lookahead.eof() && isAnyChar(lookahead.char)) {
            sb.append(lookahead.char)
            consume()
        }

        return Token(TokenType.ANY, sb.toString(), startPos)
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