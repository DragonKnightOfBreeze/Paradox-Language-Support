package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*

class ParadoxScriptUnwrapDescriptor : UnwrapDescriptorBase(){ 
    companion object {
        private val _unwrappers = arrayOf(
            ParadoxScriptUnwrappers.ParadoxScriptScriptedVariableRemover("script.remove.scriptedVariable"),
            ParadoxScriptUnwrappers.ParadoxScriptPropertyRemover("script.remove.property"),
            ParadoxScriptUnwrappers.ParadoxScriptValueRemover("script.remove.value"),
            ParadoxScriptUnwrappers.ParadoxScriptParameterConditionRemover("script.remove.parameterCondition"),
        )
    }
    
    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}