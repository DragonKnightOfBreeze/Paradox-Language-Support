package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

/**
 * @property key (key)
 * @property value (value)
 * @property required (option) required
 * @property primary (option) primary
 */
interface CwtLocationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val key: String
    val value: String
    val required: Boolean
    val primary: Boolean
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtLocationConfig? = doResolve(config)
    }
}

//Implementations (interned)

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
    override val primary: Boolean = false,
) : CwtLocationConfig
