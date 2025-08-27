@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtConfig.Option
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtLocationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val key: String
    val value: String
    @Option("required")
    val required: Boolean
    @Option("primary")
    val primary: Boolean

    companion object {
        fun resolve(config: CwtPropertyConfig): CwtLocationConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtLocationConfig? {
    //default to optional
    //default to primary for name and title if it represents a localisation location (by inference)
    //default to primary for icon if it represents an image location (by inference)
    val key = config.key
    val expression = config.stringValue ?: return null
    val required = config.findOptionValue("required") != null
    val optional = config.findOptionValue("optional") != null
    val primary = config.findOptionValue("primary") != null
    return CwtLocationConfigImpl(config, key, expression, required || !optional, primary)
}

private class CwtLocationConfigImpl(
    override val config: CwtPropertyConfig,
    override val key: String,
    override val value: String,
    override val required: Boolean = false,
    override val primary: Boolean = false
) : UserDataHolderBase(), CwtLocationConfig {
    override fun toString(): String {
        return "CwtLocationConfigImpl(key='$key', value='$value')"
    }
}
