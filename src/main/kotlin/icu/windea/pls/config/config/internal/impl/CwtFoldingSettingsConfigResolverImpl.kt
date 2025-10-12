package icu.windea.pls.config.config.internal.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.foldingSettings
import icu.windea.pls.config.util.CwtConfigResolverUtil.withLocationPrefix
import icu.windea.pls.core.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.getOne

internal class CwtFoldingSettingsConfigResolverImpl : CwtFoldingSettingsConfig.Resolver {
    private val logger = thisLogger()

    override fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
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
            configGroup.foldingSettings[groupName] = map
        }
    }
}

