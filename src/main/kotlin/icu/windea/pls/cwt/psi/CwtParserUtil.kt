package icu.windea.pls.cwt.psi

import com.intellij.lang.*
import com.intellij.lang.parser.GeneratedParserUtilBase

object CwtParserUtil: GeneratedParserUtilBase() {
    @JvmStatic
    fun parseOptionValue(b: PsiBuilder, l: Int) : Boolean {
        return CwtParser.value(b, l)
    }
}
