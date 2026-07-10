package icu.windea.pls.csv.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import icu.windea.pls.core.lookup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

@Suppress("UNUSED_PARAMETER")
object ParadoxCsvParserUtil : GeneratedParserUtilBase() {
    object Keys : KeyRegistry()

    var PsiBuilder.isFirstColumn: Boolean by registerKey(Keys) { false }
    var PsiBuilder.isEmptyColumn: Boolean by registerKey(Keys) { false }

    @JvmStatic
    fun checkEol(b: PsiBuilder, l: Int): Boolean {
        if (b.isFirstColumn && b.isEmptyColumn) return true
        val next = b.lookup(0, forward = true)
        if (next == null) return true
        val prev = b.lookup(-1, forward = false)
        return prev != EOL
    }

    @JvmStatic
    fun checkColumnToken(b: PsiBuilder, l: Int): Boolean {
        val next = b.lookup(0, forward = true)
        val prev = b.lookup(-1, forward = false)
        // Valid when:
        // - next is a real column token
        // - next is a separator, and we're either at line start (prev == EOL) or between two separators (prev == SEPARATOR) - this allows empty first/middle columns
        val isValid = next == COLUMN_TOKEN || (next == SEPARATOR && (prev == SEPARATOR || prev == EOL))
        if (!isValid) return false
        b.isFirstColumn = prev == EOL
        b.isEmptyColumn = next != COLUMN_TOKEN
        return true
    }
}
