package icu.windea.pls.localisation.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_SINGLE_QUOTE
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun isCommand(b: PsiBuilder, l: Int): Boolean {
        val tokenAfterLeftBracket = b.rawLookup(1)
        return tokenAfterLeftBracket != LEFT_SINGLE_QUOTE
    }

    @JvmStatic
    fun isConceptCommand(b: PsiBuilder, l: Int): Boolean {
        return ParadoxSyntaxConstraint.LocalisationConceptCommand.supports(b)
    }
}
