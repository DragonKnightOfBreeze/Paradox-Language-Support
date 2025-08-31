package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.impl.CwtPostfixTemplateSettingsConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroup

data class CwtPostfixTemplateSettingsConfig(
    val id: String,
    val key: String,
    val example: String?,
    val variables: Map<String, String>, // variableName - defaultValue
    val expression: String
) : CwtDetachedConfig {
    interface Resolver {
        fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup)
    }

    companion object : Resolver by CwtPostfixTemplateSettingsConfigResolverImpl()
}
