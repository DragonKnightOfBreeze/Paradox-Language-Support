package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface ParadoxCsvElementManipulators {
    class Column : AbstractElementManipulator<ParadoxCsvColumn>() {
        override fun handleContentChange(element: ParadoxCsvColumn, range: TextRange, newContent: String): ParadoxCsvColumn {
            return ParadoxCsvPsiManipulationService.changeContent(element, range, newContent)
        }
    }
}
