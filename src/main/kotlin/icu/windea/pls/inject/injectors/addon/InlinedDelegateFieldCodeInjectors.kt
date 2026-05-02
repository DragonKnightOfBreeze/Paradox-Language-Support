package icu.windea.pls.inject.injectors.addon

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.inject.annotations.InlinedDelegateFields

interface InlinedDelegateFieldCodeInjectors {
    // 用于减少样板代码，同时不带来额外的内存开销

    /** @see icu.windea.pls.config.option.CwtOptionDataHolderBase */
    @InjectionTarget("icu.windea.pls.config.option.CwtOptionDataHolderBase", pluginId = "icu.windea.pls")
    @InlinedDelegateFields
    class CwtOptionDataHolderBase : CodeInjectorBase()

    /** @see icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase */
    @InjectionTarget("icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase", pluginId = "icu.windea.pls")
    @InlinedDelegateFields
    class CwtConfigGroupDataHolderBase : CodeInjectorBase()
}
