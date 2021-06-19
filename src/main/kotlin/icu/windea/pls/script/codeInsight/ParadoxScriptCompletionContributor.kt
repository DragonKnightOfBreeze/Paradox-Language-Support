package icu.windea.pls.script.codeInsight

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptTypes.*

@Suppress("UNCHECKED_CAST")
class ParadoxScriptCompletionContributor : CompletionContributor() {
	companion object {
		private val stringPattern = psiElement(STRING_TOKEN)
		private val definitionPattern = or(
			psiElement(PROPERTY_KEY_ID), psiElement(QUOTED_PROPERTY_KEY_ID),
			psiElement(STRING_TOKEN), psiElement(QUOTED_STRING_TOKEN)
		)
		//private val propertyNamePattern = and(
		//	psiElement().withParent(ParadoxScriptBlock::class.java),
		//	or(psiElement(PROPERTY_KEY_ID), psiElement(QUOTED_PROPERTY_KEY_ID), psiElement(STRING_TOKEN), psiElement(QUOTED_STRING_TOKEN))
		//)
		//private val propertyValuePattern = or(psiElement(STRING_TOKEN), psiElement(QUOTED_STRING_TOKEN))
		
		private val booleanLookupElements = booleanValues.map { value ->
			LookupElementBuilder.create(value).bold().withPriority(80.0)
		}
		private val separatorInsertHandler = InsertHandler<LookupElement> { context, _ ->
			val customSettings = CodeStyle.getCustomSettings(context.file, ParadoxScriptCodeStyleSettings::class.java)
			val spaceAroundSeparator = customSettings.SPACE_AROUND_SEPARATOR
			val separator = if(spaceAroundSeparator) " = " else "="
			EditorModificationUtil.insertStringAtCaret(context.editor, separator)
		}
	}
	
	class BooleanCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(booleanLookupElements)
		}
	}
	
	class DefinitionCompletionProvider : CompletionProvider<CompletionParameters>() {
		//	//DONE 提示propertyName
		//	//TODO 提示propertyValue
		//	//TODO 兼容多种情况&数值类型的propertyValue & 优化规则文件格式
		//	
		//	//1. 向上得到最近的definition，从而得到definitionInfo.properties，并且算出相对的definitionPropertyPath
		//	//2. 根据properties和definitionPropertyPath确定提示结果，注意当匹配子类型时才会加入对应的提示结果，否则不加入
		//	//3. key可能是：基本类型、类型、枚举、别名、枚举、字符串等
		//	//4. value可能是：类型表达式、情况列表、子属性（映射）
		//	//5. 忽略正在填写的propertyName
		//	
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position
			val originalPosition = parameters.originalPosition ?: return
			val project = originalPosition.project
			val positionParent = position.parent ?: return //propertyKey | value
			val positionParentParent = positionParent.parent ?: return //property | propertyValue | block
			val positionParentParentParent = positionParentParent.parent ?: return //block | property
			val mayBePropertyKey = positionParentParentParent is ParadoxScriptBlock
			val mayBePropertyValue = positionParentParent is ParadoxScriptPropertyValue 
			val mayBeValue = positionParent is ParadoxScriptValue
			
			if(mayBePropertyKey) {
				val block = positionParentParentParent as? ParadoxScriptBlock?:return
				val definitionProperty = block.findParentDefinitionProperty()?:return
				val definitionPropertyInfo = definitionProperty.paradoxDefinitionPropertyInfo?:return
				val path = definitionPropertyInfo.path
				val existPropertyNames = definitionPropertyInfo.existPropertyNames
				val definitionInfo = definitionPropertyInfo.definitionInfo
				val type = definitionInfo.type
				val subtypes = definitionInfo.subtypes
				val gameType = definitionInfo.fileInfo.gameType
				val definitionConfig = getConfig(project).getValue(gameType).definitions.getValue(type).mergeConfig(subtypes)
				
				//如果path是空的，表示需要补全definition的顶级属性
				if(path.isEmpty()) {
					for((propName,propConfig) in definitionConfig) {
						//如果cardinality的最小值为0，或者属性名并非已存在，则提示（cardinality未声明也提示）
						if(propConfig.cardinality?.min != 0 || propName !in existPropertyNames){
							val propElement = propConfig.pointer.element?:context
							val lookupElement = LookupElementBuilder.create(propElement,propName)
							result.addElement(lookupElement)
						}
					}
				}
			}
			//		val position = parameters.position
			//		//block中的string也可能是propertyName
			//		if(position.elementType == STRING_TOKEN && position.parent?.parent !is ParadoxScriptBlock) return
			//		
			//		//寻找definitionInfo和definitionPropertyPath，注意需要使用originalPosition，并且忽略正在补全的字符串
			//		val parentBlock = parameters.originalPosition?.parentOfType<ParadoxScriptBlock>()
			//		val (definitionInfo, path) = parentBlock?.resolveDefinitionInfoAndDefinitionPropertyPath() ?: return
			//		
			//		//properties可能有多种情况
			//		//处理path
			//		val subPaths = path.subPaths
			//		//预匹配properties，得到keyPatterns
			//		val keyPatternExpressions = mutableListOf<ConditionalExpression>()
			//		if(subPaths.isEmpty()){
			//			val properties = definitionInfo.properties
			//			properties.keys.mapTo(keyPatternExpressions) { ConditionalExpression(it) }
			//		}else {
			//			for(properties in definitionInfo.resolvePropertiesList(subPaths)) {
			//				properties.keys.mapTo(keyPatternExpressions) { ConditionalExpression(it) }
			//			}
			//		}
			//		
			//		//一般来说key不能重复
			//		val existPropertyNames by lazy { 
			//			(position.parentOfType<ParadoxScriptProperty>()?.propertyValue?.value as? ParadoxScriptBlock)
			//				?.propertyList?.mapTo(mutableSetOf()) { it.name }.orEmpty() 
			//		}
			//		val gameType = parameters.originalFile.paradoxFileInfo?.gameType?:return
			//		val ruleGroup = rule.ruleGroups[gameType.key]?:return
			//		val project = parameters.originalFile.project
			//		
			//		//解析keyPatterns，进行提示
			//		val lookupElements = resolveExpressions(keyPatternExpressions, project, existPropertyNames, ruleGroup)
			//		result.addAllElements(lookupElements)
		}
		//	
		//	private fun resolveExpressions(expressions: List<ConditionalExpression>, project: Project, 
		//		existPropertyNames: Collection<String>, ruleGroup: ParadoxRuleGroup
		//	): List<LookupElement> {
		//		val lookupElements = mutableListOf<LookupElement>()
		//		for(keyPatternExpression in expressions) {
		//			val (keyPattern, _, _, multiple) = keyPatternExpression
		//			when {
		//				//别名
		//				keyPattern.startsWith(aliasPrefix) -> {
		//					//TODO
		//				}
		//				//类型
		//				keyPattern.startsWith(primitivePrefix) -> {
		//					val key = keyPattern.drop(primitivePrefixLength)
		//					if(key == "boolean") lookupElements.addAll(booleanLookupElements)
		//				}
		//				//定义
		//				keyPattern.startsWith(typePrefix) -> {
		//					val key = keyPattern.drop(typePrefixLength)
		//					val (type, subtypes) = TypeExpression(key)
		//					var matchedDefinitions = findDefinitionsByType(type, project)
		//					if(subtypes.isNotEmpty()) {
		//						matchedDefinitions = matchedDefinitions.filter { matchedDefinition ->
		//							val matchedSubtypes = matchedDefinition.paradoxDefinitionInfo?.subtypes
		//							matchedSubtypes != null && matchedSubtypes.any { it.name in subtypes }
		//						}
		//					}
		//					//如果不是multiple并且keyPattern的KeyString被识别，则这里不提示（一般来说都是multiple）
		//					if(!multiple && matchedDefinitions.any { it.name in existPropertyNames }) break
		//					matchedDefinitions.mapTo(lookupElements) {
		//						LookupElementBuilder.create(it).withIcon(definitionIcon)
		//							.withTypeText(it.containingFile.name).withInsertHandler(separatorInsertHandler)
		//					}
		//				}
		//				//枚举
		//				keyPattern.startsWith(enumPrefix) -> {
		//					val key = keyPattern.drop(enumPrefixLength)
		//					val enum = ruleGroup.enums[key] ?: break
		//					val enumValues = enum.enumValues
		//					//如果不是multiple并且keyPattern的KeyString被识别，则这里不提示
		//					if(!multiple && enumValues.any { it in existPropertyNames }) break
		//					enumValues.mapTo(lookupElements) {
		//						LookupElementBuilder.create(it).withIcon(scriptPropertyIcon).withInsertHandler(separatorInsertHandler)
		//					}
		//				}
		//				//字符串
		//				else -> {
		//					//如果不是multiple并且keyPattern的KeyString被识别，则这里不提示
		//					if(!multiple && keyPattern in existPropertyNames) break
		//					val lookupElement = LookupElementBuilder.create(keyPattern)
		//						.withIcon(scriptPropertyIcon).withInsertHandler(separatorInsertHandler)
		//					lookupElements.add(lookupElement)
		//				}
		//			}
		//		}
		//		return lookupElements
		//	}
	}
	
	init {
		//当用户正在输入一个string时提示
		extend(CompletionType.BASIC, stringPattern, BooleanCompletionProvider())
		//当用户正在输入一个propertyKey或string时提示
		extend(
			null, definitionPattern, DefinitionCompletionProvider()
		)
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
