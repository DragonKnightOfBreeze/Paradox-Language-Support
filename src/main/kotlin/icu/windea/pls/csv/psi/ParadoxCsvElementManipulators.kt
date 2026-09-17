package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface ParadoxCsvElementManipulators {
    class ForColumn : AbstractElementManipulator<ParadoxCsvColumn>() {
        override fun handleContentChange(element: ParadoxCsvColumn, range: TextRange, newContent: String): ParadoxCsvColumn {
            return ParadoxCsvElementManipulationService.changeContent(element, newContent, range)
        }
    }
}
