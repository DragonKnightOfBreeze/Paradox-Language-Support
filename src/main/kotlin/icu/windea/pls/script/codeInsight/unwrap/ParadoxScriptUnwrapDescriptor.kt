package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*

class ParadoxScriptUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        ParadoxScriptUnwrappers.ParadoxScriptScriptedVariableRemover("script.remove.scriptedVariable"),
        ParadoxScriptUnwrappers.ParadoxScriptPropertyRemover("script.remove.property"),
        ParadoxScriptUnwrappers.ParadoxScriptValueRemover("script.remove.value"),
        ParadoxScriptUnwrappers.ParadoxScriptBlockRemover("script.remove.block"),
        ParadoxScriptUnwrappers.ParadoxScriptParameterConditionRemover("script.remove.parameterCondition"),
        ParadoxScriptUnwrappers.ParadoxScriptPropertyUnwrapper("script.unwrap.property"),
        ParadoxScriptUnwrappers.ParadoxScriptBlockUnwrapper("script.unwrap.block"),
        ParadoxScriptUnwrappers.ParadoxScriptParameterConditionUnwrapper("script.unwrap.parameterCondition"),
        ParadoxScriptUnwrappers.ParadoxScriptInlineParameterConditionUnwrapper("script.unwrap.inlineParameterCondition"),
    )
    
    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}