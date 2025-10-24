package icu.windea.pls.localisation.psi.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter

class ParadoxLocalisationParameterManipulator: AbstractElementManipulator<ParadoxLocalisationParameter>() {
    override fun handleContentChange(element: ParadoxLocalisationParameter, range: TextRange, newContent: String): ParadoxLocalisationParameter? {
        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = ParadoxLocalisationElementFactory.createParameter(element.project, newText)
        return element.replace(newElement).cast()
    }
}
