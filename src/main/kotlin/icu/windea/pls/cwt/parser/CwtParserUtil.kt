package icu.windea.pls.cwt.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import icu.windea.pls.core.lookup
import icu.windea.pls.cwt.psi.CwtElementTypes.*

@Suppress("UNUSED_PARAMETER")
object CwtParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun checkEol(b: PsiBuilder, l: Int): Boolean {
        // check before each option member (`option`, `option_value`)
        val next = b.lookup(0, forward = true)
        if (next == null) return true
        val prev = b.lookup(-1, forward = false)
        return prev != EOL
    }
}
