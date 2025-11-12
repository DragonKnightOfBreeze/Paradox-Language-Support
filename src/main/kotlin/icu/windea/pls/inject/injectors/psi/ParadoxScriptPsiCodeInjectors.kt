package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectFieldCache
import icu.windea.pls.inject.annotations.InjectTarget

interface ParadoxScriptPsiCodeInjectors {
    // 用于优化性能

    /** @see icu.windea.pls.script.psi.ParadoxScriptScriptedVariable */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptScriptedVariableImpl", pluginId = "icu.windea.pls")
    // @InjectFieldCache("getText", cleanup = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class ScriptedVariable : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptScriptedVariableNameImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class ScriptedVariableName : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptProperty */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl", pluginId = "icu.windea.pls")
    // @InjectFieldCache("getText", cleanup = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class Property : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptPropertyKey */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyKeyImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class PropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptBoolean */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptBooleanImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    class Boolean : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInt */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptIntImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    class Int : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptFloat */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptFloatImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    class Float : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptString */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptStringImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class String : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptColor */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptColorImpl", pluginId = "icu.windea.pls")
    // @InjectFieldCache("getText", cleanup = "subtreeChanged")
    @InjectFieldCache("getColorType", cleanUp = "subtreeChanged")
    @InjectFieldCache("getColorArgs", cleanUp = "subtreeChanged")
    class Color : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptParameterConditionParameter */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterConditionParameterImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class ParameterConditionParameter : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptParameter */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class Parameter : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptInlineMathParameterImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class InlineMathParameter : CodeInjectorBase()
}
