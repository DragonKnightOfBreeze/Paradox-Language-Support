package icu.windea.pls.lang.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueElement
import icu.windea.pls.lang.resolve.ParadoxDynamicValueService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

@Suppress("unused")
object ParadoxDynamicValueManager {
    fun getName(expression: String): String? {
        return expression.substringBefore('@').orNull()
    }

    fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when (configExpression.type) {
            CwtDataTypes.Value -> Access.Read
            CwtDataTypes.ValueSet -> Access.Write
            CwtDataTypes.DynamicValue -> Access.ReadWrite
            else -> Access.ReadWrite
        }
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxDynamicValueElement? {
        if (!name.isIdentifier()) return null // skip invalid names
        val gameType = configGroup.gameType
        val readWriteAccess = getReadWriteAccess(configExpression)
        val dynamicValueType = configExpression.value ?: return null
        return ParadoxDynamicValueElement(element, name, dynamicValueType, readWriteAccess, gameType, configGroup.project)
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxDynamicValueElement? {
        if (!name.isIdentifier()) return null // skip invalid names
        val gameType = configGroup.gameType
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val dynamicValueTypes = configExpressions.mapNotNullTo(mutableSetOf()) { it.value }
        return ParadoxDynamicValueElement(element, name, dynamicValueTypes, readWriteAccess, gameType, configGroup.project)
    }

    fun getPresentableType(types: Set<String>): String {
        return when {
            types.size == 2 && "event_target" in types && "global_event_target" in types -> "event_target"
            else -> types.joinToString(" | ")
        }
    }

    fun getLocalizedName(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): String? {
        val nameLocalisation = getNameLocalisation(name, contextElement, locale)
        return nameLocalisation?.let { ParadoxLocalisationManager.getLocalizedText(it) }
    }

    fun getLocalizedNames(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<String> {
        val nameLocalisation = getNameLocalisations(name, contextElement, locale)
        return nameLocalisation.mapNotNull { ParadoxLocalisationManager.getLocalizedText(it) }.toSet()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        return ParadoxDynamicValueService.resolveNameLocalisation(name, contextElement, locale)
    }

    fun getNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<ParadoxLocalisationProperty> {
        return ParadoxDynamicValueService.resolveNameLocalisations(name, contextElement, locale)
    }
}
