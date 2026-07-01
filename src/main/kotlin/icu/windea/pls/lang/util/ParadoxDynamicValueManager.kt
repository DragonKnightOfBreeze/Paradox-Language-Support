package icu.windea.pls.lang.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.resolve.ParadoxDynamicValueService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import javax.swing.Icon

@Suppress("unused")
object ParadoxDynamicValueManager {
    fun getPresentableIcon(types: Set<String>): Icon {
        val type = types.first() // first is ok
        return ChronicleIcons.Nodes.DynamicValue(type)
    }

    fun getPresentableType(types: Set<String>): String {
        return when {
            types.size == 2 && "event_target" in types && "global_event_target" in types -> "event_target"
            else -> types.joinToString(" | ")
        }
    }

    fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when (configExpression.type) {
            CwtDataTypes.Value -> Access.Read
            CwtDataTypes.ValueSet -> Access.Write
            CwtDataTypes.DynamicValue -> Access.ReadWrite
            else -> Access.ReadWrite
        }
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxDynamicValueLightElement? {
        if (!name.isIdentifier()) return null // skip invalid names
        val readWriteAccess = getReadWriteAccess(configExpression)
        val dynamicValueType = configExpression.value ?: return null
        return ParadoxDynamicValueLightElement(element, name, dynamicValueType, readWriteAccess, configGroup.gameType, configGroup.project)
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxDynamicValueLightElement? {
        if (!name.isIdentifier()) return null // skip invalid names
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val dynamicValueTypes = configExpressions.mapNotNullTo(mutableSetOf()) { it.value }
        return ParadoxDynamicValueLightElement(element, name, dynamicValueTypes, readWriteAccess, configGroup.gameType, configGroup.project)
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

    fun getNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): List<ParadoxLocalisationProperty> {
        return ParadoxDynamicValueService.resolveNameLocalisations(name, contextElement, locale)
    }
}
