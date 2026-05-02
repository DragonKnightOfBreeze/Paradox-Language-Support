package icu.windea.pls.lang.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

data class ParadoxLocalisationManipulationContext(
    private val elementPointer: SmartPsiElementPointer<ParadoxLocalisationProperty>,
    val key: String, // KEY
    val prefix: String, // KEY:0
    val text: String, // TEXT
    val textRange: TextRange,
    val shouldHandle: Boolean,
) {
    val element: ParadoxLocalisationProperty? get() = elementPointer.element

    @Volatile
    var newText: String = text
}
