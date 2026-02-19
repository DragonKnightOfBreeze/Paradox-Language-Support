package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapDescriptorBase
import com.intellij.codeInsight.unwrap.Unwrapper

// com.intellij.codeInsight.unwrap.JavaUnwrapDescriptor
// org.jetbrains.kotlin.idea.codeInsight.unwrap.KotlinUnwrapDescriptor

class ParadoxScriptUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        ParadoxScriptScriptedVariableRemover(),
        ParadoxScriptPropertyRemover(),
        ParadoxScriptValueRemover(),
        ParadoxScriptParameterConditionRemover(),
        ParadoxScriptInlineParameterConditionRemover(),
        ParadoxScriptPropertyUnwrapper(),
        ParadoxScriptBlockUnwrapper(),
        ParadoxScriptParameterConditionUnwrapper(),
        ParadoxScriptInlineParameterConditionUnwrapper(),
    )

    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}
