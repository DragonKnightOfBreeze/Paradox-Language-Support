package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.resolve.ParadoxComplexEnumValueService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

@Suppress("unused")
object ParadoxComplexEnumValueManager {
    object Keys : KeyRegistry() {
        val cachedComplexEnumValueInfo by registerKey<CachedValue<ParadoxComplexEnumValueInfo>>(Keys)
    }

    fun getInfo(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueInfo? {
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedComplexEnumValueInfo) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val file = element.containingFile
                val value = ParadoxComplexEnumValueService.resolveInfo(element, file)
                val dependencies = ParadoxComplexEnumValueService.getDependencies(element, file)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        return ParadoxComplexEnumValueService.resolveNameLocalisation(name, contextElement, locale)
    }

    fun getNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<ParadoxLocalisationProperty> {
        return ParadoxComplexEnumValueService.resolveNameLocalisations(name, contextElement, locale)
    }
}
