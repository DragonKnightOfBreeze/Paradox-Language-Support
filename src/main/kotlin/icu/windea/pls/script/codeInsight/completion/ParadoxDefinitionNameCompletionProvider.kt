package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的名字的代码补全。
 */
class ParadoxDefinitionNameCompletionProvider : CompletionProvider<CompletionParameters>() {
	@Suppress("KotlinConstantConditions")
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		if(!getSettings().completion.completeDefinitionNames) return
		
		val element = parameters.position.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
		
		val file = parameters.originalFile
		val project = file.project
		val quoted = element.text.isLeftQuoted()
		val rightQuoted = element.text.isRightQuoted()
		val offsetInParent = parameters.offset - element.startOffset
		val keyword = element.getKeyword(offsetInParent)
		
		context.put(PlsCompletionKeys.completionTypeKey, parameters.completionType)
		context.put(PlsCompletionKeys.contextElementKey, element)
		context.put(PlsCompletionKeys.originalFileKey, file)
		context.put(PlsCompletionKeys.quotedKey, quoted)
		context.put(PlsCompletionKeys.rightQuotedKey, rightQuoted)
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.completionIdsKey, mutableSetOf())
		
		fun doAddCompletions(type: String, config: CwtPropertyConfig, isKey: Boolean?, currentElement: PsiElement, rootKey: String?) {
			ProgressManager.checkCanceled()
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.isKeyKey, isKey)
			//排除正在输入的那一个
			val selector = definitionSelector(project, file).contextSensitive()
				.filterBy { rootKey == null || (it is ParadoxScriptProperty && it.name.equals(rootKey, true)) }
				.notSamePosition(currentElement)
				.distinctByName()
			ParadoxDefinitionSearch.search(type, selector).processQuery { processDefinition(context, result, it) }
		}
		
		when {
			//key_
			//key_ = 
			//key_ = { ... }
			element is ParadoxScriptPropertyKey || (element is ParadoxScriptString && element.isBlockValue()) -> {
				val fileInfo = file.fileInfo ?: return
				val gameType = fileInfo.rootInfo.gameType
				val configGroup = getCwtConfig(project).getValue(gameType)
				val path = fileInfo.entryPath //这里使用entryPath
				val elementPath = ParadoxElementPathHandler.getFromFile(element, PlsConstants.maxDefinitionDepth) ?: return
				for(typeConfig in configGroup.types.values) {
					if(typeConfig.nameField != null) continue
					if(ParadoxDefinitionHandler.matchesTypeWithUnknownDeclaration(typeConfig, path, elementPath, null)) {
						val type = typeConfig.name
						//需要考虑不指定子类型的情况
						val configContext = CwtConfigContext(element, null, type, null, configGroup)
						val config = configGroup.declarations[type]?.getMergedConfig(configContext) ?: continue
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
		val typeFile = definition.containingFile
		val definitionInfo = definition.definitionInfo ?: return true
		val icon = PlsIcons.Definition(definitionInfo.type)
		val builder = ParadoxScriptExpressionLookupElementBuilder.create(definition, definitionInfo.name)
			.withIcon(icon)
			.withTypeText(typeFile?.name)
			.withTypeIcon(typeFile?.icon)
			.withPriority(PlsCompletionPriorities.definitionNamePriority)
		result.addScriptExpressionElement(context, builder)
		return true
	}
}
