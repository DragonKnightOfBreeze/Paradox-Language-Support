package icu.windea.pls.config.config.delegated

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 位置规则。
 *
 * 用于定位目标资源（图片、本地化等）的来源。
 * 具体而言，通过位置表达式（[CwtLocationExpression]）进行定位。
 *
 * 路径定位：
 * 1. 本地化资源：`types/type[{type}]/localisation/{key}`，`{type}` 匹配定义类型，`{key}` 匹配键名。
 * 2. 图片资源：`types/type[{type}]/images/{key}`，`{type}` 匹配定义类型，`{key}` 匹配键名。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * types = {
 *     type[army] = {
 *         # ...
 *         images = {
 *             ## primary
 *             icon = icon # <sprite>
 *         }
 *     }
 * }
 * ```
 *
 * @property key 资源的名字。
 * @property value 资源的位置表达式（[CwtLocationExpression]）。
 * @property required 是否是必需项。
 * @property primary 是否是主要项。
 *
 * @see CwtTypeImagesConfig
 * @see CwtTypeLocalisationConfig
 * @see CwtLocationExpression
 */
interface CwtLocationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val key: String
    @FromProperty(": string")
    val value: String
    @FromOption("required")
    val required: Boolean
    @FromOption("primary")
    val primary: Boolean

    interface Resolver {
        /** 由属性规则解析为位置规则。 */
        fun resolve(config: CwtPropertyConfig): CwtLocationConfig?
    }

    companion object : Resolver by CwtLocationConfigResolverImpl()
}

// region Implementations

private class CwtLocationConfigResolverImpl : CwtLocationConfig.Resolver, CwtConfigResolverScope {
    // no logger here (unnecessary)

    override fun resolve(config: CwtPropertyConfig): CwtLocationConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocationConfig? {
        // default to optional
        // default to primary for `name` and `title` if it represents a localisation location (by inference)
        // default to primary for `icon` if it represents an image location (by inference)
        val key = config.key
        val expression = config.stringValue ?: return null
        val required = config.optionData.required
        val primary = config.optionData.primary
        return CwtLocationConfigImpl(config, key, expression, required, primary)
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

// endregion
