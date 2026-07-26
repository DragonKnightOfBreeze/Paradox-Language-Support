package icu.windea.pls.inject.injectors.addon

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.inject.annotations.InlinedDelegateFields

interface InlinedDelegateFieldCodeInjectors {
    // 用于减少样板代码，同时不带来额外的内存开销

    /** @see icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase */
    @InjectionTarget("icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase", pluginId = "icu.windea.pls")
    @InlinedDelegateFields
    class CwtConfigGroupDataHolderBase : CodeInjectorBase()

    /** @see icu.windea.pls.config.option.CwtOptionMetadataBase */
    @InjectionTarget("icu.windea.pls.config.option.CwtOptionMetadataBase", pluginId = "icu.windea.pls")
    @InlinedDelegateFields
    class CwtOptionMetadataBase : CodeInjectorBase()

    /** @see icu.windea.pls.config.configExpression.CwtDataExpressionMetadataBase */
    @InjectionTarget("icu.windea.pls.config.configExpression.CwtDataExpressionMetadataBase", pluginId = "icu.windea.pls")
    @InlinedDelegateFields
    class CwtDataExpressionMetadataBase : CodeInjectorBase()
}
