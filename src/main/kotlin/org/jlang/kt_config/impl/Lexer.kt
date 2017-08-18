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


class Lexer(private val reader: StringReader): Iterator<Token> {
    private val WHITESPACES = setOf(' ', '\n', '\r', '\t')

    // lookahead buffer with one character
    private var lookahead: Lookahead<Character> = Lookahead(1, reader)

    override fun hasNext(): Boolean = true

    override fun next(): Token {
        while (!lookahead[0].eof()) {
            when {
                lookahead[0].isChar('{') -> return Token(LBRACE, lookahead[0]).also { lookahead.consume() }
                lookahead[0].isChar('}') -> return Token(RBRACE, lookahead[0]).also { lookahead.consume() }
                lookahead[0].isChar('[') -> return Token(LBRACK, lookahead[0]).also { lookahead.consume() }
                lookahead[0].isChar(']') -> return Token(RBRACK, lookahead[0]).also { lookahead.consume() }
                lookahead[0].isChar('=') -> return Token(EQUALS, lookahead[0]).also { lookahead.consume() }
                lookahead[0].isChar(',') -> return Token(COMMA,  lookahead[0]).also { lookahead.consume() }
                lookahead[0].isChar('"') -> return readStringToken('"')
                lookahead[0].isChar('\'') -> return readStringToken('\'')
                isCommentChar(lookahead[0].char) -> consumeCommentToEOL()
                isWhitespaceChar(lookahead[0].char) -> consumeWhitespaces()
                isIdentifierStartChar(lookahead[0].char) -> return readIdentifierOrPathToken()
                else -> return readAnyTokenToEOL() // let the parser decide about the error
            }
        }

        return Token.eof(lookahead[0].pos)
    }


    private fun consumeWhitespaces(): Unit {
        readChars({ isWhitespaceChar(lookahead[0].char) })
    }

    private fun consumeCommentToEOL(): Unit {
        readChars({ !isEOL(lookahead[0].char) })
    }

    private fun readStringToken(quote: Char): Token {
        val sb = StringBuilder()
        val startPos = lookahead[0].pos

        lookahead.consume() // leading quote

        while (!lookahead[0].eof() && lookahead[0].isNotChar(quote)) {
            if (lookahead[0].isChar('\n', '\r')) {
                throw ConfigException(
                        "A string must not be defined across lines. "
                                + "Position ${lookahead[0].pos}. Use a \\n instead.")
            }
            sb.append(if (lookahead[0].isEscapeChar()) readEscapedChar() else lookahead[0].char)
            lookahead.consume()
        }

        if (lookahead[0].eof()) {
            throw ConfigException(
                    "Unexpected EOF within a string at position $startPos")
        }

        lookahead.consume() // trailing quote

        return Token(TokenType.STRING, sb.toString(), startPos)
    }

    private fun readEscapedChar() : Char {
        val pos = lookahead[0].pos

        lookahead.consume() // escape char
        when(lookahead[0].char) {
            'n'  -> return '\n'
            'r'  -> return '\r'
            't'  -> return '\t'
            '\'' -> return '\''
            '"'  -> return '"'
            else -> throw ConfigException(
                    "Invalid escaped character '\\${lookahead[0].char}' at "
                            + "position $pos. Supported escape characters: "
                            + "\\n, \\r, \\t, \\', \\\"")
        }
    }

    private fun readIdentifierOrPathToken(): Token {
        val startPos = lookahead[0].pos
        val idToken: Token = readIdentifier()

        if (isDotChar(lookahead[0].char)) {
            val path = StringBuilder().append(idToken.data)

            while (isDotChar(lookahead[0].char)) {
                path.append('.')
                lookahead.consume()

                if (!isIdentifierStartChar(lookahead[0].char)) {
                    throw ConfigException(
                            "Path unexpectedly ends at position ${lookahead[0].pos}.")
                }

                path.append(readIdentifier().data)
            }

            return Token(PATH, path.toString(), startPos)
        }
        else {
            return idToken
        }
    }

    private fun readIdentifier(): Token =
        Token(IDENTIFIER, readChars({ isIdentifierPartChar(lookahead[0].char) }), lookahead[0].pos)

    private fun readAnyTokenToEOL(): Token =
        Token(ANY, readChars({ isAnyChar(lookahead[0].char) }), lookahead[0].pos)

    private fun readChars(cond: () -> Boolean): String {
        val sb = StringBuilder()
        while (!lookahead[0].eof() && cond()) {
            sb.append(lookahead[0].char)
            lookahead.consume()
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