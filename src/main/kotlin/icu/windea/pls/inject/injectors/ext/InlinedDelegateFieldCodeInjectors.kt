package icu.windea.pls.inject.injectors.ext

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.inject.annotations.InlinedDelegateFields

interface InlinedDelegateFieldCodeInjectors {
    // 用于优化内存

    /** @see icu.windea.pls.config.option.CwtOptionDataHolderBase */
    @InjectionTarget("icu.windea.pls.config.option.CwtOptionDataHolderBase", pluginId = "icu.windea.pls")
    @InlinedDelegateFields
    class CwtOptionDataHolderBase : CodeInjectorBase()

    /** @see icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase */
    @InjectionTarget("icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase", pluginId = "icu.windea.pls")
    @InlinedDelegateFields
    class CwtConfigGroupDataHolderBase : CodeInjectorBase()
}
