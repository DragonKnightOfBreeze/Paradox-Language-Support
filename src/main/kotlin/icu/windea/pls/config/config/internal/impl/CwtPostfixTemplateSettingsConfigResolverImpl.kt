package icu.windea.pls.config.config.internal.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.postfixTemplateSettings
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.getOne

internal class CwtPostfixTemplateSettingsConfigResolverImpl : CwtPostfixTemplateSettingsConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolveInFile(configGroupOnInit: CwtConfigGroup, fileConfig: CwtFileConfig) {
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
            configGroupOnInit.postfixTemplateSettings[groupName] = map
        }
    }
}
