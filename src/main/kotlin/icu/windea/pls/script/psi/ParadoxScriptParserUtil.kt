@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*

object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun processTemplate(b: PsiBuilder, l: Int): Boolean {
        val tokenType = b.rawLookup(-1)
        if(tokenType == TokenType.WHITE_SPACE) return false
        if(tokenType == ParadoxScriptElementTypes.COMMENT) return false
        if(tokenType in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) return false
        return true
    }
}
