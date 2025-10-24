package icu.windea.pls.localisation.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey

class ParadoxLocalisationPropertyKeyManipulator : AbstractElementManipulator<ParadoxLocalisationPropertyKey>() {
    override fun handleContentChange(element: ParadoxLocalisationPropertyKey, range: TextRange, newContent: String): ParadoxLocalisationPropertyKey {
        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = ParadoxLocalisationElementFactory.createPropertyKey(element.project, newText)
        return element.replace(newElement).cast()
    }
}

