@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*

class CwtPostfixTemplateSettingsConfig(
    val id: String,
    val key: String,
    val example: String?,
    val variables: Map<String, String>, //variableName - defaultValue
    val expression: String
) : CwtDetachedConfig {
    companion object Resolver {
        fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
            val configs = fileConfig.properties
            configs.forEach { groupProperty ->
                val groupName = groupProperty.key
                val map = caseInsensitiveStringKeyMap<CwtPostfixTemplateSettingsConfig>()
                groupProperty.properties?.forEach { property ->
                    val id = property.key
                    var key: String? = null
                    var example: String? = null
                    var variables: Map<String, String>? = null
                    var expression: String? = null
                    property.properties?.forEach { prop ->
                        when {
                            prop.key == "key" -> key = prop.stringValue
                            prop.key == "example" -> example = prop.stringValue
                            prop.key == "variables" -> variables = prop.properties?.let {
                                buildMap { it.forEach { p -> put(p.key, p.value) } }
                            }
                            prop.key == "expression" -> expression = prop.stringValue
                        }
                    }
                    if (key != null && expression != null) {
                        val foldingSetting = CwtPostfixTemplateSettingsConfig(id, key, example, variables.orEmpty(), expression)
                        map.put(id, foldingSetting)
                    }
                }
                configGroup.postfixTemplateSettings[groupName] = map
            }
        }
    }
}
