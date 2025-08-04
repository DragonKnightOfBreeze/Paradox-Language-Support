@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.CwtConfig.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

interface CwtDatabaseObjectTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    @Property("type: string?")
    val type: String?
    @Property("swap_type: string?")
    val swapType: String?
    @Property("localisation: string?")
    val localisation: String?

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig? = doResolve(config)
    }
}

fun CwtDatabaseObjectTypeConfig.getConfigForType(isBase: Boolean): CwtValueConfig? {
    val configExpression = when {
        localisation != null -> "localisation"
        isBase -> type?.let { "<$it>" }
        else -> swapType?.let { "<$it>" }
    }
    if (configExpression == null) return null
    return CwtValueConfig.resolve(emptyPointer(), this.configGroup, configExpression)
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig? {
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

private class CwtDatabaseObjectTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val type: String?,
    override val swapType: String?,
    override val localisation: String?
) : UserDataHolderBase(), CwtDatabaseObjectTypeConfig {
    override fun toString(): String {
        return "CwtDatabaseObjectTypeConfigImpl(name='$name')"
    }
}
