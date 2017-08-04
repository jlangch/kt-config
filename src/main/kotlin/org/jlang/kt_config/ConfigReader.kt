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

package org.jlang.kt_config

import org.jlang.kt_config.impl.*
import org.jlang.kt_config.impl.TokenType.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


/**
 * Creates a configuration reader from a string with optional user definitions.
 * The companion objects offers creating a configuration reader from an
 * InputStream
 *
 * The reader throws a ConfigurationException if the passed configuration
 * does not meet the grammar or if not all referenced definitions could
 * be resolved.
 *
 * ###Configuration EBNF
 * ```
 *    Config ::= DefStatement { Section | ConfigItem }
 *    DefStatement ::= 'def' Identifier '=' Value
 *    Section ::= (Identifier | Path) '{' { (Section | ConfigItem) } '}'
 *    ConfigItem ::= (Identifier | Path) '=' Value
 *    Identifier ::= (a..z | A..Z)  { (a..z | A..Z| 0..9 | '_') }
 *    Path ::= Identifier { '.' Identifier }
 *    Value ::= ('"' <any-char> '"' | ''' <any-char> '''
 * ``
 */
class ConfigReader(
        config: String,
        private val userDefinitions: Map<String, String> = HashMap()
) {
    // Actually a LL(2) parser is sufficient but LL(4) simplifies code
    val LOOKAHEAD_SIZE = 4

    val lexer: Lexer = Lexer(StringReader(config.trim()))
    val mapBuilder: ConfigMapBuilder = ConfigMapBuilder()
    val definitions: MutableMap<String, String> = LinkedHashMap()

    // prime lookahead buffer with tokens
    var lookahead: MutableList<Token> =
            ArrayList<Token>().apply { repeat(LOOKAHEAD_SIZE, { add(lexer.nextToken()) }) }

    companion object Factory {
        fun create(
                inStream: InputStream,
                charset: Charset = Charsets.UTF_8,
                userDefinitions: Map<String, String> = HashMap()
        ): ConfigReader = ConfigReader(slurp(inStream, charset), userDefinitions)

        private fun slurp(inStream: InputStream, charset: Charset = Charsets.UTF_8): String {
            return BufferedReader(InputStreamReader(inStream, charset)).use {
                it.readLines().reduce({ s1, s2 -> s1 + "\n" + s2 })
            }
        }
    }

    fun read(): Config {
        try {
            while (parseDefStatement()) { }

            // add the overriding user definitions, environment variables,
            // and system properties
            definitions.putAll(userDefinitions)
            definitions.putAll(getPrefixedEnvironmentVariables())
            definitions.putAll(getPrefixedSystemProperties())

            // a config is built from any number of config items and sections
            while (parseConfigItemOrSection()) { }

            if (lookahead[0].isNotType(EOF)) {
                throw ConfigException("Expected EOF at position ${lookahead[0].pos}.")
            }

            return Config(LinkedHashMap(mapBuilder.get()))
        } catch(ex: ConfigException) {
            throw ex
        } catch(ex: Exception) {
            throw ConfigException("Internal error.", ex)
        }
    }


    private fun consume(numTokens: Int) {
        fun shift() {
            lookahead.removeAt(0)
            lookahead.add(lexer.nextToken())
        }

        repeat(numTokens, { shift() })
    }

    private fun parseDefStatement(): Boolean {
        if (lookahead[0].isType(IDENTIFIER) && "def" == lookahead[0].data) {
            if (lookahead[1].isNotType(IDENTIFIER)) {
                throw ConfigException(
                        "Invalid variable definition at position ${lookahead[1].pos}. "
                                + "Expected a variable name.")
            }
            if (lookahead[2].isNotType(EQUALS)) {
                throw ConfigException(
                        "Invalid variable definition at position ${lookahead[2].pos}. "
                                + "Expected '='.")
            }
            if (lookahead[3].isNotType(STRING)) {
                throw ConfigException(
                        "Invalid variable definition at position ${lookahead[3].pos}. "
                                + "Expected a variable value string.")
            }

            definitions.put(lookahead[1].data, lookahead[3].data)
            consume(4)

            return true
        } else {
            return false
        }
    }

    private fun parseConfigItemOrSection(): Boolean = parseConfigItem() || parseSection()

    private fun parseConfigItem(): Boolean {
        if (lookahead[0].isType(IDENTIFIER, PATH) && lookahead[1].isType(EQUALS)) {
            if (lookahead[2].isNotType(STRING)) {
                throw ConfigException(
                        "Invalid config item at position ${lookahead[2].pos}. "
                                + "Expected a double quoted value.")
            }

            mapBuilder.put(
                    lookahead[0].data,
                    applyDefinitions(lookahead[2].data, lookahead[2].pos))
            consume(3)

            return true
        } else {
            return false
        }
    }

    private fun parseSection(): Boolean {
        if (lookahead[0].isType(IDENTIFIER, PATH) && lookahead[1].isType(LEFT_BRACKET)) {
            mapBuilder.pushPath(lookahead[0].data)
            consume(2)

            // a section is built from any number of config items and sub sections
            while (parseConfigItemOrSection()) {}

            if (lookahead[0].isNotType(RIGHT_BRACKET)) {
                throw ConfigException(
                        "Expected section close '{' at position ${lookahead[0].pos}.")
            }
            consume(1)

            mapBuilder.popPath()

            return true
        } else {
            return false
        }
    }

    private fun applyDefinitions(text: String, tokenPos: Position): String {
        var tmp: String = text
        definitions.forEach { k, v -> tmp = tmp.replace("${'$'}{$k}", v) }
        if (hasDefinitions(tmp)) {
            throw ConfigException(
                    "Unresolved definition in value at position ${tokenPos}.")
        }
        return tmp
    }

    private fun hasDefinitions(text: String): Boolean {
        return text.matches(Regex(".*[$][{][a-zA-Z][a-zA-Z0-9_]*[}].*"))
    }

    private fun getPrefixedEnvironmentVariables(): Map<String,String> =
            System.getenv()
                    .filterValues { v ->  v != null }
                    .mapKeys { entry -> "env." + entry.key }

    private fun getPrefixedSystemProperties(): Map<String,String> =
            System.getProperties()
                    .filterValues { v ->  v != null }
                    .mapValues { entry -> entry.value.toString() }
                    .mapKeys { entry -> "system." + entry.key }
}