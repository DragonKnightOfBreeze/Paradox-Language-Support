package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.util.CwtConfigService
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo

object ParadoxDefinitionInjectionService {
    fun resolveDeclaration(element: PsiElement, definitionInjectionInfo: ParadoxDefinitionInjectionInfo): CwtPropertyConfig? {
        val definitionName = definitionInjectionInfo.target
        if (definitionName.isNullOrEmpty()) return null
        val definitionType = definitionInjectionInfo.type
        if (definitionType.isNullOrEmpty()) return null
        val configGroup = definitionInjectionInfo.configGroup
        val declarationConfig = configGroup.declarations.get(definitionType) ?: return null
        val declarationConfigContext = CwtConfigService.getDeclarationConfigContext(element, definitionName, definitionType, null, configGroup)
        return declarationConfigContext?.getConfig(declarationConfig)
    }
}
