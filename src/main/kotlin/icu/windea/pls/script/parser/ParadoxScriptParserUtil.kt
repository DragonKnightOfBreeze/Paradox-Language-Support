package icu.windea.pls.script.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptTokenSets as TokenSets

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    private val SNIPPET_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN, SCRIPTED_VARIABLE_NAME_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    private val LEFT_SNIPPET_TOKENS = TokenSet.create(LEFT_QUOTE, AT, PROPERTY_KEY_TOKEN, SCRIPTED_VARIABLE_NAME_TOKEN)
    private val RIGHT_SNIPPET_TOKENS = TokenSet.create(RIGHT_QUOTE, STRING_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    private val BREAK_SNIPPET_TOKENS = TokenSet.create(TokenType.WHITE_SPACE, COMMENT)
    private val PROPERTY_SEPARATOR_TOKENS = TokenSets.PROPERTY_SEPARATOR_TOKENS

    @JvmStatic
    fun processRightParts(b: PsiBuilder, l: Int): Boolean {
        // compact format is allowed, e.g., `k1 = "v1"k2 = v2`
        // a token should not be parsed to a value when with a trailing separator
        var s = -1
        var end = false
        while (true) {
            s++
            val t = b.rawLookup(s)
            when {
                t == null -> break
                t in BREAK_SNIPPET_TOKENS -> end = true
                t in LEFT_SNIPPET_TOKENS && b.rawLookup(s - 1) in RIGHT_SNIPPET_TOKENS -> break
                t in PROPERTY_SEPARATOR_TOKENS -> return false
                else -> if (end) break
            }
        }
        return true
    }

    @JvmStatic
    fun processPart(b: PsiBuilder, l: Int): Boolean {
        // interrupt parsing when contains whitespaces or comments
        // also for continuous literals
        val t = b.rawLookup(-1)
        when {
            t in BREAK_SNIPPET_TOKENS -> return false
            t in SNIPPET_TOKENS -> {
                val nextTokenType = b.rawLookup(0)
                if (nextTokenType != null && nextTokenType in SNIPPET_TOKENS) return false
            }
        }
        return true
    }

    @JvmStatic
    fun processInlineConditionalBlock(b: PsiBuilder, l: Int): Boolean {
        // interrupt parsing when contains whitespaces or comments
        // also must not be alone in whole expression
        var i = 1
        var n = 1
        while (true) {
            val t = b.rawLookup(i) ?: break
            when {
                t in BREAK_SNIPPET_TOKENS -> return false
                t == LEFT_BRACKET -> n++
                t == RIGHT_BRACKET -> n--
            }
            if (n == 0) break
            i++
        }
        if (b.rawLookup(-2) in BREAK_SNIPPET_TOKENS) return false
        if (b.rawLookup(i + 1) in BREAK_SNIPPET_TOKENS) return false
        return true
    }

    @JvmStatic
    fun processInlineConditionalBlockItem(b: PsiBuilder, l: Int): Boolean {
        // remapping token types to ARGUMENT_TOKEN for inline conditional block items
        if (b !is Builder) return true
        b.setTokenTypeRemapper m@{ t, _, _, _ ->
            if (t in SNIPPET_TOKENS) return@m ARGUMENT_TOKEN
            t
        }
        return true
    }

    @JvmStatic
    fun postProcessInlineConditionalBlockItem(b: PsiBuilder, l: Int): Boolean {
        // reset remapping
        if (b !is Builder) return true
        b.setTokenTypeRemapper(null)
        return true
    }
}
