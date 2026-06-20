package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxExtendedCompletionManager
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.filterBy
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isDefinitionName

/**
 * 提供已有的定义的名字的代码补全。
 */
class ParadoxDefinitionNameCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(KEY_OR_STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsSettings.getInstance().state.completion.completeDefinitionNames) return

        val position = parameters.position
        val element = position.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (element.text.isParameterized()) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext).copy(
            expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)
        )

        when {
            // key_
            // key_ =
            // key_ = { ... }
            element is ParadoxScriptPropertyKey || (element is ParadoxScriptString && element.isBlockMember()) -> {
                val fileInfo = context.file.fileInfo ?: return
                val path = fileInfo.path
                // 忽略 rootKeys 深度超出限制，或者带参数的情况
                val maxDepth = PlsInternalSettings.getInstance().maxDefinitionDepth
                val rootKeys = ParadoxMemberService.getRootKeys(element, maxDepth = maxDepth, parameterAware = false) ?: return
                val typeKeyPrefix = lazy { ParadoxMemberService.getKeyPrefix(element) }
                for (typeConfig in context.configGroup.types.values) {
                    if (typeConfig.nameField != null) continue
                    val matchContext = CwtTypeConfigMatchContext(context.configGroup, path, null, rootKeys, typeKeyPrefix)
                    if (!ParadoxConfigMatchService.matchesTypeByUnknownDeclaration(matchContext, typeConfig)) continue
                    val type = typeConfig.name
                    val config = ParadoxDefinitionService.resolveDeclaration(element, type, configGroup = context.configGroup)

                    run {
                        val context = context.copy(isKey = true, config = config, expressionTailText = "")

                        // 仅限作为属性的定义
                        val selector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
                            .filterBy { it.name != context.keyword } // skip if name = input
                        ParadoxDefinitionSearch.searchProperty(null, type, selector).processAsync {
                            ParadoxExpressionCompletionManager.processDefinition(context, result, it)
                        }

                        ParadoxExtendedCompletionManager.completeExtendedDefinition(context, result)
                    }
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

                    run {
                        val context = context.copy(isKey = false, config = config, expressionTailText = "")

                        // 排除与正在输入的同名的定义
                        // 仅限作为属性的定义
                        val selector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
                            .filterBy { it.name != context.keyword } // skip if name = input
                        ParadoxDefinitionSearch.searchProperty(null, type, selector).processAsync {
                            ParadoxExpressionCompletionManager.processDefinition(context, result, it)
                        }

                        ParadoxExtendedCompletionManager.completeExtendedDefinition(context, result)
                    }
                }
            }
        }
    }
}
