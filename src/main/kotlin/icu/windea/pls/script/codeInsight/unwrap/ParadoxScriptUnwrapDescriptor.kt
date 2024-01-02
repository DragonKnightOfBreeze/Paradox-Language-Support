package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*

class ParadoxScriptUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        ParadoxScriptScriptedVariableRemover(),
        ParadoxScriptPropertyRemover(),
        ParadoxScriptValueRemover(),
        ParadoxScriptBlockRemover(),
        ParadoxScriptParameterConditionRemover(),
        ParadoxScriptPropertyUnwrapper(),
        ParadoxScriptBlockUnwrapper(),
        ParadoxScriptParameterConditionUnwrapper(),
        ParadoxScriptInlineParameterConditionUnwrapper(),
    )
    
    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}