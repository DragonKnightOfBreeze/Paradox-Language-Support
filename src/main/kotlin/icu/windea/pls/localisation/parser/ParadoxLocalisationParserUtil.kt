package icu.windea.pls.localisation.parser

import com.intellij.lang.*
import com.intellij.lang.parser.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.constraints.*

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
