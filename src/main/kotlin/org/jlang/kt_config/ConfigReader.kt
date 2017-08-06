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

import org.jlang.kt_config.Config
import org.jlang.kt_config.impl.*
import org.jlang.kt_config.impl.TokenType.*
import java.io.Reader
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
 *    ConfigItem ::= (Identifier | Path) '=' (Value | MultiValue)
 *    Identifier ::= (a..z | A..Z)  { (a..z | A..Z| 0..9 | '_') }
 *    Path ::= Identifier { '.' Identifier }
 *    MultiValue ::= '[' Value { ',' Value } ']'
 *    Value ::= ('"' <any-char> '"' | ''' <any-char> '''
 * ``
 */
class ConfigReader(
        config: String,
        private val userDefinitions: Map<String,String> = HashMap()
) {
    // Actually a LL(2) parser is sufficient but LL(4) simplifies code
    val LOOKAHEAD_SIZE = 4

    val lexer: Lexer = Lexer(StringReader(config.trim()))
    val mapBuilder: ConfigMapBuilder = ConfigMapBuilder()
    val definitions: MutableMap<String,String> = LinkedHashMap()

    // prime lookahead buffer with tokens
    var lookahead: MutableList<Token> =
            ArrayList<Token>().apply { repeat(LOOKAHEAD_SIZE, { add(lexer.nextToken()) }) }

    companion object Factory {
        fun create(
                inStream: InputStream,
                charset: Charset = Charsets.UTF_8,
                userDefinitions: Map<String, String> = HashMap()
        ): ConfigReader = ConfigReader(slurp(inStream, charset), userDefinitions)

        fun create(
                reader: Reader,
                userDefinitions: Map<String, String> = HashMap()
        ): ConfigReader = ConfigReader(slurp(reader), userDefinitions)

        fun create(
                classPathResource: String,
                userDefinitions: Map<String, String> = HashMap()
        ): ConfigReader = ConfigReader(slurp(classPathResource), userDefinitions)

        fun create(
                classPathResource: String,
                loader: ClassLoader,
                userDefinitions: Map<String, String> = HashMap()
        ): ConfigReader = ConfigReader(slurp(classPathResource), userDefinitions)

        private fun slurp(inStream: InputStream, charset: Charset = Charsets.UTF_8): String {
            return BufferedReader(InputStreamReader(inStream, charset)).use {
                it.readLines().reduce({ s1,s2 -> s1 + "\n" + s2 })
            }
        }

        private fun slurp(reader: Reader): String {
            return BufferedReader(reader).use {
                it.readLines().reduce({ s1,s2 -> s1 + "\n" + s2 })
            }
        }

        private fun slurp(classPathResource: String): String {
            return slurp(this.javaClass.getResourceAsStream(classPathResource))
        }

        private fun slurp(classPathResource: String, loader: ClassLoader): String {
            return slurp(loader.getResourceAsStream(classPathResource))
        }
    }

    init {
        validateUserDefinitions(userDefinitions)
    }

    fun read(): Config {
        try {
            definitions.putAll(getPrefixedEnvironmentVariables("env"))
            definitions.putAll(getPrefixedSystemProperties("system"))
            definitions.putAll(userDefinitions)

            while (parseDefStatement()) { }

            // a config is built from any number of config items and sections
            while (parseConfigItemOrSection()) { }

            if (lookahead[0].isNotType(EOF)) {
                throw ConfigException("Expected EOF at position ${lookahead[0].pos}.")
            }

            return ConfigImpl(ConfigObject(LinkedHashMap(mapBuilder.get())))
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

            definitions.put(
                    lookahead[1].data,
                    applyDefinitions(lookahead[3].data, lookahead[3].pos))
            consume(4)

            return true
        } else {
            return false
        }
    }

    private fun parseConfigItemOrSection(): Boolean = parseConfigItem() || parseSection()

    private fun parseConfigItem(): Boolean {
        if (lookahead[0].isType(IDENTIFIER, PATH) && lookahead[1].isType(EQUALS)) {
            if (lookahead[2].isType(STRING)) {
                // single value: name = "value"
                mapBuilder.put(
                        lookahead[0].data,
                        applyDefinitions(lookahead[2].data, lookahead[2].pos))

                consume(3)
            }
            else if (lookahead[2].isType(LBRACK)) {
                // multi value: name = [ "value1", "value2", ... ]
                val name = lookahead[0].data
                val pos = lookahead[2].pos

                consume(3)

                parseConfigItem_MultiValues(name, pos)

                if (lookahead[0].isNotType(RBRACK)) {
                    throw ConfigException(
                            "Expected array close ']' at position ${lookahead[0].pos}.")
                }

                consume(1)
            }
            else {
                throw ConfigException(
                        "Invalid config item at position ${lookahead[2].pos}. "
                                + "Expected a double quoted value.")
            }

            return true
        } else {
            return false
        }
    }

    private fun parseConfigItem_MultiValues(name: String, pos: Position): Unit {
        var valueIndex = 1

        if (lookahead[0].isType(STRING)) {
            mapBuilder.put(
                    name,
                    valueIndex++,
                    applyDefinitions(lookahead[0].data, lookahead[0].pos))

            consume(1)

            while (lookahead[0].isType(COMMA) && lookahead[1].isType(STRING)) {
                mapBuilder.put(
                        name,
                        valueIndex++,
                        applyDefinitions(lookahead[1].data, lookahead[1].pos))

                consume(2)
            }
        }

        mapBuilder.put(composePath(name, "size"), (valueIndex-1).toString())
    }

    private fun parseSection(): Boolean {
        if (lookahead[0].isType(IDENTIFIER, PATH) && lookahead[1].isType(LBRACE)) {
            mapBuilder.pushPath(lookahead[0].data)
            consume(2)

            // a section is built from any number of config items and sub sections
            while (parseConfigItemOrSection()) {}

            if (lookahead[0].isNotType(RBRACE)) {
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
        val tmp = applyDefinitions(text, definitions)
        if (hasDefinitions(tmp)) {
            throw ConfigException(
                    "Unresolved definition in value at position ${tokenPos}.")
        }
        return tmp
    }

    private fun applyDefinitions(text: String, defs: Map<String,String>): String {
        var tmp: String = text
        defs.forEach { k, v -> tmp = tmp.replace("${'$'}{$k}", v) }
        return tmp
    }

    private fun hasDefinitions(text: String): Boolean {
        return text.matches(Regex(".*[$][{][^}]+[}].*"))
    }

    private fun validateUserDefinitions(definitions: Map<String,String>): Unit {
        fun isReservedKey(key: String) =
                key.startsWith("env.", true) || key.startsWith("system.", true)

        if (definitions.keys.filter { isReservedKey(it) }.any()) {
            throw ConfigException(
                    "User supplied definitions may not have keys starting "
                            + "with 'env.' or 'system.' thus covering "
                            + "environment variables or system properties.")
        }
    }
}