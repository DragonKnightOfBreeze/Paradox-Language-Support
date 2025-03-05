package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的名字的代码补全。
 */
class ParadoxDefinitionNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!getSettings().completion.completeDefinitionNames) return

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
        val configGroup = getConfigGroup(project, gameType)
        context.configGroup = configGroup

        when {
            //key_
            //key_ =
            //key_ = { ... }
            element is ParadoxScriptPropertyKey || (element is ParadoxScriptString && element.isBlockMember()) -> {
                val fileInfo = file.fileInfo ?: return
                val path = fileInfo.path
                val elementPath = ParadoxExpressionPathManager.get(element, PlsConstants.Settings.maxDefinitionDepth) ?: return
                if (elementPath.path.isParameterized()) return //忽略表达式路径带参数的情况
                for (typeConfig in configGroup.types.values) {
                    if (typeConfig.nameField != null) continue
                    if (ParadoxDefinitionManager.matchesTypeByUnknownDeclaration(path, elementPath, null, typeConfig)) {
                        val type = typeConfig.name
                        val declarationConfig = configGroup.declarations.get(type) ?: continue
                        //需要考虑不指定子类型的情况
                        val configContext = CwtDeclarationConfigContextProvider.getContext(element, null, type, null, gameType, configGroup)
                        val config = configContext?.getConfig(declarationConfig) ?: continue

                        context.config = config
                        context.isKey = true
                        context.expressionTailText = ""

                        //排除正在输入的那一个
                        val selector = selector(project, file).definition().contextSensitive()
                            .notSamePosition(element)
                            .distinctByName()
                        ParadoxDefinitionSearch.search(type, selector).processQueryAsync p@{ processDefinition(context, result, it) }

                        ParadoxCompletionManager.completeExtendedDefinition(context, result)
                    }
                }
            }
            //event = { id = _ }
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
                    ParadoxDefinitionSearch.search(type, selector).processQueryAsync p@{ processDefinition(context, result, it) }

                    ParadoxCompletionManager.completeExtendedDefinition(context, result)
                }
            }
        }
    }

    private fun processDefinition(context: ProcessingContext, result: CompletionResultSet, definition: ParadoxScriptDefinitionElement): Boolean {
        ProgressManager.checkCanceled()
        val definitionInfo = definition.definitionInfo ?: return true
        if (definitionInfo.name.isEmpty()) return true //ignore anonymous definitions
        val icon = PlsIcons.Nodes.Definition(definitionInfo.type)
        val typeFile = definition.containingFile
        val lookupElement = LookupElementBuilder.create(definition, definitionInfo.name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(icon)
            .withDefinitionLocalizedNamesIfNecessary(definition)
            .forScriptExpression(context)
        result.addElement(lookupElement, context)
        return true
    }
}
