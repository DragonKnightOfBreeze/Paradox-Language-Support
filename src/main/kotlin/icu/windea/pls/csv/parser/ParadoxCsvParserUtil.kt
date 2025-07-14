package icu.windea.pls.csv.parser

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

object ParadoxCsvParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun checkLineBreak(b: PsiBuilder, l: Int): Boolean {
        var i = -1
        var next = b.rawLookup(i)
        if (next == TokenType.WHITE_SPACE) {
            i--
            next = b.rawLookup(i)
        }
        if (next == EOL) return false
        return true
    }
}
