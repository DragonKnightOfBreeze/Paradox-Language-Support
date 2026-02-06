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
 * 作为折叠设置的内部规则。目前尚不支持自定义。
 *
 * 用于提供额外的代码折叠。
 *
 * @see icu.windea.pls.lang.folding.ParadoxExpressionFoldingBuilder
 */
data class CwtFoldingSettingsConfig(
    val id: String,
    val key: String?,
    val keys: List<String>?,
    val placeholder: String
) : CwtDetachedConfig {
    interface Resolver {
        fun resolveInFile(fileConfig: CwtFileConfig)
    }

    companion object : Resolver by CwtFoldingSettingsConfigResolverImpl()
}

// region Implementations

private class CwtFoldingSettingsConfigResolverImpl : CwtFoldingSettingsConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolveInFile(fileConfig: CwtFileConfig) {
        val initializer = fileConfig.configGroup.initializer
        val configs = fileConfig.properties
        for (groupProperty in configs) {
            val groupName = groupProperty.key
            val map = caseInsensitiveStringKeyMap<CwtFoldingSettingsConfig>()
            for (property in groupProperty.properties.orEmpty()) {
                val id = property.key
                val propElements = property.properties
                if (propElements.isNullOrEmpty()) {
                    logger.warn("Skipped invalid internal folding settings config (id: $id): Missing properties".withLocationPrefix(property))
                    continue
                }
                val propGroup = propElements.groupBy { it.key }
                val key = propGroup.getOne("key")?.stringValue
                val keys = propGroup.getOne("keys")?.values?.mapNotNull { it.stringValue }
                val placeholder = propGroup.getOne("placeholder")?.stringValue
                if (placeholder == null) {
                    logger.warn("Skipped invalid internal folding settings config (id: $id): Missing placeholder property".withLocationPrefix(property))
                    continue
                }
                logger.debug { "Resolved internal folding settings config (id: $id).".withLocationPrefix(property) }
                val foldingSetting = CwtFoldingSettingsConfig(id, key, keys, placeholder)
                map.put(id, foldingSetting)
            }
            initializer.foldingSettings[groupName] = map
        }
    }
}

// endregion
