package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.impl.CwtPostfixTemplateSettingsConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 作为后缀模板设置的内部规则，目前尚不支持自定义。
 *
 * 用于提供额外的后缀补全，
 *
 * @see icu.windea.pls.lang.codeInsight.template.postfix.ParadoxExpressionEditablePostfixTemplate
 */
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
