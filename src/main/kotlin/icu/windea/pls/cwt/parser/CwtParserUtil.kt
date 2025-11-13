package icu.windea.pls.cwt.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import icu.windea.pls.core.lookupWithOffset
import icu.windea.pls.cwt.psi.CwtElementTypes.*

@Suppress("UNUSED_PARAMETER")
object CwtParserUtil: GeneratedParserUtilBase() {
    @JvmStatic
    fun checkEol(b: PsiBuilder, l: Int): Boolean {
        val (next) = b.lookupWithOffset(0, forward = true)
        if (next == null) return true
        val (prev) = b.lookupWithOffset(-1, forward = false)
        return prev != EOL
    }
}
