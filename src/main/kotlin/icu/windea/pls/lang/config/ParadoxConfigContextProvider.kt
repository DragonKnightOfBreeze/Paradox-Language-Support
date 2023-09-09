package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于提供脚本表达式所在的CWT规则上下文。
 *
 * @see ParadoxConfigContext
 */
interface ParadoxConfigContextProvider {
    fun getConfigContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): ParadoxConfigContext?
    
    fun getCacheKey(configContext: ParadoxConfigContext, matchOptions: Int = Options.Default): String? = null
    
    fun getConfigs(configContext: ParadoxConfigContext, matchOptions: Int = Options.Default): List<CwtMemberConfig<*>>?
    
    fun skipMissingExpressionCheck(configContext: ParadoxConfigContext) = false
    
    fun skipTooManyExpressionCheck(configContext: ParadoxConfigContext) = false
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxConfigContextProvider>("icu.windea.pls.configContextProvider")
        
        fun getContext(element: ParadoxScriptMemberElement): ParadoxConfigContext? {
            val file = element.containingFile ?: return null
            val elementPath = ParadoxElementPathHandler.get(element) ?: return null
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ep.getConfigContext(element, elementPath, file)
                    ?.also { it.provider = ep }
            }
        }
    }
}
