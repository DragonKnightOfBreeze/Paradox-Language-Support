package icu.windea.pls.localisation.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementDescriptionProvider
import icu.windea.pls.core.cast

/**
 * @see ElementDescriptionProvider
 */
object ParadoxLocalisationPsiManipulationService {
    fun changeContent(element: ParadoxLocalisationPropertyKey, newContent: String, range: TextRange? = null): ParadoxLocalisationPropertyKey {
        val newText = range?.replace(element.text, newContent) ?: newContent
        val newElement = ParadoxLocalisationElementFactory.createPropertyKey(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxLocalisationCommandText, newContent: String,range: TextRange? = null): ParadoxLocalisationCommandText {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue
        val newElement = ParadoxLocalisationElementFactory.createCommandText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: ParadoxLocalisationConceptName, newContent: String,range: TextRange? = null): ParadoxLocalisationConceptName {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = newValue
        val newElement = ParadoxLocalisationElementFactory.createConceptName(element.project, newText)
        return element.replace(newElement).cast()
    }
}
