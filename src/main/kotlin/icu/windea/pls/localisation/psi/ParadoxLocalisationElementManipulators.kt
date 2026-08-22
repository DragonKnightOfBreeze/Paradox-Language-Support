package icu.windea.pls.localisation.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface ParadoxLocalisationElementManipulators {
    class PropertyKeyManipulator : AbstractElementManipulator<ParadoxLocalisationPropertyKey>() {
        override fun handleContentChange(element: ParadoxLocalisationPropertyKey, range: TextRange, newContent: String): ParadoxLocalisationPropertyKey {
            return ParadoxLocalisationPsiManipulationService.changeContent(element, newContent, range)
        }
    }

    class CommandTextManipulator : AbstractElementManipulator<ParadoxLocalisationCommandText>() {
        override fun handleContentChange(element: ParadoxLocalisationCommandText, range: TextRange, newContent: String): ParadoxLocalisationCommandText {
            return ParadoxLocalisationPsiManipulationService.changeContent(element, newContent, range)
        }
    }

    class ConceptNameManipulator : AbstractElementManipulator<ParadoxLocalisationConceptName>() {
        override fun handleContentChange(element: ParadoxLocalisationConceptName, range: TextRange, newContent: String): ParadoxLocalisationConceptName {
            return ParadoxLocalisationPsiManipulationService.changeContent(element, newContent, range)
        }
    }
}
