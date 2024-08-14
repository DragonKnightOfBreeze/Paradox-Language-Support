package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icons.*
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
        if(!getSettings().completion.completeDefinitionNames) return
        
        val position = parameters.position
        val element = position.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if(element.text.isParameterized()) return
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
        context.expressionOffset = ParadoxExpressionHandler.getExpressionOffset(element)
        
        val gameType = selectGameType(file) ?: return
        val configGroup = getConfigGroup(project, gameType)
        context.configGroup = configGroup
        
        when {
            //key_
            //key_ = 
            //key_ = { ... }
            element is ParadoxScriptPropertyKey || (element is ParadoxScriptString && element.isBlockMember()) -> {
                val fileInfo = file.fileInfo ?: return
                val path = fileInfo.pathToEntry //这里使用pathToEntry
                val elementPath = ParadoxElementPathHandler.get(element, PlsConstants.maxDefinitionDepth) ?: return
                if(elementPath.path.isParameterized()) return //忽略元素路径带参数的情况
                for(typeConfig in configGroup.types.values) {
                    if(typeConfig.nameField != null) continue
                    if(ParadoxDefinitionHandler.matchesTypeByUnknownDeclaration(path, elementPath, null, typeConfig)) {
                        val type = typeConfig.name
                        val declarationConfig = configGroup.declarations.get(type) ?: continue
                        //需要考虑不指定子类型的情况
                        val configContext = CwtDeclarationConfigContextProvider.getContext(element, null, type, null, gameType, configGroup)
                        val config = configContext?.getConfig(declarationConfig) ?: continue
                        
                        context.config = config
                        context.isKey = true
                        context.expressionTailText = ""
                        
                        //排除正在输入的那一个
                        val selector = definitionSelector(project, file).contextSensitive()
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
                if(definitionInfo != null) {
                    val type = definitionInfo.type
                    val config = definitionInfo.declaration ?: return
                    
                    context.config = config
                    context.isKey = false
                    context.expressionTailText = ""
                    
                    //这里需要基于rootKey过滤结果
                    //排除正在输入的那一个
                    val selector = definitionSelector(project, file).contextSensitive()
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
        if(definitionInfo.name.isEmpty()) return true //ignore anonymous definitions
        val icon = PlsIcons.Nodes.Definition(definitionInfo.type)
        val typeFile = definition.containingFile
        val lookupElement = ParadoxLookupElementBuilder.create(definition, definitionInfo.name)
            .withIcon(icon)
            .withTypeText(typeFile?.name)
            .withTypeIcon(typeFile?.icon)
            .letIf(getSettings().completion.completeByLocalizedName) {
                //如果启用，也基于定义的本地化名字进行代码补全
                ProgressManager.checkCanceled()
                val localizedNames = ParadoxDefinitionHandler.getLocalizedNames(definition)
                it.withLocalizedNames(localizedNames)
            }
            .build(context)
        result.addElement(lookupElement)
        return true
    }
}
