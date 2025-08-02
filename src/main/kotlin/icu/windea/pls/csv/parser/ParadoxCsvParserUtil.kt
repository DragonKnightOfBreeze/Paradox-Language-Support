package icu.windea.pls.csv.parser

import com.intellij.lang.*
import com.intellij.lang.parser.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

@Suppress("UNUSED_PARAMETER")
object ParadoxCsvParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun checkEol(b: PsiBuilder, l: Int): Boolean {
        val (next) = b.lookupWithOffset(0, forward = true)
        if (next == null) return true
        val (prev) = b.lookupWithOffset(-1, forward = false)
        val isEol = prev == EOL
        return !isEol
    }

    @JvmStatic
    fun checkColumnToken(b: PsiBuilder, l: Int): Boolean {
        val (next) = b.lookupWithOffset(0, forward = true)
        if (next == null) return false
        val isValidToken = next == COLUMN_TOKEN || next == SEPARATOR
        return isValidToken
    }
}
