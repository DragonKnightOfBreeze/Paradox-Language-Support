package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtExpandableConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 并集规则。
 *
 * 用于提供一组数据表达式的候选项，以进行并集匹配，匹配时会递归展开并依次尝试其中的候选项。
 * 不同于枚举规则，这里的可选项可以是各种数据类型的数据表达式。
 *
 * 路径定位：
 * - `unions/union[{name}]`。其中 `{name}` 匹配规则名称。
 *
 * 示例：
 *
 * ```cwt
 * unions = {
 *     union[loc_or_text] = { localisation scalar }
 * }
 * ```
 *
 * > CWTools 兼容性：不兼容，插件作为扩展提供。
 *
 * @property name 规则名称。
 * @property valueConfigs 对应的值规则的列表。
 */
interface CwtUnionConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty>, CwtExpandableConfig<CwtProperty> {
    @FromName("union[$]")
    val name: String

    val valueConfigs: List<CwtValueConfig>

    companion object {
        /** 由属性规则解析为并集规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtUnionConfig? {
            return CwtUnionConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtUnionConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtUnionConfig? {
        val key = config.key
        val name = key.removeSurroundingOrNull("union[", "]")?.orNull()?.optimized() ?: return null
        val valueConfigs = config.values
        if (valueConfigs == null) {
            logger.warn("Skipped invalid union config (name: $name): Null values.".withLocationPrefix(config))
            return null
        }
        if (valueConfigs.isEmpty()) {
            logger.debug { "Resolved union config with empty values (name: $name).".withLocationPrefix(config) }
            return CwtUnionConfigImpl(config, name, emptyList())
        }
        logger.debug { "Resolved union config (name: $name).".withLocationPrefix(config) }
        return CwtUnionConfigImpl(config, name, valueConfigs.optimized())
    }
}

private class CwtUnionConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val valueConfigs: List<CwtValueConfig>
) : UserDataHolderBase(), CwtUnionConfig {
    override fun toString() = "CwtUnionConfigImpl(name='$name')"
}

// endregion
