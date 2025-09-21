package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.icon
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.processQueryAsync
import icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.configGroup
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.codeInsight.completion.expressionOffset
import icu.windea.pls.lang.codeInsight.completion.expressionTailText
import icu.windea.pls.lang.codeInsight.completion.forScriptExpression
import icu.windea.pls.lang.codeInsight.completion.isKey
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.codeInsight.completion.offsetInParent
import icu.windea.pls.lang.codeInsight.completion.quoted
import icu.windea.pls.lang.codeInsight.completion.rightQuoted
import icu.windea.pls.lang.codeInsight.completion.withDefinitionLocalizedNamesIfNecessary
import icu.windea.pls.lang.codeInsight.completion.withPatchableIcon
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.filterBy
import icu.windea.pls.lang.search.selector.notSamePosition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionPathManager
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.findParentDefinition
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isDefinitionName

/**
 * 提供定义的名字的代码补全。
 */
class ParadoxDefinitionNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeDefinitionNames) return

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
            //key_
            //key_ =
            //key_ = { ... }
            element is ParadoxScriptPropertyKey || (element is ParadoxScriptString && element.isBlockMember()) -> {
                val fileInfo = file.fileInfo ?: return
                val path = fileInfo.path
                val elementPath = ParadoxExpressionPathManager.get(element, PlsFacade.getInternalSettings().maxDefinitionDepth) ?: return
                if (elementPath.path.isParameterized()) return //忽略表达式路径带参数的情况
                val rootKeyPrefix = lazy { ParadoxExpressionPathManager.getKeyPrefixes(element).firstOrNull() }
                for (typeConfig in configGroup.types.values) {
                    if (typeConfig.nameField != null) continue
                    if (!ParadoxDefinitionManager.matchesTypeByUnknownDeclaration(typeConfig, path, elementPath, null, rootKeyPrefix)) continue
                    val type = typeConfig.name
                    val declarationConfig = configGroup.declarations.get(type) ?: continue
                    //需要考虑不指定子类型的情况
                    val declarationConfigContext = CwtDeclarationConfigContextProvider.getContext(element, null, type, null, gameType, configGroup)
                    val config = declarationConfigContext?.getConfig(declarationConfig) ?: continue

                    context.config = config
                    context.isKey = true
                    context.expressionTailText = ""

                    //排除正在输入的那一个
                    val selector = selector(project, file).definition().contextSensitive()
                        .notSamePosition(element)
                        .distinctByName()
                    ParadoxDefinitionSearch.search(null, type, selector).processQueryAsync p@{ processDefinition(context, result, it) }

                    ParadoxCompletionManager.completeExtendedDefinition(context, result)
                }
            }
            //event = { id = _ }
            //#131 won't be a number or some type else on completion
            element is ParadoxScriptString && element.isDefinitionName() -> {
                val definition = element.findParentDefinition() ?: return
                val definitionInfo = definition.definitionInfo
                if (definitionInfo != null) {
                    val type = definitionInfo.type
                    val config = definitionInfo.declaration ?: return

                    context.config = config
                    context.isKey = false
                    context.expressionTailText = ""

                    //这里需要基于rootKey过滤结果
                    //排除正在输入的那一个
                    val selector = selector(project, file).definition().contextSensitive()
                        .filterBy { it is ParadoxScriptProperty && it.name.equals(definitionInfo.rootKey, true) }
                        .notSamePosition(definition)
                        .distinctByName()
                    ParadoxDefinitionSearch.search(null, type, selector).processQueryAsync p@{ processDefinition(context, result, it) }

                    ParadoxCompletionManager.completeExtendedDefinition(context, result)
                }
            }
        }
    }

    private fun processDefinition(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptDefinitionElement): Boolean {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return true
        if (definitionInfo.name.isEmpty()) return true //ignore anonymous definitions
        val icon = PlsIcons.Nodes.Definition(definitionInfo.type)
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, definitionInfo.name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(icon)
            .withDefinitionLocalizedNamesIfNecessary(element)
            .forScriptExpression(context)
        result.addElement(lookupElement, context)
        return true
    }
}
