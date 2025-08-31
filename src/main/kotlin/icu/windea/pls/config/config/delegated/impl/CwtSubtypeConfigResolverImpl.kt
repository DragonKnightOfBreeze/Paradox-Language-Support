package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.getOptionValueOrValues
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.model.CwtSeparatorType

internal class CwtSubtypeConfigResolverImpl : CwtSubtypeConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSubtypeConfig? {
        // 从 key 中提取子类型名：subtype[<name>]
        val name = config.key.removeSurroundingOrNull("subtype[", "]")?.orNull()?.intern() ?: return null
        var typeKeyFilter: ReversibleValue<Set<String>>? = null
        var typeKeyRegex: Regex? = null
        var startsWith: String? = null
        var onlyIfNot: Set<String>? = null

        val options = config.optionConfigs.orEmpty()
        for (option in options) {
            if (option !is CwtOptionConfig) continue
            val key = option.key
            when (key) {
                "type_key_filter" -> {
                    // 值可能是string也可能是stringArray
                    val values = option.getOptionValueOrValues()
                    if (values == null) continue
                    val set = caseInsensitiveStringSet() // 忽略大小写
                    set.addAll(values)
                    // 使用 ReversibleValue 记录运算符（= 为正向，其他为反向）
                    val o = option.separatorType == CwtSeparatorType.EQUAL
                    typeKeyFilter = ReversibleValue(o, set.optimized())
                }
                "type_key_regex" -> {
                    typeKeyRegex = option.stringValue?.toRegex(RegexOption.IGNORE_CASE)
                }
                "starts_with" -> startsWith = option.stringValue ?: continue // 区分大小写
                // 条件禁用：当出现这些根键时不应用当前子类型
                "only_if_not" -> onlyIfNot = option.getOptionValueOrValues() ?: continue
            }
        }
        return CwtSubtypeConfigImpl(config, name, typeKeyFilter, typeKeyRegex, startsWith, onlyIfNot?.optimized())
    }
}

private class CwtSubtypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val typeKeyFilter: ReversibleValue<Set<String>>? = null,
    override val typeKeyRegex: Regex? = null,
    override val startsWith: String? = null,
    override val onlyIfNot: Set<String>? = null
) : UserDataHolderBase(), CwtSubtypeConfig {
    // 通过 group 选项判断子类型分组归属（精确匹配）
    override fun inGroup(groupName: String): Boolean {
        return config.findOption("group")?.stringValue == groupName
    }

    override fun toString() = "CwtSubtypeConfigImpl(name='$name')"
}
