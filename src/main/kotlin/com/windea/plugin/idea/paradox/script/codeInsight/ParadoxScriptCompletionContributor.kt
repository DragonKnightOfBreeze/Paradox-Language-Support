package com.windea.plugin.idea.paradox.script.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

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
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			//1. 向上得到最近的definition，从而得到definitionInfo.properties，并且算出相对的definitionPropertyPath
			//2. 根据properties和definitionPropertyPath确定提示结果，注意当匹配子类型时才会加入对应的提示结果，否则不加入
			//3. key可能是：$$类型、$类型、枚举、基本类型int/float/boolean、枚举、字符串
			//4. value可能是：类型表达式、情况列表、子属性（映射）
			//5. 忽略正在填写的propertyName
			val position = parameters.position
			val (definitionInfo, path) = position.resolveDefinitionInfoAndDefinitionPropertyPath() ?: return
			//properties可能有多种情况
			//处理path
			val parentPaths = path.parentSubPaths
			//预匹配properties，得到wildcardKeyStrings
			val wildcardKeyStrings = mutableListOf<String>()
			if(parentPaths.isEmpty()){
				val properties = definitionInfo.properties
				wildcardKeyStrings.addAll(properties.keys)
			}else {
				for(properties in definitionInfo.resolvePropertiesList(parentPaths)) {
					wildcardKeyStrings.addAll(properties.keys)
				}
			}
			//解析wildcardKeyStrings，进行提示
			val lookupElements = mutableListOf<LookupElement>()	
			for(wildcardKeyString in wildcardKeyStrings){
				//TODO 提取方法findByWildcardKeyString
				when{
					//$$类型
					wildcardKeyString.startsWith("$$") -> {
						//TODO
					} 
					//$类型（可能有子类型）
					wildcardKeyString.startsWith("$") ->{
						val (type,subtypes) = wildcardKeyString.drop(1).toTypeExpression()
						val project = position.project
						var matchedDefinitions = findDefinitionsByType(type,project)
						if(subtypes.isNotEmpty()){
							matchedDefinitions = matchedDefinitions.filter { matchedDefinition ->
								val matchedSubtypes = matchedDefinition.paradoxDefinitionInfo?.subtypes
								matchedSubtypes != null && matchedSubtypes.any{ it.name in subtypes }
							}
						}
						matchedDefinitions.mapTo(lookupElements) { 
							LookupElementBuilder.create(it).withIcon(definitionIcon).withTypeText(it.containingFile.name) 
						}
					}  
					//枚举
					wildcardKeyString.startsWith("enum:") -> {
						val gameType = position.paradoxFileInfo?.gameType?:return
						val ruleGroup = paradoxRuleGroups[gameType.key]?:return
						val enum = ruleGroup.enums[wildcardKeyString.drop(5)]?:return
						val enumValues = enum.enumValues
						enumValues.mapTo(lookupElements){
							LookupElementBuilder.create(it).withIcon(scriptPropertyIcon)
						}
					}
					//基本类型（忽略）
					wildcardKeyString == "int" || wildcardKeyString == "float" || wildcardKeyString == "boolean" -> {}
					//字符串
					else -> {
						val lookupElement = LookupElementBuilder.create(wildcardKeyString).withIcon(scriptPropertyIcon)
						lookupElements.add(lookupElement)
					}
				}
			}
			result.addAllElements(lookupElements)
		}
	}
	
	init {
		//当用户正在输入一个string时提示
		extend(CompletionType.BASIC, psiElement(STRING_TOKEN), BooleanCompletionProvider())
		extend(null, or(
			psiElement(PROPERTY_KEY_ID),
			psiElement(QUOTED_PROPERTY_KEY_ID),
			psiElement(STRING_TOKEN).withParent(ParadoxScriptBlock::class.java)
		), DefinitionPropertyNameCompletionProvider())
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
