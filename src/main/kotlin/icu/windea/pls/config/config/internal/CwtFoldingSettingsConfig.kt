@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*

class CwtFoldingSettingsConfig(
    val id: String,
    val key: String?,
    val keys: List<String>?,
    val placeholder: String
) : CwtDetachedConfig {
    companion object Resolver {
        fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
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
                        val foldingSetting = CwtFoldingSettingsConfig(id, key, keys, placeholder!!)
                        map.put(id, foldingSetting)
                    }
                }
                configGroup.foldingSettings[groupName] = map
            }
        }
    }
}

