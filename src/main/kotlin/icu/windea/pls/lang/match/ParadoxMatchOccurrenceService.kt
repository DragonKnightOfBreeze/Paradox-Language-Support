package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.castOrNull
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

object ParadoxMatchOccurrenceService {
    fun <T : CwtMember> evaluate(contextElement: PsiElement, config: CwtMemberConfig<T>): ParadoxMatchOccurrence {
        val project = config.configGroup.project
        val cardinality = config.optionData.cardinality ?: return ParadoxMatchOccurrence(0, null, null)
        val cardinalityMinDefine = config.optionData.cardinalityMinDefine
        val cardinalityMaxDefine = config.optionData.cardinalityMaxDefine
        val occurrence = ParadoxMatchOccurrence(0, cardinality.min, cardinality.max, cardinality.relaxMin, cardinality.relaxMax)
        config.run {
            if (cardinalityMinDefine == null) return@run
            val defineValue = ParadoxDefineManager.getDefineValue(cardinalityMinDefine, contextElement, project)?.castOrNull<Int>() ?: return@run
            occurrence.min = defineValue
            occurrence.minDefine = cardinalityMinDefine
        }
        config.run {
            if (cardinalityMaxDefine == null) return@run
            val defineValue = ParadoxDefineManager.getDefineValue(cardinalityMaxDefine, contextElement, project)?.castOrNull<Int>() ?: return@run
            occurrence.max = defineValue
            occurrence.maxDefine = cardinalityMaxDefine
        }
        return occurrence
    }

    private fun evaluateForChildren(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, ParadoxMatchOccurrence> {
        if (configs.isEmpty()) return emptyMap()
        val configGroup = configs.first().configGroup
        // 这里需要先按优先级排序
        val childConfigs = configs.flatMap { it.configs.orEmpty() }.sortedByPriority({ it.configExpression }, { configGroup })
        if (childConfigs.isEmpty()) return emptyMap()
        val blockElement = when (element) {
            is ParadoxScriptDefinitionElement -> element.block
            is ParadoxScriptBlockElement -> element
            else -> null
        }
        if (blockElement == null) return emptyMap()
        val occurrenceMap = mutableMapOf<CwtDataExpression, ParadoxMatchOccurrence>()
        for (childConfig in childConfigs) {
            occurrenceMap[childConfig.configExpression] = ParadoxMatchOccurrenceService.evaluate(element, childConfig)
        }
        ProgressManager.checkCanceled()
        // 注意这里需要考虑内联和可选的情况
        blockElement.members(conditional = true, inline = true).forEach f@{ data ->
            val expression = when (data) {
                is ParadoxScriptProperty -> ParadoxScriptExpression.resolve(data.propertyKey)
                is ParadoxScriptValue -> ParadoxScriptExpression.resolve(data)
                else -> return@f
            }
            val isParameterized = expression.type == ParadoxType.String && expression.value.isParameterized()
            // may contain parameter -> can't and should not get occurrences
            if (isParameterized) {
                occurrenceMap.clear()
                return@f
            }
            val matched = childConfigs.find { childConfig ->
                if (childConfig is CwtPropertyConfig && data !is ParadoxScriptProperty) return@find false
                if (childConfig is CwtValueConfig && data !is ParadoxScriptValue) return@find false
                ParadoxMatchService.matchScriptExpression(data, expression, childConfig.configExpression, childConfig, configGroup).get()
            }
            if (matched == null) return@f
            val occurrence = occurrenceMap[matched.configExpression]
            if (occurrence == null) return@f
            occurrence.actual += 1
        }
        return occurrenceMap
    }
}
