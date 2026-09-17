package icu.windea.pls.inject.injectors.opt

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.FieldCache
import icu.windea.pls.inject.annotations.InjectionTarget

interface CwtPsiCodeInjectors {
    // 用于优化性能

    /** @see icu.windea.pls.cwt.psi.CwtOption */
    @InjectionTarget("icu.windea.pls.cwt.psi.impl.CwtOptionImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ForOption : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtOptionKey */
    @InjectionTarget("icu.windea.pls.cwt.psi.impl.CwtOptionKeyImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ForOptionKey : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtProperty */
    @InjectionTarget("icu.windea.pls.cwt.psi.impl.CwtPropertyImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ForProperty : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtPropertyKey */
    @InjectionTarget("icu.windea.pls.cwt.psi.impl.CwtPropertyKeyImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ForPropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtBoolean */
    @InjectionTarget("icu.windea.pls.cwt.psi.impl.CwtBooleanImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class ForBoolean : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtInt */
    @InjectionTarget("icu.windea.pls.cwt.psi.impl.CwtIntImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class ForInt : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtFloat */
    @InjectionTarget("icu.windea.pls.cwt.psi.impl.CwtFloatImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class ForFloat : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtString */
    @InjectionTarget("icu.windea.pls.cwt.psi.impl.CwtStringImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class ForString : CodeInjectorBase()
}
