package icu.windea.pls.config.config

import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

/**
 * @property name string
 * @property typeKeyFilter (property) type_key_filter: boolean
 * @property typeKeyRegex (option) type_key_regex: string
 * @property startsWith (option) starts_with: string
 * @property pushScope (option) push_scope: scope
 * @property displayName (option) display_name: string
 * @property abbreviation (option) abbreviation: string
 * @property onlyIfNot (option) only_if_not: string[]
 */
interface CwtSubtypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    val typeKeyRegex: Regex?
    val startsWith: @CaseInsensitive String?
    val pushScope: String?
    val displayName: String?
    val abbreviation: String?
    val onlyIfNot: Set<String>?
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtSubtypeConfig? {
    val name = config.key.removeSurroundingOrNull("subtype[", "]")?.orNull()?.intern() ?: return null
    var typeKeyFilter: ReversibleValue<Set<String>>? = null
    var typeKeyRegex: Regex? = null
    var pushScope: String? = null
    var startsWith: String? = null
    var displayName: String? = null
    var abbreviation: String? = null
    var onlyIfNot: Set<String>? = null
    
    val options = config.options
    if(!options.isNullOrEmpty()) {
        for(option in options) {
            if(option !is CwtOptionConfig) continue
            val key = option.key
            when(key) {
                "type_key_filter" -> {
                    //值可能是string也可能是stringArray
                    val values = option.getOptionValueOrValues()
                    if(values == null) continue
                    val set = caseInsensitiveStringSet() //忽略大小写
                    set.addAll(values)
                    val o = option.separatorType == CwtSeparatorType.EQUAL
                    typeKeyFilter = set reverseIf o
                }
                "type_key_regex" -> {
                    typeKeyRegex = option.stringValue?.toRegex(RegexOption.IGNORE_CASE)
                }
                "starts_with" -> startsWith = option.stringValue ?: continue //忽略大小写
                "push_scope" -> pushScope = option.stringValue ?: continue
                "display_name" -> displayName = option.stringValue ?: continue
                "abbreviation" -> abbreviation = option.stringValue ?: continue
                "only_if_not" -> onlyIfNot = option.getOptionValueOrValues() ?: continue
            }
        }
    }
    return CwtSubtypeConfigImpl(config, name, typeKeyFilter, typeKeyRegex, startsWith, pushScope, displayName, abbreviation, onlyIfNot)
}

private class CwtSubtypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val typeKeyFilter: ReversibleValue<Set<String>>? = null,
    override val typeKeyRegex: Regex? = null,
    override val startsWith: String? = null,
    override val pushScope: String? = null,
    override val displayName: String? = null,
    override val abbreviation: String? = null,
    override val onlyIfNot: Set<String>? = null
) : CwtSubtypeConfig