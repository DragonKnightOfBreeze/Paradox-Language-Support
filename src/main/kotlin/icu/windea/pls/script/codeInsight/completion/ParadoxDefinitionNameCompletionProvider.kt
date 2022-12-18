package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的名字的代码补全。
 */
class ParadoxDefinitionNameCompletionProvider : CompletionProvider<CompletionParameters>() {
	@Suppress("KotlinConstantConditions")
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val position = parameters.position
		val element = position.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
		
		val file = parameters.originalFile
		val project = file.project
		val quoted = element.text.isLeftQuoted()
		val rightQuoted = element.text.isRightQuoted()
		val offsetInParent = parameters.offset - element.textRange.startOffset
		val keyword = element.getKeyword(offsetInParent)
		
		context.put(PlsCompletionKeys.completionTypeKey, parameters.completionType)
		context.put(PlsCompletionKeys.contextElementKey, element)
		context.put(PlsCompletionKeys.originalFileKey, file)
		context.put(PlsCompletionKeys.quotedKey, quoted)
		context.put(PlsCompletionKeys.rightQuotedKey, rightQuoted)
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.completionIdsKey, mutableSetOf())
		
		fun doAddCompletions(type: String, config: CwtPropertyConfig, isKey: Boolean?, currentDefinition: ParadoxScriptDefinitionElement?) {
			ProgressManager.checkCanceled()
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.isKeyKey, isKey)
			val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				.notSamePosition(currentDefinition)
				.distinctByName()
			val query = ParadoxDefinitionSearch.search(type, project, selector = selector)
			query.processQuery { processDefinition(context, result, it) }
		}
		
		when {
			//key_ = { ... }
			element is ParadoxScriptPropertyKey -> {
				val definition = element.parent.castOrNull<ParadoxScriptProperty>() ?: return
				val definitionInfo = definition.definitionInfo
				if(definitionInfo != null) {
					val type = definitionInfo.type
					val config = definitionInfo.declaration ?: return
					doAddCompletions(type, config, true, definition)
				}
			}
			//event = { id = _ }
			element is ParadoxScriptString && element.isDefinitionName() -> {
				val definition = element.findParentDefinition() ?: return
				val definitionInfo = definition.definitionInfo
				if(definitionInfo != null) {
					val type = definitionInfo.type
					val config = definitionInfo.declaration ?: return
					doAddCompletions(type, config, false, definition)
				}
			}
			//key_
			//key_ = 
			else -> {
				val fileInfo = file.fileInfo ?: return
				val gameType = fileInfo.rootInfo.gameType
				val configGroup = getCwtConfig(project).getValue(gameType)
				val path = fileInfo.path
				val definitionElement = element.findParentProperty() ?: return
				val elementPath = ParadoxElementPathHandler.resolveFromFile(definitionElement, PlsConstants.maxDefinitionDepth) ?: return
				for(typeConfig in configGroup.types.values) {
					if(typeConfig.nameField != null) continue
					if(ParadoxDefinitionHandler.matchesTypeByPath(typeConfig, path)) {
						val skipRootKeyConfig = typeConfig.skipRootKey
						if(skipRootKeyConfig == null || skipRootKeyConfig.isEmpty()) {
							if(elementPath.isEmpty()) {
								val type = typeConfig.name
								val config = configGroup.declarations[type]?.getMergedConfig() ?: continue
								doAddCompletions(type, config, true, null)
							}
						} else {
							for(skipConfig in skipRootKeyConfig) {
								val relative = elementPath.relativeTo(skipConfig) ?: continue
								if(relative.isEmpty()) {
									val type = typeConfig.name
									val config = configGroup.declarations[type]?.getMergedConfig() ?: continue
									doAddCompletions(type, config, true, null)
								}
								break
							}
						}
					}
				}
			}
		}
	}
	
	private fun processDefinition(context: ProcessingContext, result: CompletionResultSet, definition: ParadoxScriptDefinitionElement): Boolean {
		val typeFile = definition.containingFile
		val definitionInfo = definition.definitionInfo ?: return true
		val icon = PlsIcons.Definition(definitionInfo.type)
		result.addScriptExpressionElement(definition, definitionInfo.name, context,
			icon = icon,
			typeText = typeFile?.name,
			typeIcon = typeFile?.icon
		) {
			this.withPriority(PlsCompletionPriorities.definitionNamePriority)
		}
		return true
	}
}
