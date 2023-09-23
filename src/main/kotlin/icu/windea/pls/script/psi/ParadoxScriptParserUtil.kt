@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*

object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun skipWhitespace(b: PsiBuilder, l: Int): Boolean {
        val tokenType = b.rawLookup(0)
        if(tokenType == TokenType.WHITE_SPACE || tokenType == ParadoxScriptElementTypes.COMMENT) return false
        return true
    }
}