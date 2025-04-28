package icu.windea.pls.cwt.psi.util

import com.intellij.lang.*
import com.intellij.lang.parser.*
import icu.windea.pls.cwt.psi.*

object CwtParserUtil: GeneratedParserUtilBase() {
    @JvmStatic
    fun parseOptionValue(b: PsiBuilder, l: Int) : Boolean {
        return CwtParser.value(b, l)
    }
}
