package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.CwtConfigMatcher.Options
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于提供脚本表达式所在的CWT规则上下文。
 *
 * @see CwtConfigContext
 */
@WithGameTypeEP
interface CwtConfigContextProvider {
    fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): CwtConfigContext?
    
    fun getCacheKey(context: CwtConfigContext, matchOptions: Int = Options.Default): String?
    
    fun getConfigs(context: CwtConfigContext, matchOptions: Int = Options.Default): List<CwtMemberConfig<*>>?
    
    fun skipMissingExpressionCheck(context: CwtConfigContext) = false
    
    fun skipTooManyExpressionCheck(context: CwtConfigContext) = false
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigContextProvider>("icu.windea.pls.configContextProvider")
        
        fun getContext(element: ParadoxScriptMemberElement): CwtConfigContext? {
            val file = element.containingFile ?: return null
            val elementPath = ParadoxElementPathHandler.get(element) ?: return null
            val gameType = selectGameType(file)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getContext(element, elementPath, file)
                    ?.also { it.provider = ep }
            }
        }
    }
}
