package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的名字的代码补全。
 */
class ParadoxDefinitionNameCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		if(!getSettings().completion.completeDefinitionNames) return
		
		val element = parameters.position.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
		
		val file = parameters.originalFile
		val project = file.project
		val quoted = element.text.isLeftQuoted()
		val rightQuoted = element.text.isRightQuoted()
		val offsetInParent = parameters.offset - element.startOffset
		val keyword = element.getKeyword(offsetInParent)
		
		context.completionIds = mutableSetOf<String>().synced()
		context.parameters = parameters
		context.contextElement = element
		context.originalFile = file
		context.offsetInParent = offsetInParent
		context.keyword = keyword
		context.quoted = quoted
		context.rightQuoted = rightQuoted
		
		fun doAddCompletions(type: String, config: CwtPropertyConfig, isKey: Boolean?, currentElement: PsiElement, rootKey: String?) {
			ProgressManager.checkCanceled()
			context.config = config
			context.isKey = isKey
			//排除正在输入的那一个
			val selector = definitionSelector(project, file).contextSensitive()
				.filterBy { rootKey == null || (it is ParadoxScriptProperty && it.name.equals(rootKey, true)) }
				.notSamePosition(currentElement)
				.distinctByName()
			ParadoxDefinitionSearch.search(type, selector).processQueryAsync p@{ processDefinition(context, result, it) }
		}
		
		when {
			//key_
			//key_ = 
			//key_ = { ... }
			element is ParadoxScriptPropertyKey || (element is ParadoxScriptString && element.isBlockValue()) -> {
				val fileInfo = file.fileInfo ?: return
				val gameType = fileInfo.rootInfo.gameType
				val configGroup = getCwtConfig(project).get(gameType)
				val path = fileInfo.pathToEntry //这里使用pathToEntry
				val elementPath = ParadoxElementPathHandler.get(element, PlsConstants.maxDefinitionDepth) ?: return
				for(typeConfig in configGroup.types.values) {
					if(typeConfig.nameField != null) continue
					if(ParadoxDefinitionHandler.matchesTypeWithUnknownDeclaration(path, elementPath, null, typeConfig)) {
						val type = typeConfig.name
						//需要考虑不指定子类型的情况
						val configContext = CwtDeclarationConfigContext(element, null, type, null, configGroup)
						val config = configGroup.declarations.get(type)?.getConfig(configContext) ?: continue
						doAddCompletions(type, config, true, element, null)
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
					//这里需要基于rootKey过滤结果
					doAddCompletions(type, config, false, element, definitionInfo.rootKey)
				}
			}
		}
	}
	
	private fun processDefinition(context: ProcessingContext, result: CompletionResultSet, definition: ParadoxScriptDefinitionElement): Boolean {
		ProgressManager.checkCanceled()
		val definitionInfo = definition.definitionInfo ?: return true
		if(definitionInfo.name.isEmpty()) return true //ignore anonymous definitions
		val icon = PlsIcons.Definition(definitionInfo.type)
		val typeFile = definition.containingFile
		val builder = ParadoxScriptExpressionLookupElementBuilder.create(definition, definitionInfo.name)
			.withIcon(icon)
			.withTypeText(typeFile?.name)
			.withTypeIcon(typeFile?.icon)
			.withPriority(PlsCompletionPriorities.definitionNamePriority)
			.letIf(getSettings().completion.completeByLocalizedName) {
				//如果启用，也基于定义的本地化名字进行代码补全
				ProgressManager.checkCanceled()
				val localizedNames = ParadoxDefinitionHandler.getLocalizedNames(definition)
				it.withLocalizedNames(localizedNames)
			}
		result.addScriptExpressionElement(context, builder)
		return true
	}
}
