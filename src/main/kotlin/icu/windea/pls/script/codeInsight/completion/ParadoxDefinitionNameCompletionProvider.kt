package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
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
		
		fun doAddCompletions(type: String, config: CwtPropertyConfig, isKey: Boolean?, currentDefinition: ParadoxDefinitionProperty?) {
			val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
			val query = ParadoxDefinitionSearch.search(type, project, selector = selector)
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.isKeyKey, isKey)
			context.put(PlsCompletionKeys.contextElementKey, element)
			query.processQuery {
				if(currentDefinition != null && currentDefinition.isSamePosition(it)) return@processQuery true //排除正在输入的 
				processDefinition(context, result, it)
			}
		}
		
		run {
			when {
				//key_ = { ... }
				element is ParadoxScriptPropertyKey -> {
					val definition = element.parent.castOrNull<ParadoxScriptProperty>() ?: return@run
					val definitionInfo = definition.definitionInfo
					if(definitionInfo != null) {
						val type = definitionInfo.type
						val config = definitionInfo.declaration ?: return@run
						doAddCompletions(type, config, true, definition)
					}
				}
				//event = { id = _ }
				element is ParadoxScriptString && element.isDefinitionName() -> {
					val definition = element.findParentDefinition() ?: return@run
					val definitionInfo = definition.definitionInfo
					if(definitionInfo != null) {
						val type = definitionInfo.type
						val config = definitionInfo.declaration ?: return@run
						doAddCompletions(type, config, false, definition)
					}
				}
				//key_
				//key_ = 
				else -> {
					val fileInfo = file.fileInfo ?: return@run
					val gameType = fileInfo.rootInfo.gameType
					val configGroup = getCwtConfig(project).getValue(gameType)
					val path = fileInfo.path
					val elementPath = ParadoxElementPathHandler.resolveFromFile(element, PlsConstants.maxDefinitionDepth) ?: return@run
					for(typeConfig in configGroup.types.values) {
						if(!typeConfig.nameField.isNullOrEmpty()) continue
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
								}
							}
						}
					}
				}
			}
		}
	}
	
	private fun processDefinition(context: ProcessingContext, result: CompletionResultSet, definition: ParadoxDefinitionProperty): Boolean {
		val typeFile = definition.containingFile
		val icon = PlsIcons.Definition
		val definitionInfo = definition.definitionInfo ?: return true
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
