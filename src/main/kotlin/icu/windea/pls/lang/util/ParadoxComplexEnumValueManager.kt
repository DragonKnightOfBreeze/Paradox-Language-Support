package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.psi.isResolvableLiteralExpression
import icu.windea.pls.lang.resolve.ParadoxComplexEnumValueService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

@Suppress("unused")
object ParadoxComplexEnumValueManager {
    object Keys : KeyRegistry() {
        val cachedComplexEnumValueInfo by registerKey<CachedValue<ParadoxComplexEnumValueInfo>>(Keys)
    }

    fun getInfo(element: ParadoxScriptExpressionElement): ParadoxComplexEnumValueInfo? {
        // fast return
        if (!element.isResolvableLiteralExpression()) return null
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedComplexEnumValueInfo) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val file = element.containingFile
                val value = ParadoxComplexEnumValueService.resolveInfo(element, file)
                val dependencies = ParadoxComplexEnumValueService.getInfoDependencies(element, file)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getInfo(element: ParadoxCsvExpressionElement): ParadoxComplexEnumValueInfo? {
        // fast return
        if (element !is ParadoxCsvColumn) return null
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedComplexEnumValueInfo) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = ParadoxComplexEnumValueService.resolveInfo(element)
                val dependencies = ParadoxComplexEnumValueService.getInfoDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getPresentableName(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): String? {
        val nameLocalisation = getNameLocalisation(name, contextElement, locale)
        return nameLocalisation?.let { ParadoxLocalisationManager.getLocalizedText(it) }
    }

    fun getPresentableNames(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<String> {
        val nameLocalisation = getNameLocalisations(name, contextElement, locale)
        return nameLocalisation.mapNotNull { ParadoxLocalisationManager.getLocalizedText(it) }.toSet()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        return ParadoxComplexEnumValueService.resolveNameLocalisation(name, contextElement, locale)
    }

    fun getNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): List<ParadoxLocalisationProperty> {
        return ParadoxComplexEnumValueService.resolveNameLocalisations(name, contextElement, locale)
    }
}
