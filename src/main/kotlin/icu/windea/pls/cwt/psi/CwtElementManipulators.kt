package icu.windea.pls.cwt.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface CwtElementManipulators {
    class OptionKeyManipulator : AbstractElementManipulator<CwtOptionKey>() {
        override fun handleContentChange(element: CwtOptionKey, range: TextRange, newContent: String): CwtOptionKey {
            return CwtPsiManipulationService.changeContent(element, newContent, range)
        }
    }

    class PropertyKeyManipulator : AbstractElementManipulator<CwtPropertyKey>() {
        override fun handleContentChange(element: CwtPropertyKey, range: TextRange, newContent: String): CwtPropertyKey {
            return CwtPsiManipulationService.changeContent(element, newContent, range)
        }
    }

    class ValueManipulator : AbstractElementManipulator<CwtValue>() {
        override fun handleContentChange(element: CwtValue, range: TextRange, newContent: String): CwtValue {
            return CwtPsiManipulationService.changeContent(element, newContent, range)
        }
    }

    class StringManipulator : AbstractElementManipulator<CwtString>() {
        override fun handleContentChange(element: CwtString, range: TextRange, newContent: String): CwtString {
            return CwtPsiManipulationService.changeContent(element, newContent, range)
        }
    }
}
