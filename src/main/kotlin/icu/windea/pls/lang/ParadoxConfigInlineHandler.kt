package icu.windea.pls.lang

import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
object ParadoxConfigInlineHandler {
    enum class Mode {
        KEY_TO_KEY, KEY_TO_VALUE, VALUE_TO_KEY, VALUE_TO_VALUE
    }
    
    @Suppress("KotlinConstantConditions") 
    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtDataConfig<*>, mode: Mode) : CwtPropertyConfig? {
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
            booleanValue = when(mode) {
                Mode.KEY_TO_VALUE -> null
                Mode.VALUE_TO_VALUE -> otherConfig.booleanValue
                else -> config.booleanValue
            },
            intValue = when(mode) {
                Mode.KEY_TO_VALUE -> null
                Mode.VALUE_TO_VALUE -> otherConfig.intValue
                else -> config.intValue
            },
            floatValue = when(mode) {
                Mode.KEY_TO_VALUE -> null
                Mode.VALUE_TO_VALUE -> otherConfig.floatValue
                else -> config.floatValue
            },
            stringValue = when(mode) {
                Mode.KEY_TO_VALUE -> if(otherConfig is CwtPropertyConfig) otherConfig.key else return null
                Mode.VALUE_TO_VALUE -> otherConfig.stringValue
                else -> config.stringValue
            },
            configs = when(mode) {
                Mode.KEY_TO_VALUE -> null
                Mode.VALUE_TO_VALUE -> otherConfig.deepCopyConfigs()
                else -> config.deepCopyConfigs()
            },
        )
        inlined.parent = config.parent
        inlined.configs?.forEach { it.parent = inlined }
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
            booleanValue = other.booleanValue,
            intValue = other.intValue,
            floatValue = other.floatValue,
            stringValue = other.stringValue,
            configs = other.deepCopyConfigs(),
            documentation = other.documentation,
            options =  other.options,
            optionValues =  other.optionValues,
        )
        inlined.parent = config.parent
        inlined.configs?.forEach { it.parent = inlined }
        inlined.inlineableConfig = aliasConfig
        return inlined
    }
    
    /**
     * 将指定的[inlineConfig]内联作为子节点并返回。如果需要拷贝，则进行深拷贝。
     */
    fun inlineWithInlineConfig(config: CwtPropertyConfig, inlineConfig: CwtInlineConfig) : CwtPropertyConfig{
        val other = inlineConfig.config
        val inlined = other.copy(
            key = config.key,
            configs = other.deepCopyConfigs()
        )
        inlined.parent = config
        inlined.configs?.forEach { it.parent = inlined }
        inlined.inlineableConfig = inlineConfig
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
            booleanValue = other.booleanValue,
            intValue = other.intValue,
            floatValue = other.floatValue,
            stringValue = other.stringValue,
            configs = other.deepCopyConfigs(),
            documentation = config.documentation ?: other.documentation,
            options = config.options,
            optionValues = config.optionValues
        )
        inlined.parent = config.parent
        inlined.configs?.forEach { it.parent = inlined }
        inlined.inlineableConfig = singleAliasConfig
        return inlined
    }
    
    fun inlineFromInlineConfig(element: ParadoxScriptMemberElement, key: String, isQuoted: Boolean, config: CwtPropertyConfig, result: SmartList<CwtDataConfig<*>>): Boolean {
        //内联特定的规则：inline_script
        val configGroup = config.info.configGroup
        val inlineConfigs = configGroup.inlineConfigGroup[key]
        if(inlineConfigs.isNullOrEmpty()) return false
        for(inlineConfig in inlineConfigs) {
            result.add(inlineWithInlineConfig(config, inlineConfig))
        }
        return true
    }
    
    fun inlineFromAliasConfig(element: ParadoxScriptMemberElement, key: String, isQuoted: Boolean, config: CwtPropertyConfig, result: MutableList<CwtDataConfig<*>>, matchType: Int) {
        //内联类型为single_alias_right或alias_match_left的规则
        run {
            val configGroup = config.info.configGroup
            val valueExpression = config.valueExpression
            when(valueExpression.type) {
                CwtDataType.SingleAliasRight -> {
                    val singleAliasName = valueExpression.value ?: return@run
                    val singleAlias = configGroup.singleAliases[singleAliasName] ?: return@run
                    result.add(inlineWithSingleAliasConfig(config, singleAlias))
                    return
                }
                CwtDataType.AliasMatchLeft -> {
                    val aliasName = valueExpression.value ?: return@run
                    val aliasGroup = configGroup.aliasGroups[aliasName] ?: return@run
                    val aliasSubNames = ParadoxConfigHandler.getAliasSubNames(element, key, isQuoted, aliasName, configGroup, matchType)
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
                    return
                }
                else -> pass()
            }
        }
        result.add(config)
    }
}