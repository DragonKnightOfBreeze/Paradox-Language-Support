package icu.windea.pls.localisation.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*

object ParadoxLocalisationElementManipulators {
    class LocalisationCommandExpression: AbstractElementManipulator<ParadoxLocalisationCommandExpression>() {
        override fun handleContentChange(element: ParadoxLocalisationCommandExpression, range: TextRange, newContent: String): ParadoxLocalisationCommandExpression? {
            return ParadoxLocalisationElementFactory.createCommandExpression(element.project, newContent)
        }
    }
}