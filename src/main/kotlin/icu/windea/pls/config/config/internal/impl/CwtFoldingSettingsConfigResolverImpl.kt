package icu.windea.pls.config.config.internal.impl

import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.foldingSettings
import icu.windea.pls.core.caseInsensitiveStringKeyMap

internal class CwtFoldingSettingsConfigResolverImpl : CwtFoldingSettingsConfig.Resolver {
    override fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties
        configs.forEach { groupProperty ->
            val groupName = groupProperty.key
            val map = caseInsensitiveStringKeyMap<CwtFoldingSettingsConfig>()
            groupProperty.properties?.forEach { property ->
                val id = property.key
                var key: String? = null
                var keys: List<String>? = null
                var placeholder: String? = null
                property.properties?.forEach { prop ->
                    when {
                        prop.key == "key" -> key = prop.stringValue
                        prop.key == "keys" -> keys = prop.values?.mapNotNull { it.stringValue }
                        prop.key == "placeholder" -> placeholder = prop.stringValue
                    }
                }
                if (placeholder != null) {
                    val foldingSetting = CwtFoldingSettingsConfig(id, key, keys, placeholder)
                    map.put(id, foldingSetting)
                }
            }
            configGroup.foldingSettings[groupName] = map
        }
    }
}

