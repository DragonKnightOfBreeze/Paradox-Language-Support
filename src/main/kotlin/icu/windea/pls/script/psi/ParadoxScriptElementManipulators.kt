package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

interface ParadoxScriptElementManipulators {
    class ForPropertyKey : AbstractElementManipulator<ParadoxScriptPropertyKey>() {
        override fun handleContentChange(element: ParadoxScriptPropertyKey, range: TextRange, newContent: String): ParadoxScriptPropertyKey {
            return ParadoxScriptElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForValue : AbstractElementManipulator<ParadoxScriptValue>() {
        override fun handleContentChange(element: ParadoxScriptValue, range: TextRange, newContent: String): ParadoxScriptValue {
            return ParadoxScriptElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForString : AbstractElementManipulator<ParadoxScriptString>() {
        override fun handleContentChange(element: ParadoxScriptString, range: TextRange, newContent: String): ParadoxScriptString {
            return ParadoxScriptElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForNormalParameterArgument : AbstractElementManipulator<ParadoxScriptNormalParameterArgument>() {
        override fun handleContentChange(element: ParadoxScriptNormalParameterArgument, range: TextRange, newContent: String): ParadoxScriptNormalParameterArgument {
            return ParadoxScriptElementManipulationService.changeContent(element, newContent, range)
        }
    }

    class ForInlineMathParameterArgument : AbstractElementManipulator<ParadoxScriptInlineMathParameterArgument>() {
        override fun handleContentChange(element: ParadoxScriptInlineMathParameterArgument, range: TextRange, newContent: String): ParadoxScriptInlineMathParameterArgument {
            return ParadoxScriptElementManipulationService.changeContent(element, newContent, range)
        }
    }
}
