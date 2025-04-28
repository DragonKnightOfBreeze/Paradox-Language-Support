@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.psi.util

import com.intellij.lang.*
import com.intellij.lang.parser.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptTokenSets as TokenSets

object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun postProcessFirstSnippet(b: PsiBuilder, l: Int): Boolean {
        //compact format is allowed, e.g., k1 = "v1"k2 = v2
        //a token should not be parsed to a value when with a trailing separator
        var s = -1
        var end = false
        while (true) {
            s++
            val t = b.rawLookup(s)
            when {
                t == null -> break
                t in TokenSets.BREAK_SNIPPET_TYPES -> end = true
                t in TokenSets.LEFT_SNIPPET_TYPES && b.rawLookup(s - 1) in TokenSets.RIGHT_SNIPPET_TYPES -> break
                t in TokenSets.PROPERTY_SEPARATOR_TOKENS -> return false
                else -> if (end) break
            }
        }
        return true
    }

    @JvmStatic
    fun processSnippet(b: PsiBuilder, l: Int): Boolean {
        //interrupt parsing when contains whitespaces or comments
        //also for continuous literals
        val t = b.rawLookup(-1)
        when {
            t in TokenSets.BREAK_SNIPPET_TYPES -> return false
            t in TokenSets.SNIPPET_TYPES -> {
                val nextTokenType = b.rawLookup(0)
                if (nextTokenType != null && nextTokenType in TokenSets.SNIPPET_TYPES) return false
            }
        }
        return true
    }

    @JvmStatic
    fun processInlineParameterCondition(b: PsiBuilder, l: Int): Boolean {
        //interrupt parsing when contains whitespaces or comments
        //also must not be alone in whole template expression
        var i = 1
        var n = 1
        while (true) {
            val t = b.rawLookup(i) ?: break
            when {
                t in TokenSets.BREAK_SNIPPET_TYPES -> return false
                t == LEFT_BRACKET -> n++
                t == RIGHT_BRACKET -> n--
            }
            if (n == 0) break
            i++
        }
        if(b.rawLookup(-2) in TokenSets.BREAK_SNIPPET_TYPES) return false
        if(b.rawLookup(i + 1) in TokenSets.BREAK_SNIPPET_TYPES) return false
        return true
    }

    @JvmStatic
    fun processInlineParameterConditionItem(b: PsiBuilder, l: Int): Boolean {
        //remapping token types to PARAMETER_VALUE_TOKEN for inline parameter condition items
        if (b !is Builder) return true
        b.setTokenTypeRemapper m@{ t, _, _, _ ->
            if (t in TokenSets.SNIPPET_TYPES) return@m PARAMETER_VALUE_TOKEN
            t
        }
        return true
    }

    @JvmStatic
    fun postProcessInlineParameterConditionItem(b: PsiBuilder, l: Int): Boolean {
        //reset remapping
        if (b !is Builder) return true
        b.setTokenTypeRemapper(null)
        return true
    }
}
