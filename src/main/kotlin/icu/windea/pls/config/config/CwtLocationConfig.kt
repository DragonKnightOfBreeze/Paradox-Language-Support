package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property key (property key)
 * @property value (property value)
 * @property required (option) required
 * @property primary (option) primary
 */
class CwtLocationConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val key: String,
    val value: String,
    val required: Boolean = false,
    val primary: Boolean = false,
) : CwtConfig<CwtProperty> {
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtLocationConfig? {
            //default to optional
            //default to primary for name and title if it represents a localisation location (by inference)
            //default to primary for icon if it represents an image location (by inference)
            val expression = config.stringValue ?: return null
            val required = config.findOptionValue("required") != null
            val optional = config.findOptionValue("optional") != null
            val primary = config.findOptionValue("primary") != null
            return CwtLocationConfig(config.pointer, config.info, name, expression, required || !optional, primary)
        }
    }
}
