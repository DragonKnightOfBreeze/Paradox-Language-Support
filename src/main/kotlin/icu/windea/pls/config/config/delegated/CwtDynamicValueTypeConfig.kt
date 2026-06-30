package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 动态值类型规则。
 *
 * 用于为对应的动态值类型提供一组可选项，作为预定义的动态值。
 * 这里预定义的动态值必须是常量，且不会忽略大小写。
 *
 * 动态值是一组不固定的可选项，通常是合法的标识符，使用同名本地化的文本作为 UI 显示。
 * 事件目标（event target）、变量（variable）、标志（flag）等通常都会被视为动态值。
 *
 * 路径定位：
 * - `values/value[{name}]`。其中 `{name}` 匹配规则名称。
 *
 * 示例：
 *
 * ```cwt
 * values = {
 *     value[event_target] = { owner capital }  # case-insensitive
 * }
 * ```
 *
 * > CWTools 兼容性：部分兼容。拥有不同的解析和处理逻辑。
 *
 * @property name 规则名称（即动态值类型）。
 * @property values 可选项集合（不忽略大小写）。
 * @property valueConfigMap 可选项到对应的值规则的映射。
 */
interface CwtDynamicValueTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty> {
    @FromName("value[$]")
    val name: String
    @FromMember("values: template_expression[]")
    val values: Set<String>

    val valueConfigMap: Map<String, CwtValueConfig>

    companion object {
        /** 由属性规则解析为动态值类型规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig? {
            return CwtDynamicValueTypeConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtDynamicValueTypeConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig? {
        val key = config.key
        val name = key.removeSurroundingOrNull("value[", "]")?.orNull()?.optimized() ?: return null
        val valueConfigs = config.values
        if (valueConfigs == null) {
            logger.warn("Skipped invalid dynamic value type config (name: $name): Null values.".withLocationPrefix(config))
            return null
        }
        if (valueConfigs.isEmpty()) {
            logger.debug { "Resolved dynamic value type config with empty values (name: $name).".withLocationPrefix(config) }
            return CwtDynamicValueTypeConfigImpl(config, name, emptySet(), emptyMap())
        }
        val values = mutableSetOf<String>()
        val valueConfigMap = mutableMapOf<String, CwtValueConfig>()
        for (propertyConfigValue in valueConfigs) {
            val v = propertyConfigValue.value.optimized()
            values.add(v)
            valueConfigMap.put(v, propertyConfigValue)
        }
        logger.debug { "Resolved dynamic value type config (name: $name).".withLocationPrefix(config) }
        return CwtDynamicValueTypeConfigImpl(config, name, values.optimized(), valueConfigMap.optimized())
    }
}

private class CwtDynamicValueTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtDynamicValueTypeConfig {
    override fun toString() = "CwtDynamicValueTypeConfigImpl(name='$name')"
}

// endregion
