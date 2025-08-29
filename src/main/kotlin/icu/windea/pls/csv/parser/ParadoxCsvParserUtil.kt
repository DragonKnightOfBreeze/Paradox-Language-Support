package icu.windea.pls.csv.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import icu.windea.pls.core.lookupWithOffset
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.COLUMN_TOKEN
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.EOL
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.SEPARATOR

@Suppress("UNUSED_PARAMETER")
object ParadoxCsvParserUtil : GeneratedParserUtilBase() {
    object Keys : KeyRegistry()

    var PsiBuilder.isFirstColumn: Boolean by createKey(Keys) { false }
    var PsiBuilder.isEmptyColumn: Boolean by createKey(Keys) { false }

    @JvmStatic
    fun checkEol(b: PsiBuilder, l: Int): Boolean {
        if (b.isFirstColumn && b.isEmptyColumn) return true
        val (next) = b.lookupWithOffset(0, forward = true)
        if (next == null) return true
        val (prev) = b.lookupWithOffset(-1, forward = false)
        return prev != EOL
    }

    @JvmStatic
    fun checkColumnToken(b: PsiBuilder, l: Int): Boolean {
        val (next) = b.lookupWithOffset(0, forward = true)
        val (prev) = b.lookupWithOffset(-1, forward = false)
        val isValid = next == COLUMN_TOKEN || next == SEPARATOR || prev == SEPARATOR
        if (!isValid) return false
        b.isFirstColumn = prev == EOL
        b.isEmptyColumn = next != COLUMN_TOKEN
        return true
    }
}
