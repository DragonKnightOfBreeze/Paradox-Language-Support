package icu.windea.pls.inject.injectors.ext

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.FieldCache
import icu.windea.pls.inject.annotations.InjectionTarget

interface ParadoxScriptPsiCodeInjectors {
    // 用于优化性能

    /** @see icu.windea.pls.script.psi.ParadoxScriptScriptedVariable */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptScriptedVariableImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ScriptedVariable : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptScriptedVariableNameImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class ScriptedVariableName : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptProperty */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class Property : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptPropertyKey */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyKeyImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class PropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptBoolean */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptBooleanImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class Boolean : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInt */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptIntImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class Int : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptFloat */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptFloatImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class Float : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptString */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptStringImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class String : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptColor */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptColorImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getColorType", cleanUp = "subtreeChanged")
    @FieldCache("getColorArgs", cleanUp = "subtreeChanged")
    class Color : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInlineMath */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptInlineMathImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getExpression", cleanUp = "subtreeChanged")
    class InlineMath : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptParameterConditionParameter */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterConditionParameterImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class ParameterConditionParameter : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptParameter */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class Parameter : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptInlineMathParameterImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class InlineMathParameter : CodeInjectorBase()
}
