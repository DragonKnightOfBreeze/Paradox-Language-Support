package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

interface ParadoxScriptPsiCodeInjectors {
    //用于优化性能

    /** @see icu.windea.pls.script.psi.ParadoxScriptProperty */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getValue", cleanup = "subtreeChanged")
    class Property : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptPropertyKey */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyKeyImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getValue", cleanup = "subtreeChanged")
    class PropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptBoolean */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptBooleanImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    class Boolean : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInt */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptIntImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    class Int : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptFloat */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptFloatImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    class Float : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptString */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptStringImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getValue", cleanup = "subtreeChanged")
    class String : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptColor */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptColorImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getColorType", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getColorArgs", cleanup = "subtreeChanged")
    class Color : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptParameterConditionParameter */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterConditionParameterImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    class ParameterConditionParameter : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptParameter */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    class Parameter : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter */
    @InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptInlineMathParameterImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    class InlineMathParameter : CodeInjectorBase()
}
