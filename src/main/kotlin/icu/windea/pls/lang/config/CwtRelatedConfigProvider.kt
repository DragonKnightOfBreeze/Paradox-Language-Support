package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.navigation.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于上下文提供相关的CWT规则。
 *
 * @see GotoRelatedCwtConfigsAction
 * @see GotoRelatedCwtConfigsHandler
 */
@WithGameTypeEP
interface CwtRelatedConfigProvider {
    /**
     * 基于指定的脚本表达式[element]获取相关的CWT规则。
     */
    fun getRelatedConfigs(element: ParadoxScriptExpressionElement): List<CwtConfig<*>>
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtRelatedConfigProvider>("icu.windea.pls.relatedConfigProvider")
        
        fun getRelatedConfigs(element: ParadoxScriptExpressionElement): List<CwtConfig<*>> {
            val gameType = selectGameType(element) ?: return emptyList()
            val result = mutableListOf<CwtConfig<*>>()
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                val r = ep.getRelatedConfigs(element)
                result += r
            }
            return result
        }
    }
}