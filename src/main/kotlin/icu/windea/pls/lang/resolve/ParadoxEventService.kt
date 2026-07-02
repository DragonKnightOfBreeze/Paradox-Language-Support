package icu.windea.pls.lang.resolve

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.search.searches.ReferencesSearch
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.process
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.withGameType
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxEventManager.getInvocations
import icu.windea.pls.lang.util.ParadoxEventManager.isValidEventId
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiService
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

object ParadoxEventService {
    fun resolveInvocations(definition: ParadoxDefinitionElement): Set<String> {
        val result = mutableSetOf<String>()
        definition.block?.acceptChildren(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
                if (!ParadoxScriptPsiService.isMemberContextElement(element)) return // optimize
                super.visitElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if (!element.isDataExpression()) return
                val value = element.value
                if (result.contains(value)) return
                if (!isValidEventId(value)) return // 排除非法的事件ID
                val configs = ParadoxConfigManager.getConfigs(element)
                val isEventConfig = configs.any { isEventConfig(it) }
                if (isEventConfig) {
                    result.add(value)
                }
            }

            private fun isEventConfig(config: CwtMemberConfig<*>): Boolean {
                return config.configExpression.type == CwtDataTypes.Definition
                    && config.configExpression.value?.substringBefore('.') == ParadoxDefinitionTypes.event
            }
        })
        return result
    }

    fun resolveInvokerEvents(definition: ParadoxDefinitionElement, selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
        // NOTE 1. 目前不兼容封装变量引用 2. 这里需要从所有同名定义查找用法
        // NOTE 为了优化性能，这里可能需要新增并应用索引
        val name = definition.definitionInfo?.name
        if (name.isNullOrEmpty()) return emptyList()
        selector.withGameType(ParadoxGameType.Stellaris)
        return buildList {
            ParadoxDefinitionSearch.searchProperty(name, ParadoxDefinitionTypes.event, selector).process p0@{ definition0 ->
                ProgressManager.checkCanceled()
                ReferencesSearch.search(definition0, selector.scope).process p@{ ref ->
                    if (ref !is ParadoxScriptExpressionPsiReference) return@p true
                    ProgressManager.checkCanceled()
                    val refElement = ref.element.castOrNull<ParadoxScriptString>() ?: return@p true
                    val rDefinition = selectScope { refElement.parentDefinition().asProperty() } ?: return@p true
                    val rDefinitionInfo = rDefinition.definitionInfo ?: return@p true
                    if (rDefinitionInfo.name.isEmpty()) return@p true
                    if (rDefinitionInfo.type != ParadoxDefinitionTypes.event) return@p true
                    this += rDefinition
                    true
                }
                true
            }
        }.distinct()
    }

    fun resolveInvokedEvents(definition: ParadoxDefinitionElement, selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
        // NOTE 1. 目前不兼容封装变量引用
        // NOTE 为了优化性能，这里可能需要新增并应用索引
        val name = definition.definitionInfo?.name
        if (name.isNullOrEmpty()) return emptyList()
        val invocations = getInvocations(definition)
        if (invocations.isEmpty()) return emptyList()
        selector.withGameType(ParadoxGameType.Stellaris)
        return buildList {
            ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.event, selector).process p@{ rDefinition ->
                ProgressManager.checkCanceled()
                val rDefinitionInfo = rDefinition.definitionInfo ?: return@p true
                if (rDefinitionInfo.name.isEmpty()) return@p true
                if (rDefinitionInfo.name !in invocations) return@p true
                this += rDefinition
                true
            }
        }.distinct()
    }
}
