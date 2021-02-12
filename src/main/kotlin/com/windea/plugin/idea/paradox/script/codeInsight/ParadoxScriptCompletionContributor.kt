package com.windea.plugin.idea.paradox.script.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.*
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
		//1. 向上得到最近的definition，从而得到definitionInfo.properties，并且算出相对的definitionPropertyPath
		//2. 根据properties和definitionPropertyPath确定提示结果，注意当匹配子类型时才会加入对应的提示结果，否则不加入
		//3. key可能是：$$类型、$类型、枚举、基本类型int/float/boolean、枚举、字符串
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
			//预匹配properties，得到wildcardKeyStrings
			val wildcardKeyExpressions = mutableListOf<ConditionalExpression>()
			if(subpaths.isEmpty()){
				val properties = definitionInfo.properties
				properties.keys.mapTo(wildcardKeyExpressions){ it.toConditionalExpression() }
			}else {
				for(properties in definitionInfo.resolvePropertiesList(subpaths)) {
					properties.keys.mapTo(wildcardKeyExpressions){ it.toConditionalExpression() }
				}
			}
			
			//一般来说key不能重复
			val existPropertyNames by lazy { 
				(position.parentOfType<ParadoxScriptProperty>()?.propertyValue?.value as? ParadoxScriptBlock)
					?.propertyList?.mapTo(mutableSetOf()) { it.name } ?: emptyList() 
			}
			
			//解析wildcardKeyStrings，进行提示
			val lookupElements = mutableListOf<LookupElement>()	
			for(wildcardKeyExpression in wildcardKeyExpressions){
				val (wildcardKeyString,_,_,multiple) = wildcardKeyExpression
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
						//如果不是multiple并且wildcard的KeyString被识别，则这里不提示（一般来说都是multiple）
						if(!multiple && matchedDefinitions.any { it.name in existPropertyNames }) continue
						matchedDefinitions.mapTo(lookupElements) { 
							LookupElementBuilder.create(it).withIcon(definitionIcon).withTypeText(it.containingFile.name).withInsertHandler(insertHandler)
						}
					}  
					//枚举
					wildcardKeyString.startsWith("enum:") -> {
						val gameType = position.paradoxFileInfo?.gameType?:return
						val ruleGroup = paradoxRuleGroups[gameType.key]?:return
						val enum = ruleGroup.enums[wildcardKeyString.drop(5)]?:return
						val enumValues = enum.enumValues
						//如果不是multiple并且wildcard的KeyString被识别，则这里不提示
						if(!multiple && enumValues.any { it in existPropertyNames }) continue
						enumValues.mapTo(lookupElements){
							LookupElementBuilder.create(it).withIcon(scriptPropertyIcon).withInsertHandler(insertHandler)
						}
					}
					//基本类型（忽略）
					wildcardKeyString == "int" || wildcardKeyString == "float" || wildcardKeyString == "boolean" -> {}
					//字符串
					else -> {
						//如果不是multiple并且wildcard的KeyString被识别，则这里不提示
						if(!multiple && wildcardKeyString in existPropertyNames) continue
						val lookupElement = LookupElementBuilder.create(wildcardKeyString).withIcon(scriptPropertyIcon).withInsertHandler(insertHandler)
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
			psiElement(STRING_TOKEN)
		), DefinitionPropertyNameCompletionProvider())
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
