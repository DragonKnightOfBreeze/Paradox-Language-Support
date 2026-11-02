package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.lang.psi.light.CwtConfigSymbolLightElement

object CwtConfigElementDescriptionService {
    fun getName(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

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
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

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
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // {type} {nameOrAnonymous}
        val type = getType(element) ?: return null
        val name = getName(element)
        return type + " " + name.orAnonymous()
    }

    fun getHighlightUsagesDescription(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return getNodeText(element)
    }

    private fun checkLanguage(element: PsiElement): Boolean = element.language == CwtLanguage
}
