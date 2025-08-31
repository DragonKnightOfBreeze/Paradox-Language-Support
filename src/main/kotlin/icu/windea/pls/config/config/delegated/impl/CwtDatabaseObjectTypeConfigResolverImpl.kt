package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.resolve
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.emptyPointer

class CwtDatabaseObjectTypeConfigResolverImpl : CwtDatabaseObjectTypeConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfigImpl? {
        val name = config.key
        var type: String? = null
        var swapType: String? = null
        var localisation: String? = null
        val props = config.properties
        if (props.isNullOrEmpty()) return null
        for (prop in props) {
            when (prop.key) {
                "type" -> type = prop.stringValue
                "swap_type" -> swapType = prop.stringValue
                "localisation" -> localisation = prop.stringValue
            }
        }
        if (type == null && localisation == null) return null
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
        return CwtValueConfig.resolve(emptyPointer(), this.configGroup, configExpression)
    }

    override fun toString() = "CwtDatabaseObjectTypeConfigImpl(name='$name')"
}
