package com.windea.plugin.idea.paradox.script.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*
import com.windea.plugin.idea.paradox.util.*
import org.jetbrains.annotations.*

@Suppress("UNCHECKED_CAST")
class ParadoxScriptCompletionContributor : CompletionContributor() {
	class BooleanCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = booleanValues.map { value ->
			LookupElementBuilder.create(value).bold().withPriority(80.0)
		}
		
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(lookupElements)
		}
	}
	
	class DefinitionPropertyNameCompletionProvider : CompletionProvider<CompletionParameters>() {
		//DONE 提示propertyName
		//TODO 提示propertyValue
		//TODO 兼容多种情况&数值类型的propertyValue & 优化规则文件格式
		
		//1. 向上得到最近的definition，从而得到definitionInfo.properties，并且算出相对的definitionPropertyPath
		//2. 根据properties和definitionPropertyPath确定提示结果，注意当匹配子类型时才会加入对应的提示结果，否则不加入
		//3. key可能是：$$类型、$类型、枚举、基本类型int/float、枚举、字符串
		//4. value可能是：类型表达式、情况列表、子属性（映射）
		//5. 忽略正在填写的propertyName
		
		private val insertHandler = InsertHandler<LookupElement> { context, _ ->
			val s = " = "
			EditorModificationUtil.insertStringAtCaret(context.editor, s , false, true, 3)
		}
		
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position
			//block中的string也可能是propertyName
			if(position.elementType == STRING_TOKEN && position.parent?.parent !is ParadoxScriptBlock) return
			
			//寻找definitionInfo和definitionPropertyPath，注意需要使用originalPosition，并且忽略正在补全的字符串
			val parentBlock = parameters.originalPosition?.parentOfType<ParadoxScriptBlock>()
			val (definitionInfo, path) = parentBlock?.resolveDefinitionInfoAndDefinitionPropertyPath() ?: return
			
			//properties可能有多种情况
			//处理path
			val subpaths = path.subpaths
			//预匹配properties，得到keyPatterns
			val keyPatternExpressions = mutableListOf<ConditionalExpression>()
			if(subpaths.isEmpty()){
				val properties = definitionInfo.properties
				properties.keys.mapTo(keyPatternExpressions){ it.toConditionalExpression() }
			}else {
				for(properties in definitionInfo.resolvePropertiesList(subpaths)) {
					properties.keys.mapTo(keyPatternExpressions){ it.toConditionalExpression() }
				}
			}
			
			//一般来说key不能重复
			val existPropertyNames by lazy { 
				(position.parentOfType<ParadoxScriptProperty>()?.propertyValue?.value as? ParadoxScriptBlock)
					?.propertyList?.mapTo(mutableSetOf()) { it.name } ?: emptyList() 
			}
			val gameType = position.paradoxFileInfo?.gameType?:return
			val ruleGroup = paradoxRuleGroups[gameType.key]?:return
			val project = position.project
			
			//解析keyPatterns，进行提示
			val lookupElements = mutableListOf<LookupElement>()	
			resolveKeyPatternExpressions(keyPatternExpressions, project, existPropertyNames, lookupElements, ruleGroup)
			result.addAllElements(lookupElements)
		}
		
		private fun resolveKeyPatternExpressions(keyPatternExpressions: List<ConditionalExpression>, project: Project,
			existPropertyNames: Collection<String>, lookupElements: MutableList<LookupElement>, ruleGroup: ParadoxRuleGroup) {
			for(keyPatternExpression in keyPatternExpressions) {
				val (keyPattern, _, _, multiple) = keyPatternExpression
				when {
					//$$类型
					keyPattern.startsWith("$$") -> {
						//TODO
					}
					//$类型（可能有子类型）
					keyPattern.startsWith("$") -> {
						val (type, subtypes) = keyPattern.drop(1).toTypeExpression()
						var matchedDefinitions = findDefinitionsByType(type, project)
						if(subtypes.isNotEmpty()) {
							matchedDefinitions = matchedDefinitions.filter { matchedDefinition ->
								val matchedSubtypes = matchedDefinition.paradoxDefinitionInfo?.subtypes
								matchedSubtypes != null && matchedSubtypes.any { it.name in subtypes }
							}
						}
						//如果不是multiple并且keyPattern的KeyString被识别，则这里不提示（一般来说都是multiple）
						if(!multiple && matchedDefinitions.any { it.name in existPropertyNames }) return
						matchedDefinitions.mapTo(lookupElements) {
							LookupElementBuilder.create(it).withIcon(definitionIcon).withTypeText(it.containingFile.name).withInsertHandler(insertHandler)
						}
					}
					//枚举
					keyPattern.startsWith("enum:") -> {
						val enum = ruleGroup.enums[keyPattern.drop(5)] ?: return
						val enumValues = enum.enumValues
						//如果不是multiple并且keyPattern的KeyString被识别，则这里不提示
						if(!multiple && enumValues.any { it in existPropertyNames }) return
						enumValues.mapTo(lookupElements) {
							LookupElementBuilder.create(it).withIcon(scriptPropertyIcon).withInsertHandler(insertHandler)
						}
					}
					//基本类型int/float
					keyPattern == "int" || keyPattern == "float" -> {
						//忽略
					}
					//字符串
					else -> {
						//如果不是multiple并且keyPattern的KeyString被识别，则这里不提示
						if(!multiple && keyPattern in existPropertyNames) return
						val lookupElement = LookupElementBuilder.create(keyPattern).withIcon(scriptPropertyIcon).withInsertHandler(insertHandler)
						lookupElements.add(lookupElement)
					}
				}
			}
		}
	}
	
	init {
		//当用户正在输入一个string时提示
		extend(CompletionType.BASIC, psiElement(STRING_TOKEN), BooleanCompletionProvider())
		extend(null, or(
			psiElement(PROPERTY_KEY_ID),
			psiElement(QUOTED_PROPERTY_KEY_ID),
			psiElement(STRING_TOKEN)
		), DefinitionPropertyNameCompletionProvider())
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
