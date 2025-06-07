package icu.windea.pls.lang.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

object ParadoxDynamicValueManager {
    const val EVENT_TARGET_PREFIX = "event_target:"

    val EVENT_TARGETS = setOf("event_target", "global_event_target")

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
        if (!name.isIdentifier()) return null //skip invalid names
        val gameType = configGroup.gameType ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val dynamicValueType = configExpression.value ?: return null
        return ParadoxDynamicValueElement(element, name, dynamicValueType, readWriteAccess, gameType, configGroup.project)
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxDynamicValueElement? {
        if (!name.isIdentifier()) return null //skip invalid names
        val gameType = configGroup.gameType ?: return null
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val dynamicValueTypes = configExpressions.mapNotNullTo(mutableSetOf()) { it.value }
        return ParadoxDynamicValueElement(element, name, dynamicValueTypes, readWriteAccess, gameType, configGroup.project)
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig): ParadoxLocalisationProperty? {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive()
            .preferLocale(locale)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }

    fun getHintFromExtendedConfig(name: String, type: String, contextElement: PsiElement): String? {
        if (name.isEmpty()) return null
        val gameType = selectGameType(contextElement) ?: return null
        val configGroup = PlsFacade.getConfigGroup(contextElement.project, gameType)
        val configs = configGroup.extendedDynamicValues[type] ?: return null
        val config = configs.findFromPattern(name, contextElement, configGroup) ?: return null
        return config.hint
    }
}
