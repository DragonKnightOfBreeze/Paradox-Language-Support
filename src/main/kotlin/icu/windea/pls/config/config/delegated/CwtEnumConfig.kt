package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 简单枚举规则。
 *
 * 用于描述拥有一组固定的可选项（即枚举值）的简单枚举。
 * 其枚举值默认忽略大小写。
 *
 * 路径定位：`enums/enum[{name}]`，`{name}` 匹配规则名称（枚举名）。
 *
 * CWTools 兼容性：部分兼容。PLS 仅支持常量类型（[CwtDataTypes.Constant]）的可选项。
 *
 * 示例：
 * ```cwt
 * enums = {
 *     enum[weight_or_base] = { weight base }
 * }
 * ```
 *
 * @property name 名称（枚举名）。
 * @property values 可选项集合（忽略大小写）。
 * @property valueConfigMap 可选项到对应的值规则的映射。
 */
interface CwtEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("enum[$]")
    val name: String
    @FromProperty("values: template_expression[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        /** 由属性规则解析为简单枚举规则。 */
        fun resolve(config: CwtPropertyConfig): CwtEnumConfig?
    }

    companion object : Resolver by CwtEnumConfigResolverImpl()
}

// region Implementations

private class CwtEnumConfigResolverImpl : CwtEnumConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    // TODO an enum value can also be a template expression

    override fun resolve(config: CwtPropertyConfig): CwtEnumConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtEnumConfig? {
        val key = config.key
        val name = key.removeSurroundingOrNull("enum[", "]")?.orNull()?.optimized() ?: return null
        val valueElements = config.values
        if (valueElements == null) {
            logger.warn("Skipped invalid enum config (name: $name): Null values.".withLocationPrefix(config))
            return null
        }
        if (valueElements.isEmpty()) {
            logger.debug { "Resolved enum config with empty values (name: $name).".withLocationPrefix(config) }
            return CwtEnumConfigImpl(config, name, emptySet(), emptyMap())
        }
        val values = caseInsensitiveStringSet() // ignore case
        val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() // ignore case
        for (valueElement in valueElements) {
            values.add(valueElement.value)
            valueConfigMap.put(valueElement.value, valueElement)
        }
        logger.debug { "Resolved enum config (name: $name).".withLocationPrefix(config) }
        return CwtEnumConfigImpl(config, name, values.optimized(), valueConfigMap.optimized())
    }
}

private class CwtEnumConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtEnumConfig {
    override fun toString() = "CwtEnumConfigImpl(name='$name')"
}

// endregion
