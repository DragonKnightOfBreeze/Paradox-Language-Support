package icu.windea.pls.localisation.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import icu.windea.pls.core.lookup
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun isCommand(b: PsiBuilder, l: Int): Boolean {
        val tokenAfterLeftBracket =  b.lookup(1, forward = true)
        return tokenAfterLeftBracket != LEFT_SINGLE_QUOTE
    }
}
