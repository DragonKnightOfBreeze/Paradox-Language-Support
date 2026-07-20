package icu.windea.pls.config.config.internal

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.collections.CaseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.lang.folding.ParadoxExpressionFoldingBuilder

/**
 * 作为折叠设置的内部规则。目前尚不支持自定义。
 *
 * 用于提供额外的代码折叠。
 *
 * @see ParadoxExpressionFoldingBuilder
 */
data class CwtFoldingSettingsConfig(
    val id: String,
    val key: String?,
    val keys: List<String>?,
    val placeholder: String
) : CwtDetachedConfig {
    companion object {
        @JvmStatic
        fun resolveInFile(fileConfig: CwtFileConfig) {
            return CwtFoldingSettingsConfigResolver.resolveInFile(fileConfig)
        }
    }
}

// region Implementations

private object CwtFoldingSettingsConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolveInFile(fileConfig: CwtFileConfig) {
        val initializer = fileConfig.configGroup.initializer
        val configs = fileConfig.properties
        for (groupProperty in configs) {
            val groupName = groupProperty.key
            val map = CaseInsensitiveStringKeyMap<CwtFoldingSettingsConfig>()
            for (property in groupProperty.properties.orEmpty()) {
                val id = property.key
                val propConfigs = property.properties
                if (propConfigs.isNullOrEmpty()) {
                    logger.warn("Skipped invalid internal folding settings config (id: $id): Missing properties".withLocationPrefix(property))
                    continue
                }
                val propGroup = propConfigs.groupBy { it.key }
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
