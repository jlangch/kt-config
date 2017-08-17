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
import org.jlang.kt_config.impl.TokenType.*


class Lexer(private val text: String): Iterator<Token> {
    private val WHITESPACES = setOf(' ', '\n', '\r', '\t')
    private val reader = StringReader(text).iterator()
    private var lookahead: Character = reader.next()

    override fun hasNext(): Boolean = true

    override fun next(): Token {
        while (!lookahead.eof()) {
            when {
                lookahead.isChar('{') -> return Token(LBRACE, lookahead).also { consume() }
                lookahead.isChar('}') -> return Token(RBRACE, lookahead).also { consume() }
                lookahead.isChar('[') -> return Token(LBRACK, lookahead).also { consume() }
                lookahead.isChar(']') -> return Token(RBRACK, lookahead).also { consume() }
                lookahead.isChar('=') -> return Token(EQUALS, lookahead).also { consume() }
                lookahead.isChar(',') -> return Token(COMMA, lookahead).also { consume() }
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

    private fun consume(): Character = reader.next().also { lookahead = it }

    private fun consumeWhitespaces(): Unit {
        readChars({ isWhitespaceChar(lookahead.char) })
    }

    private fun consumeCommentToEOL(): Unit {
        readChars({ !isEOL(lookahead.char) })
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

        consume() // escape char
        when(lookahead.char) {
            'n'  -> return '\n'
            'r'  -> return '\r'
            't'  -> return '\t'
            '\'' -> return '\''
            '"'  -> return '"'
            else -> throw ConfigException(
                    "Invalid escaped character '\\${lookahead.char}' at "
                            + "position $pos. Supported escape characters: "
                            + "\\n, \\r, \\t, \\', \\\"")
        }
    }

    private fun readIdentifierOrPathToken(): Token {
        val sb = StringBuilder()
        val startPos = lookahead.pos
        val idToken: Token = readIdentifier()

        if (isDotChar(lookahead.char)) {
            sb.append(idToken.data).append('.')
            consume()
            while(isIdentifierStartChar(lookahead.char)) {
                sb.append(readChars({ isIdentifierPartChar(lookahead.char) }))
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
                        "Invalid path at position $startPos. Paths must not end with a '.'")
            }
            return Token(PATH, sb.toString(), startPos)
        }
        else {
            return idToken
        }
    }

    private fun readIdentifier(): Token =
        Token(IDENTIFIER, readChars({ isIdentifierPartChar(lookahead.char) }), lookahead.pos)

    private fun readAnyTokenToEOL(): Token =
        Token(ANY, readChars({ isAnyChar(lookahead.char) }), lookahead.pos)

    private fun readChars(cond: () -> Boolean): String {
        val sb = StringBuilder()
        while (!lookahead.eof() && cond()) {
            sb.append(lookahead.char)
            consume()
        }
        return sb.toString()
    }

    private fun isEOL(ch: Char?): Boolean = (ch == '\n')

    private fun isDotChar(ch: Char?): Boolean = (ch == '.')

    private fun isCommentChar(ch: Char?): Boolean = (ch == '#')

    private fun isWhitespaceChar(ch: Char?): Boolean = WHITESPACES.contains(ch)

    private fun isIdentifierStartChar(ch: Char?): Boolean =
            ch?.isJavaIdentifierStart() ?: false

    private fun isIdentifierPartChar(ch: Char?): Boolean =
            ch?.isJavaIdentifierPart() ?: false

    private fun isAnyChar(ch: Char?): Boolean = ch != null && !isWhitespaceChar(ch)
}