package icu.windea.pls.lang.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.isIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.resolve.util.ParadoxReadWriteAccessFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import javax.swing.Icon

@Optimized
object ParadoxDynamicValueManager {
    @Deprecated("", ReplaceWith("ParadoxReadWriteAccessFactory.from(configExpression)", "icu.windea.pls.lang.resolve.util.ParadoxReadWriteAccessFactory"))
    fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return ParadoxReadWriteAccessFactory.from(configExpression)
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxDynamicValueLightElement? {
        if (!name.isIdentifier()) return null // skip invalid names
        val readWriteAccess = ParadoxReadWriteAccessFactory.from(configExpression)
        val dynamicValueType = configExpression.metadata.value ?: return null
        return ParadoxDynamicValueLightElement(element, name, dynamicValueType, readWriteAccess, configGroup.gameType, configGroup.project)
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpressions: Collection<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxDynamicValueLightElement? {
        if (!name.isIdentifier()) return null // skip invalid names
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = ParadoxReadWriteAccessFactory.from(configExpression)
        val dynamicValueTypes = configExpressions.mapNotNull { it.metadata.value }.toSet()
        return ParadoxDynamicValueLightElement(element, name, dynamicValueTypes, readWriteAccess, configGroup.gameType, configGroup.project)
    }

    // region Related Items

    fun getRelatedLocalisations(
        element: ParadoxDynamicValueLightElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<ParadoxLocalisationProperty> {
        return getRelatedLocalisations(element.name, element, preferredLocale, onlyOne)
    }

    fun getRelatedLocalisations(
        name: String?,
        contextElement: PsiElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<ParadoxLocalisationProperty> {
        return ParadoxLocalisationManager.getRelatedLocalisationsFrom(name, contextElement, preferredLocale, onlyOne)
    }

    // endregion

    // region Presentable Items

    @Suppress("unused")
    fun getPresentableNames(
        element: ParadoxDynamicValueLightElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<String> {
        val localisations = getRelatedLocalisations(element, preferredLocale, onlyOne)
        return ParadoxLocalisationManager.getPresentableText(localisations)
    }

    @Suppress("unused")
    fun getPresentableNames(
        name: String,
        contextElement: PsiElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<String> {
        val localisation = getRelatedLocalisations(name, contextElement, preferredLocale, onlyOne)
        return ParadoxLocalisationManager.getPresentableText(localisation)
    }

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

    // endregion
}
