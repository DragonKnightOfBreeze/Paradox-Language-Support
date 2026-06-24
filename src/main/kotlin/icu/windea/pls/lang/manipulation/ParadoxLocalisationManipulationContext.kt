package icu.windea.pls.lang.manipulation

import com.intellij.openapi.util.TextRange
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.core.createPointer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

data class ParadoxLocalisationManipulationContext(
    private val elementPointer: SmartPsiElementPointer<ParadoxLocalisationProperty>,
    val key: String, // KEY
    val prefix: String, // KEY:0
    val text: String, // TEXT
    val textRange: TextRange,
    val needProcess: Boolean,
) {
    val element: ParadoxLocalisationProperty? get() = elementPointer.element

    @Volatile var newText: String = text

    companion object {
        @JvmStatic
        fun create(element: ParadoxLocalisationProperty): ParadoxLocalisationManipulationContext {
            return ParadoxLocalisationManipulationContextBuilder.build(element)
        }
    }
}

// region Implementations

private object ParadoxLocalisationManipulationContextBuilder {
    fun build(element: ParadoxLocalisationProperty): ParadoxLocalisationManipulationContext {
        val name = element.name
        val elementText = element.text
        val i1 = elementText.indexOf('"')
        if (i1 == -1) {
            val prefix = elementText.trimEnd()
            val textRange = TextRange.from(elementText.length, 0)
            return ParadoxLocalisationManipulationContext(element.createPointer(), name, prefix, "", textRange, false)
        }
        val prefix = elementText.substring(0, i1)
        val text = elementText.substring(i1 + 1)
            .let { if (it.lastOrNull() == '"') it.dropLast(1) else it }
        val textRange = TextRange.create(i1, elementText.length)
        val needProcess = ParadoxLocalisationManipulationService.needProcess(element)
        return ParadoxLocalisationManipulationContext(element.createPointer(), name, prefix, text, textRange, needProcess)
    }
}

// endregion
