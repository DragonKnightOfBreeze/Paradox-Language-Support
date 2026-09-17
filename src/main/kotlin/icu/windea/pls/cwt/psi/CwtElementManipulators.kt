package icu.windea.pls.cwt.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface CwtElementManipulators {
    class ForOptionKey : AbstractElementManipulator<CwtOptionKey>() {
        override fun handleContentChange(element: CwtOptionKey, range: TextRange, newContent: String): CwtOptionKey {
            return CwtElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForPropertyKey : AbstractElementManipulator<CwtPropertyKey>() {
        override fun handleContentChange(element: CwtPropertyKey, range: TextRange, newContent: String): CwtPropertyKey {
            return CwtElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForValue : AbstractElementManipulator<CwtValue>() {
        override fun handleContentChange(element: CwtValue, range: TextRange, newContent: String): CwtValue {
            return CwtElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForString : AbstractElementManipulator<CwtString>() {
        override fun handleContentChange(element: CwtString, range: TextRange, newContent: String): CwtString {
            return CwtElementManipulationService.changeContent(element, newContent, range)
        }
    }
}
