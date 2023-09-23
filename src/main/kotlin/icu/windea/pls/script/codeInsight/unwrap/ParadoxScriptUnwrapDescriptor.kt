package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*

class ParadoxScriptUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        ParadoxScriptUnwrappers.ParadoxScriptScriptedVariableRemover("script.remove.scriptedVariable"),
        ParadoxScriptUnwrappers.ParadoxScriptPropertyRemover("script.remove.property"),
        ParadoxScriptUnwrappers.ParadoxScriptValueRemover("script.remove.value"),
        ParadoxScriptUnwrappers.ParadoxScriptParameterConditionRemover("script.remove.parameterCondition"),
        ParadoxScriptUnwrappers.ParadoxScriptInlineParameterConditionRemover("script.remove.inlineParameterCondition"),
    )
    
    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}