package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getCachedValueOnDemand
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.isResolvableLiteralExpression
import icu.windea.pls.lang.resolve.ParadoxComplexEnumValueService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

@Optimized
object ParadoxComplexEnumValueManager {
    object Keys : KeyRegistry() {
        val cachedComplexEnumValueInfo by registerKey<CachedValue<ParadoxComplexEnumValueInfo?>>(this)
    }

    fun getInfo(element: ParadoxExpressionElement): ParadoxComplexEnumValueInfo? {
        when (element) {
            is ParadoxScriptExpressionElement -> {
                if (!element.isResolvableLiteralExpression()) return null// fast return
                return getInfoInternal(element)
            }
            is ParadoxCsvExpressionElement -> {
                if (element !is ParadoxCsvColumn) return null // fast return
                return getInfoInternal(element)
            }
            else -> return null
        }
    }

    private fun getInfoInternal(element: ParadoxScriptExpressionElement): ParadoxComplexEnumValueInfo? {
        return getCachedValueOnDemand(element, Keys.cachedComplexEnumValueInfo, onDemand = ChronicleCapabilities.Cache.complexEnumValue) {
            runSmartReadAction {
                val file = element.containingFile
                val value = ParadoxComplexEnumValueService.resolveInfo(element, file)
                CachedValueProvider.Result.create(value, getInfoDependencies(element, file))
            }
        }
    }

    private fun getInfoInternal(element: ParadoxCsvColumn): ParadoxComplexEnumValueInfo? {
        return getCachedValueOnDemand(element, Keys.cachedComplexEnumValueInfo, onDemand = ChronicleCapabilities.Cache.complexEnumValue) {
            runSmartReadAction {
                val value = ParadoxComplexEnumValueService.resolveInfo(element)
                CachedValueProvider.Result.create(value, getInfoDependencies(element))
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun getInfoDependencies(element: ParadoxScriptExpressionElement, file: PsiFile): List<Any> {
        return listOf(file) // depends on file current only
    }

    @Suppress("UNUSED_PARAMETER")
    private fun getInfoDependencies(element: ParadoxCsvExpressionElement): List<Any> {
        if (element is ParadoxCsvColumn) element.parent?.let { return listOf(it) } // depend on current row
        return listOf(element.containingFile)
    }

    fun getPresentableName(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): String? {
        val nameLocalisation = getNameLocalisation(name, contextElement, locale)
        return nameLocalisation?.let { ParadoxLocalisationManager.getPresentableText(it) }
    }

    @Suppress("unused")
    fun getPresentableNames(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<String> {
        val nameLocalisation = getNameLocalisations(name, contextElement, locale)
        if (nameLocalisation.isEmpty()) return emptySet()
        return nameLocalisation.mapNotNullFast { ParadoxLocalisationManager.getPresentableText(it) }.toSet()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        return ParadoxComplexEnumValueService.resolveNameLocalisation(name, contextElement, locale)
    }

    fun getNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): List<ParadoxLocalisationProperty> {
        return ParadoxComplexEnumValueService.resolveNameLocalisations(name, contextElement, locale)
    }
}
