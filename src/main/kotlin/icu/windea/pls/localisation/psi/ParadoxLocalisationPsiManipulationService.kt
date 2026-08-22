package icu.windea.pls.localisation.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast

object ParadoxLocalisationPsiManipulationService {
    fun changeContent(element: ParadoxLocalisationPropertyKey, range: TextRange, newContent: String): ParadoxLocalisationPropertyKey {
        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = ParadoxLocalisationElementFactory.createPropertyKey(element.project, newText)
        return element.replace(newElement).cast()
    }
}
