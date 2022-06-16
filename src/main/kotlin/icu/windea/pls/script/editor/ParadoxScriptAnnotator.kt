package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*
import icu.windea.pls.script.highlighter.ParadoxScriptAttributesKeys as Keys

/**
 * 脚本文件的注解器。
 *
 * * 提供定义的特殊颜色高亮。（基于CWT规则）
 * * 提供定义元素的特殊颜色高亮。（基于CWT规则）
 * * 提供标签的特殊颜色高亮。（基于扩展的CWT规则）
 */
@Suppress("UNUSED_PARAMETER")
class ParadoxScriptAnnotator : Annotator, DumbAware {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is ParadoxScriptProperty -> annotateProperty(element, holder)
			is ParadoxScriptPropertyKey -> annotatePropertyKey(element, holder)
			is ParadoxScriptString -> annotateString(element, holder)
		}
	}
	
	private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) annotateDefinition(element, holder, definitionInfo)
	}
	
	private fun annotateDefinition(element: ParadoxScriptProperty, holder: AnnotationHolder, definitionInfo: ParadoxDefinitionInfo) {
		//颜色高亮
		holder.newSilentAnnotation(INFORMATION).range(element.propertyKey).textAttributes(Keys.DEFINITION_KEY).create()
	}
	
	private fun annotatePropertyKey(element: ParadoxScriptPropertyKey, holder: AnnotationHolder) {
		val propertyConfig = element.getPropertyConfig()
		if(propertyConfig != null) annotateKeyExpression(element, holder, propertyConfig)
		
		//是定义元素，非定义自身，且是简单的keyExpression
		if(propertyConfig == null && element.isSimpleExpression() && element.definitionElementInfo.isValid) annotateUnresolvedKeyExpression(element, holder)
	}
	
	private fun annotateKeyExpression(element: ParadoxScriptPropertyKey, holder: AnnotationHolder, propertyConfig: CwtPropertyConfig) {
		//颜色高亮
		val expressionInfo = element.kvExpressionInfo ?: return
		when(expressionInfo.type) {
			ParadoxKvExpressionType.LiteralType -> {
				val attributesKey = when {
					CwtConfigHandler.isInputParameter(propertyConfig) -> Keys.INPUT_PARAMETER_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.InlineLocalisation && !element.isQuoted() -> Keys.LOCALISATION_REFERENCE_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.Localisation -> Keys.LOCALISATION_REFERENCE_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.SyncedLocalisation -> Keys.SYNCED_LOCALISATION_REFERENCE_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.TypeExpression -> Keys.DEFINITION_REFERENCE_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.TypeExpressionString -> Keys.DEFINITION_REFERENCE_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.Value -> Keys.VALUE_VALUE_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.ValueSet -> Keys.VALUE_VALUE_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.Enum -> Keys.ENUM_VALUE_KEY
					propertyConfig.keyExpression.type == CwtDataTypes.ComplexEnum -> Keys.ENUM_VALUE_KEY
					else -> {
						val resolved = element.reference?.resolve()
						val configType = resolved?.let { CwtConfigType.resolve(it) }
						when {
							configType == CwtConfigType.SystemScope -> Keys.SYSTEM_SCOPE_KEY
							configType == CwtConfigType.Scope -> Keys.SCOPE_KEY
							configType == CwtConfigType.Modifier -> Keys.MODIFIER_KEY
							else -> null
						}
					}
				}
				if(attributesKey != null) {
					holder.newSilentAnnotation(INFORMATION).range(expressionInfo.wholeRange).textAttributes(attributesKey).create()
				}
			}
			ParadoxKvExpressionType.ScopeExpression -> {
				val references = element.references
				for(reference in references) {
					val resolved = reference.resolve()
					val configType = resolved?.let { CwtConfigType.resolve(it) }
					val attributesKey = when {
						configType == CwtConfigType.SystemScope -> Keys.SYSTEM_SCOPE_KEY
						configType == CwtConfigType.Scope -> Keys.SCOPE_KEY
						else -> Keys.SCOPE_KEY //unresolved scope, use SCOPE_KEY
					}
					holder.newSilentAnnotation(INFORMATION).range(expressionInfo.wholeRange).textAttributes(attributesKey).create()
				}
			}
			ParadoxKvExpressionType.ScopeValueExpression -> {
				expressionInfo.ranges.forEachIndexed { index, textRange -> 
					when{
						index == 0 -> holder.newSilentAnnotation(INFORMATION).range(textRange).textAttributes(Keys.SCOPE_VALUE_PREFIX_KEY).create()
						index == 1 -> holder.newSilentAnnotation(INFORMATION).range(textRange).textAttributes(Keys.SCOPE_VALUE_KEY).create()
					}
				}
			}
			ParadoxKvExpressionType.ScriptValueExpression -> pass() //unexpected
			else -> pass()
		}
	}
	
	private fun annotateUnresolvedKeyExpression(element: ParadoxScriptPropertyKey, holder: AnnotationHolder) {
		if(getInternalSettings().annotateUnresolvedKeyExpression) {
			holder.newAnnotation(ERROR, PlsBundle.message("script.internal.unresolvedKeyExpression", element.text)).range(element).create()
		}
	}
	
	private fun annotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		//特殊处理字符串需要被识别为标签的情况
		if(annotateTag(element, holder)) return
		
		val valueConfig = element.getValueConfig()
		if(valueConfig != null) annotateValueExpression(element, holder, valueConfig)
		
		//是定义元素，非定义自身，且是简单的valueExpression
		if(valueConfig == null && element.isSimpleExpression() && element.definitionElementInfo.isValid) annotateUnresolvedValueExpression(element, holder)
	}
	
	private fun annotateValueExpression(element: ParadoxScriptString, holder: AnnotationHolder, valueConfig: CwtValueConfig) {
		//颜色高亮
		val expressionInfo = element.kvExpressionInfo ?: return
		when(expressionInfo.type) {
			ParadoxKvExpressionType.LiteralType -> {
				val attributesKey = when {
					valueConfig.valueExpression.type == CwtDataTypes.InlineLocalisation && !element.isQuoted() -> Keys.LOCALISATION_REFERENCE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.Localisation -> Keys.LOCALISATION_REFERENCE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.SyncedLocalisation -> Keys.SYNCED_LOCALISATION_REFERENCE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.TypeExpression -> Keys.DEFINITION_REFERENCE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.TypeExpressionString -> Keys.DEFINITION_REFERENCE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.AbsoluteFilePath -> Keys.PATH_REFERENCE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.FilePath -> Keys.PATH_REFERENCE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.Icon -> Keys.PATH_REFERENCE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.Value -> Keys.VALUE_VALUE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.ValueSet -> Keys.VALUE_VALUE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.Enum -> Keys.ENUM_VALUE_KEY
					valueConfig.valueExpression.type == CwtDataTypes.ComplexEnum -> Keys.ENUM_VALUE_KEY
					else -> {
						val resolved = element.reference?.resolve()
						val configType = resolved?.let { CwtConfigType.resolve(it) }
						when {
							configType == CwtConfigType.SystemScope -> Keys.SYSTEM_SCOPE_KEY
							configType == CwtConfigType.Scope -> Keys.SCOPE_KEY
							configType == CwtConfigType.Modifier -> Keys.MODIFIER_KEY
							else -> null
						}
					}
				}
				if(attributesKey != null) {
					holder.newSilentAnnotation(INFORMATION).range(expressionInfo.wholeRange).textAttributes(attributesKey).create()
				}
			}
			ParadoxKvExpressionType.ScopeExpression -> {
				val references = element.references
				for(reference in references) {
					val resolved = reference.resolve()
					val configType = resolved?.let { CwtConfigType.resolve(it) }
					val attributesKey = when {
						configType == CwtConfigType.SystemScope -> Keys.SYSTEM_SCOPE_KEY
						configType == CwtConfigType.Scope -> Keys.SCOPE_KEY
						else -> Keys.SCOPE_KEY //unresolved scope, use SCOPE_KEY
					}
					holder.newSilentAnnotation(INFORMATION).range(expressionInfo.wholeRange).textAttributes(attributesKey).create()
				}
			}
			ParadoxKvExpressionType.ScopeValueExpression -> {
				expressionInfo.ranges.forEachIndexed { index, textRange ->
					when {
						index == 0 -> holder.newSilentAnnotation(INFORMATION).range(textRange).textAttributes(Keys.SCOPE_VALUE_PREFIX_KEY).create()
						index == 1 -> holder.newSilentAnnotation(INFORMATION).range(textRange).textAttributes(Keys.SCOPE_VALUE_KEY).create()
					}
				}
			}
			ParadoxKvExpressionType.ScriptValueExpression -> {
				expressionInfo.ranges.forEachIndexed { index, textRange ->
					when {
						index == 0 -> holder.newSilentAnnotation(INFORMATION).range(textRange).textAttributes(Keys.SCRIPT_VALUE_PREFIX_KEY).create()
						index == 1 -> holder.newSilentAnnotation(INFORMATION).range(textRange).textAttributes(Keys.SCRIPT_VALUE_KEY).create()
						index % 2 == 0 -> holder.newSilentAnnotation(INFORMATION).range(textRange).textAttributes(Keys.INPUT_PARAMETER_KEY).create()
						else -> {
							val isNumber = ParadoxValueType.isFloat(textRange.substring(element.text))
							val textAttributesKey = if(isNumber) Keys.NUMBER_KEY else Keys.STRING_KEY
							holder.newSilentAnnotation(INFORMATION).range(textRange).textAttributes(textAttributesKey).create()
						}
					}
				}
			}
			else -> pass()
		}
	}
	
	private fun annotateUnresolvedValueExpression(element: ParadoxScriptString, holder: AnnotationHolder) {
		if(getInternalSettings().annotateUnresolvedValueExpression) {
			holder.newAnnotation(ERROR, PlsBundle.message("script.internal.unresolvedValueExpression", element.text)).range(element).create()
		}
	}
	
	private fun annotateTag(element: ParadoxScriptString, holder: AnnotationHolder): Boolean {
		//颜色高亮
		if(element.resolveTagConfig() == null) return false
		holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(Keys.TAG_KEY).create()
		return true
	}
}
