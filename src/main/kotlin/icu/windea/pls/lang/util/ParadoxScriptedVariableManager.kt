package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.resolve.ParadoxScriptedVariableService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于处理封装变量。
 */
object ParadoxScriptedVariableManager {
    fun isGlobalFilePath(path: ParadoxPath): Boolean {
        return "common/scripted_variables".matchesPath(path.path)
    }

    fun getLocalizedName(element: ParadoxScriptScriptedVariable, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): String? {
        val name = element.name?.orNull() ?: return null
        val nameLocalisation = getNameLocalisation(name, element, locale)
        return nameLocalisation?.let { ParadoxLocalisationManager.getLocalizedText(it) }
    }

    fun getLocalizedNames(element: ParadoxScriptScriptedVariable, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<String> {
        val name = element.name?.orNull() ?: return emptySet()
        val nameLocalisation = getNameLocalisations(name, element, locale)
        return nameLocalisation.mapNotNull { ParadoxLocalisationManager.getLocalizedText(it) }.toSet()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        return ParadoxScriptedVariableService.resolveNameLocalisation(name, contextElement, locale)
    }

    fun getNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<ParadoxLocalisationProperty> {
        return ParadoxScriptedVariableService.resolveNameLocalisations(name, contextElement, locale)
    }
}
