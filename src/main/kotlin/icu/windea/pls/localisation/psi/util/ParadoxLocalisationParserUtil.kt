package icu.windea.pls.localisation.psi.util

import com.intellij.lang.*
import com.intellij.lang.parser.*
import icu.windea.pls.model.constraints.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun supportsConceptQuoted(b: PsiBuilder, l: Int): Boolean {
        return ParadoxSyntaxConstraint.LocalisationConceptQuoted.supports(b)
    }
}
