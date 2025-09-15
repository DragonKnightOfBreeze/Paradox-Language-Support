package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtLocationConfig
import icu.windea.pls.config.config.optionFlags
import icu.windea.pls.config.config.stringValue

internal class CwtLocationConfigResolverImpl : CwtLocationConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtLocationConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocationConfig? {
        // default to optional
        // default to primary for `name` and `title` if it represents a localisation location (by inference)
        // default to primary for `icon` if it represents an image location (by inference)
        val key = config.key
        val expression = config.stringValue ?: return null
        val optionFlags = config.optionFlags
        val required = optionFlags.required
        val optional = optionFlags.optional
        val primary = optionFlags.primary
        return CwtLocationConfigImpl(config, key, expression, required || !optional, primary)
    }
}

private class CwtLocationConfigImpl(
    override val config: CwtPropertyConfig,
    override val key: String,
    override val value: String,
    override val required: Boolean = false,
    override val primary: Boolean = false
) : UserDataHolderBase(), CwtLocationConfig {
    override fun toString() = "CwtLocationConfigImpl(key='$key', value='$value')"
}
