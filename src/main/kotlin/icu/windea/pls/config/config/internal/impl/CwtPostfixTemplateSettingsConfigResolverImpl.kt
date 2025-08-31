package icu.windea.pls.config.config.internal.impl

import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.postfixTemplateSettings
import icu.windea.pls.core.caseInsensitiveStringKeyMap

internal class CwtPostfixTemplateSettingsConfigResolverImpl : CwtPostfixTemplateSettingsConfig.Resolver {
    override fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
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
