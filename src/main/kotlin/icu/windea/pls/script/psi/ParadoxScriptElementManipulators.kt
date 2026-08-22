package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface ParadoxScriptElementManipulators {
    class PropertyKeyManipulator : AbstractElementManipulator<ParadoxScriptPropertyKey>() {
        override fun handleContentChange(element: ParadoxScriptPropertyKey, range: TextRange, newContent: String): ParadoxScriptPropertyKey {
            return ParadoxScriptPsiManipulationService.changeContent(element, range, newContent)
        }
    }

    class ValueManipulator: AbstractElementManipulator<ParadoxScriptValue>() {
        override fun handleContentChange(element: ParadoxScriptValue, range: TextRange, newContent: String): ParadoxScriptValue {
            return ParadoxScriptPsiManipulationService.changeContent(element, range, newContent)
        }
    }

    class StringManipulator : AbstractElementManipulator<ParadoxScriptString>() {
        override fun handleContentChange(element: ParadoxScriptString, range: TextRange, newContent: String): ParadoxScriptString {
            return ParadoxScriptPsiManipulationService.changeContent(element, range, newContent)
        }
    }

    class ParameterManipulator : AbstractElementManipulator<ParadoxScriptParameter>() {
        override fun handleContentChange(element: ParadoxScriptParameter, range: TextRange, newContent: String): ParadoxScriptParameter {
            return ParadoxScriptPsiManipulationService.changeContent(element, range, newContent)
        }
    }

    class InlineMathParameterManipulator : AbstractElementManipulator<ParadoxScriptInlineMathParameter>() {
        override fun handleContentChange(element: ParadoxScriptInlineMathParameter, range: TextRange, newContent: String): ParadoxScriptInlineMathParameter {
            return ParadoxScriptPsiManipulationService.changeContent(element, range, newContent)
        }
    }
}
