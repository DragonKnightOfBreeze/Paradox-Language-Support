package icu.windea.pls.lang

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*

object ParadoxConfigInlineHandler {
    fun inlineByInlineConfig(element: PsiElement, key: String, isQuoted: Boolean, parentConfig: CwtPropertyConfig, configGroup: CwtConfigGroup, result: SmartList<CwtDataConfig<*>>): Boolean {
        //内联特定的规则：inline_script
        val inlineConfigs = configGroup.inlineConfigGroup[key]
        if(inlineConfigs.isNullOrEmpty()) return false
        for(inlineConfig in inlineConfigs) {
            result.add(parentConfig.inlineConfigAsChild(inlineConfig))
        }
        return true
    }
    
    fun inlineByAliasConfig(element: PsiElement, key: String, isQuoted: Boolean, config: CwtPropertyConfig, configGroup: CwtConfigGroup, result: MutableList<CwtDataConfig<*>>, matchType: Int) {
        //内联类型为single_alias_right或alias_match_left的规则
        run {
            val valueExpression = config.valueExpression
            when(valueExpression.type) {
                CwtDataType.SingleAliasRight -> {
                    val singleAliasName = valueExpression.value ?: return@run
                    val singleAlias = configGroup.singleAliases[singleAliasName] ?: return@run
                    result.add(config.inlineFromSingleAliasConfig(singleAlias))
                    return
                }
                CwtDataType.AliasMatchLeft -> {
                    val aliasName = valueExpression.value ?: return@run
                    val aliasGroup = configGroup.aliasGroups[aliasName] ?: return@run
                    val aliasSubNames = ParadoxConfigHandler.getAliasSubNames(element, key, isQuoted, aliasName, configGroup, matchType)
                    for(aliasSubName in aliasSubNames) {
                        val aliases = aliasGroup[aliasSubName] ?: continue
                        for(alias in aliases) {
                            var inlinedConfig = config.inlineFromAliasConfig(alias)
                            if(inlinedConfig.valueExpression.type == CwtDataType.SingleAliasRight) {
                                val singleAliasName = inlinedConfig.valueExpression.value ?: continue
                                val singleAlias = configGroup.singleAliases[singleAliasName] ?: continue
                                inlinedConfig = inlinedConfig.inlineFromSingleAliasConfig(singleAlias)
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