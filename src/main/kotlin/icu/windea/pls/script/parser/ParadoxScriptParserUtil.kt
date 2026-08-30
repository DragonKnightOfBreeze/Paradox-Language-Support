package icu.windea.pls.script.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptTokenSets as TokenSets

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    private val LHS_SNIPPET_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, SCRIPTED_VARIABLE_NAME_TOKEN)
    private val RHS_SNIPPET_TOKENS = TokenSet.create(STRING_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    private val SNIPPET_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN, SCRIPTED_VARIABLE_NAME_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    private val BREAK_PARTS_TOKENS = TokenSet.create(TokenType.WHITE_SPACE, COMMENT, LEFT_BRACE, RIGHT_BRACE)
    private val SEPARATOR_TOKENS = TokenSets.PROPERTY_SEPARATOR_TOKENS

    private val LHS_CONTENT_VALID_TOKENS = TokenSet.create(*LHS_SNIPPET_TOKENS.types, *SEPARATOR_TOKENS.types)
    private val LHS_CONTENT_INVALID_TOKENS = TokenSet.create(*RHS_SNIPPET_TOKENS.types, *BREAK_PARTS_TOKENS.types)

    @JvmStatic
    fun processLhsContent(b: PsiBuilder, l: Int): Boolean {
        // interpolations should be parsed to LHS (property key, scripted variable) parts when with a trailing separator
        // compatible with compact formats (e.g., `k1 = "v1"k2 = v2`)
        var s = -1
        while (true) {
            s++
            val t = b.rawLookup(s) // first LHS part (e.g., PROPERTY_KEY_TOKEN)
            if (t == null) return true // null -> should be EOF -> return true for better error report
            when (t) {
                in LHS_CONTENT_VALID_TOKENS -> return true
                in LHS_CONTENT_INVALID_TOKENS -> return false
            }
        }
    }

    @JvmStatic
    fun processPart(b: PsiBuilder, l: Int): Boolean {
        // interrupt parsing interpolation container parts when contains whitespaces or comments
        // compatible with continuous literals
        val t = b.rawLookup(-1)
        when {
            t in SNIPPET_TOKENS -> {
                val nextTokenType = b.rawLookup(0)
                if (nextTokenType != null && nextTokenType in SNIPPET_TOKENS) return false
            }
            t in BREAK_PARTS_TOKENS -> return false
        }
        return true
    }

    @JvmStatic
    fun processInlineConditionalBlock(b: PsiBuilder, l: Int): Boolean {
        // TODO 3.0.2 refactor
        // interrupt parsing when contains whitespaces or comments
        var i = 1
        var n = 1
        while (true) {
            val t = b.rawLookup(i) ?: break
            when {
                t in BREAK_PARTS_TOKENS -> return false
                t == LEFT_BRACKET -> n++
                t == RIGHT_BRACKET -> n--
            }
            if (n == 0) break
            i++
        }
        if (b.rawLookup(-2) in BREAK_PARTS_TOKENS) return false
        if (b.rawLookup(i + 1) in BREAK_PARTS_TOKENS) return false
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
