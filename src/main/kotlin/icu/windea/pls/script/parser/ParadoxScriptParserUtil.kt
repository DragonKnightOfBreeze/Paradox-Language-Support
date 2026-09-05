package icu.windea.pls.script.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import it.unimi.dsi.fastutil.ints.IntArrayList
import icu.windea.pls.script.psi.ParadoxScriptTokenSets as TokenSets

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    private val SEPARATOR_TOKENS = TokenSets.PROPERTY_SEPARATOR_TOKENS
    private val SNIPPET_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN, SCRIPTED_VARIABLE_NAME_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    // private val LHS_SNIPPET_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, SCRIPTED_VARIABLE_NAME_TOKEN)
    // private val RHS_SNIPPET_TOKENS = TokenSet.create(STRING_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    private val ACCEPT_LHS_TOKENS = SEPARATOR_TOKENS
    private val REJECT_LHS_TOKENS = TokenSet.create(AT, STRING_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)

    @JvmStatic
    fun processLhsContent(b: PsiBuilder, l: Int): Boolean {
        // check after first part (include `@`)
        // interpolations should be parsed to LHS (property key, scripted variable) parts when with a trailing separator

        // compatible with compact formats
        // e.g., `k1 = "v1"k2 = v2` (where there are no whitespaces before `k2`)

        var i = -1
        var end = false
        while (true) {
            i++
            val t = b.rawLookup(i) // token after first LHS part (e.g., PROPERTY_KEY_TOKEN)
            if (t == null) return false // null -> should be EOF
            when (t) {
                TokenType.WHITE_SPACE, COMMENT -> end = true
                in ACCEPT_LHS_TOKENS -> return true
                in REJECT_LHS_TOKENS -> return false
                else -> if (end) return false
            }
        }
    }

    @JvmStatic
    fun processPart(b: PsiBuilder, l: Int): Boolean {
        // check before every part, except first part
        // interrupt parsing interpolation container parts when contains whitespace tokens or comment tokens
        // compatible with continuous literals

        val t = b.rawLookup(-1)
        when (t) {
            TokenType.WHITE_SPACE, COMMENT -> return false
            in SNIPPET_TOKENS -> {
                val nextTokenType = b.rawLookup(0)
                if (nextTokenType != null && nextTokenType in SNIPPET_TOKENS) return false
            }
        }
        return true
    }

    private const val IN_CONDITIONAL_HEADER = 0
    private const val IN_CONDITIONAL_BODY = 1

    @JvmStatic
    fun processInlineConditionalBlock(b: PsiBuilder, l: Int): Boolean {
        // check after conditional block start marker (`LEFT_BRACKET`)
        // interrupt parsing inline conditional block when its body contains whitespace tokens, comment tokens, or separator tokens

        // compatible with optional whitespaces
        // e.g., `"[ [ PARAM ] text ]"` (where ` text ` is a `STRING_TOKEN`, other whitespaces are still whitespace tokens)

        var i = -1
        val expectState = IntArrayList()
        expectState.push(IN_CONDITIONAL_HEADER)
        while (true) {
            i++
            val t = b.rawLookup(i) ?: break
            when (t) {
                LEFT_BRACKET -> {
                    expectState.push(IN_CONDITIONAL_HEADER)
                }
                NESTED_RIGHT_BRACKET -> {
                    expectState.push(IN_CONDITIONAL_BODY)
                }
                RIGHT_BRACKET -> {
                    if (expectState.size < 2) return false // unexpected, but in case
                    expectState.popInt()
                    expectState.popInt()
                }
                TokenType.WHITE_SPACE, COMMENT -> {
                    if (expectState.topInt() == IN_CONDITIONAL_BODY) return false
                }
                in SEPARATOR_TOKENS -> {
                    return false
                }
            }
            if (expectState.isEmpty) break
        }
        return true
    }
}
