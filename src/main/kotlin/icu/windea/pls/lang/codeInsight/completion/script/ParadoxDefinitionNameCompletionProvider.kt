package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxExtendedCompletionManager
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.configGroup
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.codeInsight.completion.expressionOffset
import icu.windea.pls.lang.codeInsight.completion.expressionTailText
import icu.windea.pls.lang.codeInsight.completion.isKey
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.codeInsight.completion.offsetInParent
import icu.windea.pls.lang.codeInsight.completion.quoted
import icu.windea.pls.lang.codeInsight.completion.rightQuoted
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.distinctByDefinitionName
import icu.windea.pls.lang.search.selector.filterBy
import icu.windea.pls.lang.search.selector.notSamePosition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isDefinitionName

/**
 * 提供定义的名字的代码补全。
 */
class ParadoxDefinitionNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsSettings.getInstance().state.completion.completeDefinitionNames) return

        val position = parameters.position
        val element = position.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (element.text.isParameterized()) return
        val file = parameters.originalFile
        val project = file.project
        val quoted = element.text.isLeftQuoted()
        val rightQuoted = element.text.isRightQuoted()
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)

        ParadoxCompletionManager.initializeContext(parameters, context)
        context.contextElement = element
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted
        context.expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)

        val gameType = selectGameType(file) ?: return
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        context.configGroup = configGroup

        when {
            // key_
            // key_ =
            // key_ = { ... }
            element is ParadoxScriptPropertyKey || (element is ParadoxScriptString && element.isBlockMember()) -> {
                val fileInfo = file.fileInfo ?: return
                val path = fileInfo.path
                val maxDepth = PlsInternalSettings.getInstance().maxDefinitionDepth
                val rootKeys = ParadoxMemberService.getRootKeys(element, maxDepth = maxDepth) ?: return
                if (rootKeys.any { it.isParameterized() }) return // 忽略带参数的情况
                val typeKeyPrefix = lazy { ParadoxMemberService.getKeyPrefix(element) }
                for (typeConfig in configGroup.types.values) {
                    if (typeConfig.nameField != null) continue
                    val matchContext = CwtTypeConfigMatchContext(configGroup, path, null, rootKeys, typeKeyPrefix)
                    if (!ParadoxConfigMatchService.matchesTypeByUnknownDeclaration(matchContext, typeConfig)) continue
                    val type = typeConfig.name
                    val config = ParadoxDefinitionService.resolveDeclaration(element, configGroup, type)

                    context.config = config
                    context.isKey = true
                    context.expressionTailText = ""
                    // 排除正在输入的那一个
                    val selector = selector(project, file).definition().contextSensitive()
                        .notSamePosition(element)
                        .distinctByDefinitionName()
                    ParadoxDefinitionSearch.search(null, type, selector, forFile = false).processAsync {
                        ParadoxCompletionManager.processDefinition(context, result, it)
                    }

                    ParadoxExtendedCompletionManager.completeExtendedDefinition(context, result)
                }
            }
            // event = { id = _ }
            // #131 won't be a number or some type else on completion
            element is ParadoxScriptString && element.isDefinitionName() -> {
                val definition = selectScope { element.parentDefinition() } ?: return
                val definitionInfo = definition.definitionInfo
                if (definitionInfo != null) {
                    val type = definitionInfo.type
                    val config = definitionInfo.declaration ?: return

                    context.config = config
                    context.isKey = false
                    context.expressionTailText = ""
                    // 基于类型键过滤结果
                    // 排除正在输入的那一个
                    val selector = selector(project, file).definition().contextSensitive()
                        .filterBy { it is ParadoxScriptProperty && it.name.equals(definitionInfo.typeKey, true) }
                        .notSamePosition(definition)
                        .distinctByDefinitionName()
                    ParadoxDefinitionSearch.search(null, type, selector, forFile = false).processAsync {
                        ParadoxCompletionManager.processDefinition(context, result, it)
                    }

                    ParadoxExtendedCompletionManager.completeExtendedDefinition(context, result)
                }
            }
        }
    }
}
