package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*

object ParadoxConfigGenerator {
    fun deepCopyConfigs(config: CwtMemberConfig<*>): List<CwtMemberConfig<*>>? {
        if(config.configs.isNullOrEmpty()) return config.configs
        return config.configs?.mapFast { c1 ->
            when(c1) {
                is CwtPropertyConfig -> c1.delegated(deepCopyConfigs(c1), c1.parentConfig)
                is CwtValueConfig -> c1.delegated(deepCopyConfigs(c1), c1.parentConfig)
            }
        }
    }
    
    fun deepCopyConfigsInDeclarationConfig(config: CwtMemberConfig<*>, context: CwtDeclarationConfigContext): List<CwtMemberConfig<*>> {
        //因为之后可能需要对得到的声明规则进行注入，需要保证当注入时所有规则列表都是可变的
        
        val mergedConfigs: MutableList<CwtMemberConfig<*>>? = if(config.configs != null) mutableListOf() else null
        config.configs?.forEachFast { c1 ->
            val c2s = deepCopyConfigsInDeclarationConfig(c1, context)
            if(c2s.isNotEmpty()) {
                for(c2 in c2s) {
                    mergedConfigs?.add(c2)
                }
            }
        }
        when(config) {
            is CwtValueConfig -> {
                val mergedConfig = config.delegated(mergedConfigs, config.parentConfig)
                return mergedConfig.toSingletonList()
            }
            is CwtPropertyConfig -> {
                val subtypeExpression = config.key.removeSurroundingOrNull("subtype[", "]")
                if(subtypeExpression == null) {
                    val mergedConfig = config.delegated(mergedConfigs, config.parentConfig)
                    return mergedConfig.toSingletonList()
                } else {
                    val subtypes = context.definitionSubtypes
                    if(subtypes == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
                        mergedConfigs?.forEachFast { mergedConfig ->
                            mergedConfig.parentConfig = config.parentConfig
                        }
                        return mergedConfigs.orEmpty()
                    } else {
                        return emptyList()
                    }
                }
            }
        }
    }
    
    enum class InlineMode {
        KEY_TO_KEY, KEY_TO_VALUE, VALUE_TO_KEY, VALUE_TO_VALUE
    }
    
    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, inlineMode: InlineMode): CwtPropertyConfig? {
        val inlined = config.copy(
            key = when(inlineMode) {
                InlineMode.KEY_TO_KEY -> if(otherConfig is CwtPropertyConfig) otherConfig.key else return null
                InlineMode.VALUE_TO_KEY -> otherConfig.value
                else -> config.key
            },
            value = when(inlineMode) {
                InlineMode.VALUE_TO_VALUE -> otherConfig.value
                InlineMode.KEY_TO_VALUE -> if(otherConfig is CwtPropertyConfig) otherConfig.key else return null
                else -> config.value
            },
            configs = when(inlineMode) {
                InlineMode.KEY_TO_VALUE -> null
                InlineMode.VALUE_TO_VALUE -> deepCopyConfigs(otherConfig)
                else -> deepCopyConfigs(config)
            },
        )
        inlined.configs?.forEachFast { it.parentConfig = inlined }
        inlined.parentConfig = config.parentConfig
        inlined.inlineableConfig = config.inlineableConfig
        return inlined
    }
    
    fun inlineWithInlineConfig(inlineConfig: CwtInlineConfig): CwtPropertyConfig {
        return doInlineWithInlineConfig(inlineConfig)
    }
    
    private fun doInlineWithInlineConfig(inlineConfig: CwtInlineConfig): CwtPropertyConfig {
        val other = inlineConfig.config
        val inlined = other.copy(
            key = inlineConfig.name,
            configs = deepCopyConfigs(other)
        )
        inlined.configs?.forEachFast { it.parentConfig = inlined }
        inlined.inlineableConfig = inlineConfig
        return inlined
    }
    
    fun inlineByConfig(element: PsiElement, key: String, isQuoted: Boolean, config: CwtPropertyConfig, matchOptions: Int): List<CwtMemberConfig<*>> {
        //内联类型为single_alias_right或alias_match_left的规则
        val configGroup = config.info.configGroup
        val valueExpression = config.valueExpression
        when(valueExpression.type) {
            CwtDataType.SingleAliasRight -> {
                val singleAliasName = valueExpression.value ?: return emptyList()
                val singleAlias = configGroup.singleAliases[singleAliasName] ?: return emptyList()
                val result = mutableListOf<CwtMemberConfig<*>>()
                result.add(singleAlias.inline(config))
                return result
            }
            CwtDataType.AliasMatchLeft -> {
                val aliasName = valueExpression.value ?: return emptyList()
                val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
                val result = mutableListOf<CwtMemberConfig<*>>()
                val aliasSubNames = ParadoxConfigHandler.getAliasSubNames(element, key, isQuoted, aliasName, configGroup, matchOptions)
                aliasSubNames.forEachFast f1@{ aliasSubName ->
                    val aliases = aliasGroup[aliasSubName] ?: return@f1
                    aliases.forEachFast f2@{ alias ->
                        var inlinedConfig = alias.inline(config)
                        if(inlinedConfig.valueExpression.type == CwtDataType.SingleAliasRight) {
                            val singleAliasName = inlinedConfig.valueExpression.value ?: return@f2
                            val singleAlias = configGroup.singleAliases[singleAliasName] ?: return@f2
                            inlinedConfig = singleAlias.inline(inlinedConfig)
                        }
                        result.add(inlinedConfig)
                    }
                }
                return result
            }
            else -> return emptyList()
        }
    }
}