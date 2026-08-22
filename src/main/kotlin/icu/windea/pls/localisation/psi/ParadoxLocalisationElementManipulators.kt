package icu.windea.pls.localisation.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface ParadoxLocalisationElementManipulators {
    class PropertyKeyManipulator : AbstractElementManipulator<ParadoxLocalisationPropertyKey>() {
        override fun handleContentChange(element: ParadoxLocalisationPropertyKey, range: TextRange, newContent: String): ParadoxLocalisationPropertyKey {
            return ParadoxLocalisationPsiManipulationService.changeContent(element, range, newContent)
        }
    }
}
