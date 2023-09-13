package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*

/**
 * 用于提供CWT声明规则上下文。
 *
 * @see CwtDeclarationConfigContext
 */
@WithGameTypeEP
interface CwtDeclarationConfigContextProvider {
    fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext?
    
    fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String
    
    fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig) : CwtPropertyConfig
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDeclarationConfigContextProvider>("icu.windea.pls.declarationConfigContextProvider")
        
        fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getContext(element, definitionName, definitionType, definitionSubtypes, configGroup)
                    ?.also { it.provider = ep }
            }
        }
    }
}