package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.orNull
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo

object ParadoxDefinitionInjectionService {
    fun resolveDeclaration(element: PsiElement, definitionInjectionInfo: ParadoxDefinitionInjectionInfo): CwtPropertyConfig? {
        val declarationConfig = definitionInjectionInfo.declarationConfig ?: return null
        val definitionName = definitionInjectionInfo.target?.orNull() ?: return null
        val definitionType = definitionInjectionInfo.type?.orNull() ?: return null
        val definitionSubtypes = definitionInjectionInfo.subtypes
        val configGroup = definitionInjectionInfo.configGroup
        val declarationConfigContext = ParadoxConfigService.getDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, configGroup)
        return declarationConfigContext?.getConfig(declarationConfig)
    }
}
