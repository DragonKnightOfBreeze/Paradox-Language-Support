package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*

object ParadoxConfigInlineHandler {
    enum class Mode {
        KEY_TO_KEY, KEY_TO_VALUE, VALUE_TO_KEY, VALUE_TO_VALUE
    }
    
    /**
     * 将指定的[inlineConfig]内联作为子节点并返回。如果需要拷贝，则进行深拷贝。
     */
    fun inlineWithInlineConfig(inlineConfig: CwtInlineConfig): CwtPropertyConfig {
        val other = inlineConfig.config
        val inlined = other.copy(
            key = inlineConfig.name,
            configs = other.deepCopyConfigs()
        )
        inlined.configs?.forEach { it.parent = inlined }
        inlined.inlineableConfig = inlineConfig
        return inlined
    }
    
    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, mode: Mode): CwtPropertyConfig? {
        val inlined = config.copy(
            key = when(mode) {
                Mode.KEY_TO_KEY -> if(otherConfig is CwtPropertyConfig) otherConfig.key else return null
                Mode.VALUE_TO_KEY -> otherConfig.value
                else -> config.key
            },
            value = when(mode) {
                Mode.VALUE_TO_VALUE -> otherConfig.value
                Mode.KEY_TO_VALUE -> if(otherConfig is CwtPropertyConfig) otherConfig.key else return null
                else -> config.value
            },
            configs = when(mode) {
                Mode.KEY_TO_VALUE -> null
                Mode.VALUE_TO_VALUE -> otherConfig.deepCopyConfigs()
                else -> config.deepCopyConfigs()
            },
        )
        inlined.configs?.forEach { it.parent = inlined }
        inlined.parent = config.parent
        inlined.inlineableConfig = config.inlineableConfig
        return inlined
    }
    
    /**
     * 从[aliasConfig]内联规则：key改为取[aliasConfig]的subName，value改为取[aliasConfig]的的value，如果需要拷贝，则进行深拷贝。
     */
    fun inlineWithAliasConfig(config: CwtPropertyConfig, aliasConfig: CwtAliasConfig): CwtPropertyConfig {
        val other = aliasConfig.config
        val inlined = config.copy(
            key = aliasConfig.subName,
            value = other.value,
            configs = other.deepCopyConfigs(),
            documentation = other.documentation,
            options = other.options
        )
        inlined.parent = config.parent
        inlined.configs?.forEach { it.parent = inlined }
        inlined.inlineableConfig = config.inlineableConfig ?: aliasConfig
        return inlined
    }
    
    /**
     * 从[singleAliasConfig]内联规则：value改为取[singleAliasConfig]的的value，如果需要拷贝，则进行深拷贝。
     */
    fun inlineWithSingleAliasConfig(config: CwtPropertyConfig, singleAliasConfig: CwtSingleAliasConfig): CwtPropertyConfig {
        //内联所有value
        //这里需要优先使用singleAliasConfig的options、optionValues和documentation
        val other = singleAliasConfig.config
        val inlined = config.copy(
            value = other.value,
            configs = other.deepCopyConfigs(),
            documentation = config.documentation ?: other.documentation,
            options = config.options
        )
        inlined.parent = config.parent
        inlined.configs?.forEach { it.parent = inlined }
        inlined.inlineableConfig = config.inlineableConfig //should not set to singleAliasConfig - a single alias config do not inline property key  
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
                result.add(inlineWithSingleAliasConfig(config, singleAlias))
                return result
            }
            CwtDataType.AliasMatchLeft -> {
                val aliasName = valueExpression.value ?: return emptyList()
                val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
                val result = mutableListOf<CwtMemberConfig<*>>()
                val aliasSubNames = ParadoxConfigHandler.getAliasSubNames(element, key, isQuoted, aliasName, configGroup, matchOptions)
                for(aliasSubName in aliasSubNames) {
                    val aliases = aliasGroup[aliasSubName] ?: continue
                    for(alias in aliases) {
                        var inlinedConfig = inlineWithAliasConfig(config, alias)
                        if(inlinedConfig.valueExpression.type == CwtDataType.SingleAliasRight) {
                            val singleAliasName = inlinedConfig.valueExpression.value ?: continue
                            val singleAlias = configGroup.singleAliases[singleAliasName] ?: continue
                            inlinedConfig = inlineWithSingleAliasConfig(inlinedConfig, singleAlias)
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