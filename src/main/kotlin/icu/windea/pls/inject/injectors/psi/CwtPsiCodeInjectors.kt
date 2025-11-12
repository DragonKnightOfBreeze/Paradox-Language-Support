package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectFieldCache
import icu.windea.pls.inject.annotations.InjectTarget

interface CwtPsiCodeInjectors {
    // 用于优化性能

    /** @see icu.windea.pls.cwt.psi.CwtProperty */
    @InjectTarget("icu.windea.pls.cwt.psi.impl.CwtPropertyImpl", pluginId = "icu.windea.pls")
    // @InjectFieldCache("getText", cleanup = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class Property : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtPropertyKey */
    @InjectTarget("icu.windea.pls.cwt.psi.impl.CwtPropertyKeyImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class PropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtOption */
    @InjectTarget("icu.windea.pls.cwt.psi.impl.CwtOptionImpl", pluginId = "icu.windea.pls")
    // @InjectFieldCache("getText", cleanup = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class Option : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtOptionKey */
    @InjectTarget("icu.windea.pls.cwt.psi.impl.CwtOptionKeyImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class OptionKey : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtBoolean */
    @InjectTarget("icu.windea.pls.cwt.psi.impl.CwtBooleanImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    class Boolean : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtInt */
    @InjectTarget("icu.windea.pls.cwt.psi.impl.CwtIntImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    class Int : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtFloat */
    @InjectTarget("icu.windea.pls.cwt.psi.impl.CwtFloatImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    class Float : CodeInjectorBase()

    /** @see icu.windea.pls.cwt.psi.CwtString */
    @InjectTarget("icu.windea.pls.cwt.psi.impl.CwtStringImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class String : CodeInjectorBase()
}
