package icu.windea.pls.ep.configContext

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtDeclarationConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.config.configContext.provider
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于提供CWT声明规则上下文。
 *
 * @see CwtDeclarationConfigContext
 */
@WithGameTypeEP
interface CwtDeclarationConfigContextProvider {
    fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext?

    fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String

    fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtDeclarationConfigContextProvider>("icu.windea.pls.declarationConfigContextProvider")

        fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getContext(element, definitionName, definitionType, definitionSubtypes, gameType, configGroup)
                    ?.also { it.provider = ep }
            }
        }
    }
}
