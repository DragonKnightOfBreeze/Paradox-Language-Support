package icu.windea.pls.localisation.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface ParadoxLocalisationElementManipulators {
    class ForPropertyKey : AbstractElementManipulator<ParadoxLocalisationPropertyKey>() {
        override fun handleContentChange(element: ParadoxLocalisationPropertyKey, range: TextRange, newContent: String): ParadoxLocalisationPropertyKey {
            return ParadoxLocalisationElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForCommandText : AbstractElementManipulator<ParadoxLocalisationCommandText>() {
        override fun handleContentChange(element: ParadoxLocalisationCommandText, range: TextRange, newContent: String): ParadoxLocalisationCommandText {
            return ParadoxLocalisationElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForConceptName : AbstractElementManipulator<ParadoxLocalisationConceptName>() {
        override fun handleContentChange(element: ParadoxLocalisationConceptName, range: TextRange, newContent: String): ParadoxLocalisationConceptName {
            return ParadoxLocalisationElementManipulationService.changeContent(element, newContent, range)
        }
    }
}
