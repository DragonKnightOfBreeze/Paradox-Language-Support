package icu.windea.pls.inject.injectors.opt

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
    class ForScriptedVariable : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptScriptedVariableNameImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class ForScriptedVariableName : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptProperty */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ForProperty : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptPropertyKey */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyKeyImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ForPropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptBoolean */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptBooleanImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class ForBoolean : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInt */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptIntImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class ForInt : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptFloat */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptFloatImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class ForFloat : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptString */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptStringImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ForString : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptConditionalParameter */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptConditionalParameterImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class ForConditionalParameter : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptNormalParameter */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptNormalParameterImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class ForNormalParameter : CodeInjectorBase()

    /** @see icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter */
    @InjectionTarget("icu.windea.pls.script.psi.impl.ParadoxScriptInlineMathParameterImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class ForInlineMathParameter : CodeInjectorBase()
}
