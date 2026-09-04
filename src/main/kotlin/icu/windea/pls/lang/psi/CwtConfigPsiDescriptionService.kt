package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPsiDescriptionService
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.lang.psi.light.CwtConfigSymbolLightElement

object CwtConfigPsiDescriptionService {
    fun getName(element: PsiElement): String? {
        return when (element) {
            is CwtProperty -> getNameFromConfigType(element)
            is CwtString -> getNameFromConfigType(element)
            is CwtConfigSymbolLightElement -> element.name
            else -> null
        }
    }

    private fun getNameFromConfigType(element: CwtMember): String? {
        val configType = CwtConfigManager.getConfigType(element)?.takeIf { it.isReference } ?: return null
        val elementName = element.name ?: return null
        return CwtConfigManager.getNameByConfigType(elementName, configType) ?: elementName
    }

    fun getType(element: PsiElement): String? {
        // should not be upper-cased
        return when (element) {
            is CwtProperty -> getTypeFromConfigType(element)
            is CwtString -> getTypeFromConfigType(element)
            is CwtConfigSymbolLightElement -> element.configType.description
            else -> null
        }
    }

    private fun getTypeFromConfigType(element: CwtMember): String? {
        val configType = CwtConfigManager.getConfigType(element)?.takeIf { it.isReference } ?: return null
        return configType.description
    }

    fun getNodeText(element: PsiElement): String? {
        // {type} {nameOrAnonymous}
        val type = CwtPsiDescriptionService.getType(element) ?: return null
        val name = getName(element)
        return type + " " + name.or.anonymous()
    }

    fun getHighlightUsagesDescription(element: PsiElement): String? {
        return getNodeText(element)
    }
}
