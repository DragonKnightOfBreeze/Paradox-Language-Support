package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.model.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

@Suppress("UNCHECKED_CAST")
class ParadoxScriptAnnotator : Annotator, DumbAware {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is ParadoxScriptProperty -> annotateProperty(element, holder)
			is ParadoxScriptVariableReference -> annotateVariableReference(element, holder)
			is ParadoxScriptString -> annotateString(element, holder)
		}
	}
	
	private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
		//如果是定义，则
		val definitionInfo = element.paradoxDefinitionInfo
		if(definitionInfo != null) annotateDefinition(element, holder,definitionInfo)
	}
	
	private fun annotateDefinition(element: ParadoxScriptProperty, holder: AnnotationHolder, definitionInfo: ParadoxDefinitionInfo) {
		holder.newSilentAnnotation(INFORMATION)
			.range(element.propertyKey)
			.textAttributes(ParadoxScriptAttributesKeys.DEFINITION_KEY)
			.create()
		
		//检查definitionProperty的名字、数量是否合法，第一次不合法不会中断检查
		//val existProperties = (element.propertyValue?.value as? ParadoxScriptBlock)?.propertyList
		//if(!existProperties.isNullOrEmpty()){
		//	val properties = definitionInfo.properties
		//	val keyPatternExpressions = mutableListOf<ConditionalExpression>()
		//	properties.keys.mapTo(keyPatternExpressions) { ConditionalExpression(it) }
		//	
		//	val gameType = element.paradoxFileInfo?.gameType?:return
		//	val configGroup = config[gameType]
		//	val project = element.project
		//	
		//	//遍历existProperties
		//	annotateDefinitionProperties(existProperties, keyPatternExpressions, project, definitionInfo, configGroup, holder)
		//}
	}
	
	//private fun annotateDefinitionProperties(existProperties:List<ParadoxScriptProperty>,
	//	keyPatternExpressions: List<ConditionalExpression>,project: Project, definitionInfo: ParadoxDefinitionInfo, 
	//	ruleGroup: ParadoxRuleGroup, holder: AnnotationHolder,subpaths:List<String> = emptyList()){
	//	
	//	//用于保存要求的pattern
	//	val requiredData = keyPatternExpressions.filter { it.required }.mapTo(mutableSetOf()) { it.value }
	//	//用于检查不可重复的keyPattern是否重复 
	//	val multipleData = mutableSetOf<String>()
	//	
	//	loop@ for(existProperty in existProperties){
	//		//如果definitionProperty本身就是definition，则跳过检查
	//		if(existProperty.paradoxDefinitionInfo != null) continue
	//		
	//		val existPropertyName = existProperty.name
	//		val typeText = definitionInfo.typeText
	//		val childSubpaths = subpaths + existPropertyName
	//		
	//		val state = resolveExpressions(keyPatternExpressions, project, existPropertyName, ruleGroup,requiredData,multipleData)
	//		
	//		//如果有问题
	//		when(state) {
	//			ValidateState.Unresolved -> {
	//				holder.newAnnotation(ERROR, message("paradox.script.annotator.unresolvedDefinitionProperty", existPropertyName, typeText))
	//					.range(existProperty.propertyKey)
	//					.create()
	//				continue@loop
	//			}
	//			ValidateState.Dupliate -> {
	//				holder.newAnnotation(ERROR, message("paradox.script.annotator.duplicateDefinitionProperty", existPropertyName, typeText))
	//					.range(existProperty.propertyKey)
	//					.create()
	//				continue@loop
	//			}
	//			else -> {}
	//		}
	//		
	//		//如果有缺失的属性
	//		if(requiredData.isNotEmpty()){
	//			for(name in requiredData) {
	//				holder.newAnnotation(ERROR, message("paradox.script.annotator.missingDefinitionProperty", name, typeText))
	//					.range(existProperty.propertyKey)
	//					.create()
	//			}
	//		}
	//		
	//		//没有问题再去检查可能的子属性
	//		val childExistProperties = (existProperty.propertyValue?.value as? ParadoxScriptBlock)?.propertyList
	//		if(!childExistProperties.isNullOrEmpty()) {
	//			val childKeyPatternExpressions = mutableListOf<ConditionalExpression>()
	//			for(childProperties in definitionInfo.resolvePropertiesList(mutableListOf(existProperty.name))) {
	//				childProperties.keys.mapTo(childKeyPatternExpressions) { ConditionalExpression(it) }
	//			}
	//			//递归检查
	//			//TODO 重构
	//			annotateDefinitionProperties(childExistProperties, childKeyPatternExpressions, project, definitionInfo, ruleGroup, holder,childSubpaths )
	//		}
	//	}
	//}
	
	//private fun resolveExpressions(expressions: List<ConditionalExpression>, project: Project,
	//	existPropertyName: String, ruleGroup: ParadoxRuleGroup, requiredData: MutableSet<String>,multipleData: MutableSet<String>): ValidateState {
	//	for(keyPatternExpression in expressions) {
	//		val keyPattern = keyPatternExpression.value
	//		when {
	//			//别名
	//			keyPattern.startsWith(aliasPrefix) -> {
	//				//TODO
	//				return afterResolved(keyPatternExpression, requiredData, multipleData)
	//			}
	//			//类型
	//			keyPattern.startsWith(primitivePrefix) -> {
	//				val key = keyPattern.drop(primitivePrefixLength)
	//				if(existPropertyName.isTypeOf(key)){
	//					return afterResolved(keyPatternExpression, requiredData, multipleData)
	//				}
	//			}
	//			//定义
	//			keyPattern.startsWith(typePrefix) -> {
	//				val key = keyPattern.drop(typePrefixLength)
	//				val (type, subtypes) = TypeExpression(key)
	//				var matchedDefinitions = findDefinitions(existPropertyName, type, project)
	//				if(subtypes.isNotEmpty()) {
	//					matchedDefinitions = matchedDefinitions.filter { matchedDefinition ->
	//						val matchedSubtypes = matchedDefinition.paradoxDefinitionInfo?.subtypes
	//						matchedSubtypes != null && matchedSubtypes.any { it.name in subtypes }
	//					}
	//				}
	//				if(matchedDefinitions.isNotEmpty()) {
	//					return afterResolved(keyPatternExpression, requiredData, multipleData)
	//				}
	//			}
	//			//枚举
	//			keyPattern.startsWith(enumPrefix) -> {
	//				val key = keyPattern.drop(enumPrefixLength)
	//				val enum = ruleGroup.enums[key] ?: continue
	//				val enumValues = enum.enumValues
	//				if(existPropertyName in enumValues) {
	//					return afterResolved(keyPatternExpression, requiredData, multipleData)
	//				}
	//			}
	//			//字符串
	//			else -> {
	//				if(existPropertyName == keyPattern) {
	//					return afterResolved(keyPatternExpression, requiredData, multipleData)
	//				}
	//			}
	//		}
	//	}
	//	return ValidateState.Unresolved
	//}
	//
	//private fun afterResolved(keyPatternExpression:ConditionalExpression, requiredData: MutableSet<String>,multipleData: MutableSet<String>): ValidateState {
	//	val (keyPattern,_,required,multiple) = keyPatternExpression
	//	if(!multiple) {
	//		if(keyPattern in multipleData) {
	//			return ValidateState.Dupliate
	//		} else {
	//			multipleData.add(keyPattern)
	//		}
	//	}
	//	if(required){
	//		requiredData.remove(keyPattern)
	//	}
	//	return ValidateState.Ok
	//}
	
	private fun annotateVariableReference(element: ParadoxScriptVariableReference, holder: AnnotationHolder) {
		//注明无法解析的情况
		val reference = element.reference
		if(reference.resolve() == null) {
			holder.newAnnotation(ERROR, message("paradox.script.annotator.unresolvedVariable", element.name))
				.create()
		}
	}
	
	private fun annotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		val name = element.value
		val project = element.project
		
		//注明所有对应名称的脚本属性，或者本地化属性（如果存在）
		val definition = findDefinition(name, null, project)
		if(definition != null) {
			holder.newSilentAnnotation(INFORMATION)
				.textAttributes(ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY)
				.create()
			return
		}
		val localisation = findLocalisation(name, null, project, hasDefault = true)
		if(localisation != null) {
			holder.newSilentAnnotation(INFORMATION)
				.textAttributes(ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY)
				.create()
		}
	}
}
