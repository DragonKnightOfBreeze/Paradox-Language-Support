package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.emptyPointer

class CwtDatabaseObjectTypeConfigResolverImpl : CwtDatabaseObjectTypeConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig? {
        val name = config.key
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid database object type config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }
        val propGroup = propElements.groupBy { it.key }
        val type = propGroup.getOne("type")?.stringValue
        val swapType = propGroup.getOne("swap_type")?.stringValue
        val localisation = propGroup.getOne("localisation")?.stringValue
        if (type == null && localisation == null) {
            logger.warn("Skipped invalid database object type config (name: $name): Missing type or localisation property.".withLocationPrefix(config))
            return null
        }
        logger.debug { "Resolved database object type config (name: $name).".withLocationPrefix(config) }
        return CwtDatabaseObjectTypeConfigImpl(config, name, type, swapType, localisation)
    }
}

private class CwtDatabaseObjectTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val type: String?,
    override val swapType: String?,
    override val localisation: String?
) : UserDataHolderBase(), CwtDatabaseObjectTypeConfig {
    override fun getConfigForType(isBase: Boolean): CwtValueConfig? {
        val configExpression = when {
            localisation != null -> "localisation"
            isBase -> type?.let { "<$it>" }
            else -> swapType?.let { "<$it>" }
        }
        if (configExpression == null) return null
        return CwtValueConfig.create(emptyPointer(), this.configGroup, configExpression)
    }

    override fun toString() = "CwtDatabaseObjectTypeConfigImpl(name='$name')"
}
