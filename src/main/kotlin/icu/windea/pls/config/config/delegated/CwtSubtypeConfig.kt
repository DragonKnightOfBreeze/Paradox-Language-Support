@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.CwtConfig.Option
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

interface CwtSubtypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    @Option("type_key_filter: string | string[]")
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    @Option("type_key_regex: string?")
    val typeKeyRegex: Regex?
    @Option("starts_with: string?")
    val startsWith: @CaseInsensitive String?
    @Option("only_if_not: string[]?")
    val onlyIfNot: Set<String>?

    companion object {
        fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig? = doResolve(config)
    }
}

fun CwtSubtypeConfig.inGroup(groupName: String): Boolean {
    return config.findOption("group")?.stringValue == groupName
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtSubtypeConfig? {
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
                //值可能是string也可能是stringArray
                val values = option.getOptionValueOrValues()
                if (values == null) continue
                val set = caseInsensitiveStringSet() //忽略大小写
                set.addAll(values)
                val o = option.separatorType == CwtSeparatorType.EQUAL
                typeKeyFilter = ReversibleValue(o, set.optimized())
            }
            "type_key_regex" -> {
                typeKeyRegex = option.stringValue?.toRegex(RegexOption.IGNORE_CASE)
            }
            "starts_with" -> startsWith = option.stringValue ?: continue //忽略大小写
            "only_if_not" -> onlyIfNot = option.getOptionValueOrValues() ?: continue
        }
    }
    return CwtSubtypeConfigImpl(config, name, typeKeyFilter, typeKeyRegex, startsWith, onlyIfNot?.optimized())
}

private class CwtSubtypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val typeKeyFilter: ReversibleValue<Set<String>>? = null,
    override val typeKeyRegex: Regex? = null,
    override val startsWith: String? = null,
    override val onlyIfNot: Set<String>? = null
) : UserDataHolderBase(), CwtSubtypeConfig {
    override fun toString(): String {
        return "CwtSubtypeConfigImpl(name='$name')"
    }
}
