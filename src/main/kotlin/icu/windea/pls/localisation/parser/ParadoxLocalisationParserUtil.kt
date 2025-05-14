package icu.windea.pls.localisation.parser

import com.intellij.lang.*
import com.intellij.lang.parser.*
import icu.windea.pls.model.constraints.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun supportsConceptQuoted(b: PsiBuilder, l: Int): Boolean {
        return ParadoxSyntaxConstraint.LocalisationConceptQuoted.supports(b)
    }

    @JvmStatic
    fun supportsFormatting(b: PsiBuilder, l: Int): Boolean {
        return ParadoxSyntaxConstraint.LocalisationFormatting.supports(b)
    }

    @JvmStatic
    fun supportsTextIcon(b: PsiBuilder, l: Int): Boolean {
        return ParadoxSyntaxConstraint.LocalisationTextIcon.supports(b)
    }
}
