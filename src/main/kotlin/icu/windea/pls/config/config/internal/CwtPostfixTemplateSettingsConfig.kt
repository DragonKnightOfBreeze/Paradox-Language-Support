package icu.windea.pls.config.config.internal

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.getOne

/**
 * 作为后缀模板设置的内部规则。目前尚不支持自定义。
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
        fun resolveInFile(fileConfig: CwtFileConfig)
    }

    companion object : Resolver by CwtPostfixTemplateSettingsConfigResolverImpl()
}

// region Implementations

private class CwtPostfixTemplateSettingsConfigResolverImpl : CwtPostfixTemplateSettingsConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolveInFile(fileConfig: CwtFileConfig) {
        val initializer = fileConfig.configGroup.initializer
        val configs = fileConfig.properties
        for (groupProperty in configs) {
            val groupName = groupProperty.key
            val map = caseInsensitiveStringKeyMap<CwtPostfixTemplateSettingsConfig>()
            for (property in groupProperty.properties.orEmpty()) {
                val id = property.key
                val propElements = property.properties
                if (propElements.isNullOrEmpty()) {
                    logger.warn("Skipped invalid internal postfix template settings config (id: $id): Missing properties".withLocationPrefix(property))
                    continue
                }
                val propGroup = propElements.groupBy { it.key }
                val key = propGroup.getOne("key")?.stringValue
                val example = propGroup.getOne("example")?.stringValue
                val variables = propGroup.getOne("variables")?.properties?.associateBy({ it.key }, { it.value }).orEmpty()
                val expression = propGroup.getOne("expression")?.stringValue
                if (key == null || expression == null) {
                    logger.warn("Skipped invalid internal postfix template settings config (id: $id): Missing key or expression property".withLocationPrefix(property))
                    continue
                }
                logger.debug { "Resolved internal postfix template settings config (id: $id).".withLocationPrefix(property) }
                val foldingSetting = CwtPostfixTemplateSettingsConfig(id, key, example, variables, expression)
                map.put(id, foldingSetting)
            }
            initializer.postfixTemplateSettings[groupName] = map
        }
    }
}

// endregion
