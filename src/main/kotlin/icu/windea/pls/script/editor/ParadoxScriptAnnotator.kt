package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*
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
		val propertyConfig = ParadoxCwtConfigHandler.resolvePropertyConfig(element)
		if(propertyConfig != null) annotateExpression(element, element.textRange, propertyConfig.expression, propertyConfig.info.configGroup, holder)
	}
	
	private fun annotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		//特殊处理字符串需要被识别为标签的情况
		if(annotateTag(element, holder)) return
		
		val valueConfig = ParadoxCwtConfigHandler.resolveValueConfig(element)
		if(valueConfig != null) annotateExpression(element, element.textRange, valueConfig.expression, valueConfig.info.configGroup, holder)
	}
	
	private fun annotateExpression(element: ParadoxScriptExpressionElement, range: TextRange, expression: CwtKvExpression, configGroup: CwtConfigGroup, holder: AnnotationHolder) {
		//颜色高亮
		when(expression.type) {
			CwtDataTypes.InlineLocalisation -> {
				if(element.isQuoted()) {
					val attributesKey = Keys.LOCALISATION_REFERENCE_KEY
					holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
				}
			}
			CwtDataTypes.Localisation -> {
				val attributesKey = Keys.LOCALISATION_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.SyncedLocalisation -> {
				val attributesKey = Keys.SYNCED_LOCALISATION_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.TypeExpression -> {
				val attributesKey = Keys.DEFINITION_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.TypeExpressionString -> {
				val attributesKey = Keys.DEFINITION_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val attributesKey = Keys.PATH_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.FilePath -> {
				val attributesKey = Keys.PATH_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.Icon -> {
				val attributesKey = Keys.PATH_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.Enum -> {
				//TODO 支持complex_enum
				val attributesKey = when {
					expression.value == CwtConfigHandler.paramsEnumName -> Keys.INPUT_PARAMETER_KEY
					else -> Keys.ENUM_VALUE_KEY
				}
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.Value -> {
				val attributesKey = Keys.VALUE_SET_VALUE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.ValueSet -> {
				val attributesKey = Keys.VALUE_SET_VALUE_KEY
				holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKey).create()
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				if(!element.isQuoted()) {
					val scopeFieldExpression = ParadoxScriptScopeFieldExpression.resolve(element.value, configGroup)
					if(scopeFieldExpression.isEmpty()) return
					for(info in scopeFieldExpression.infos) {
						val attributesKeyExpressions = info.getAttributesKeyExpressions(element)
						if(attributesKeyExpressions.isNotEmpty()) {
							//使用第一个匹配的expression的高亮
							val infoRange = info.textRange.shiftRight(range.startOffset)
							annotateExpression(element, infoRange, attributesKeyExpressions.first(), configGroup, holder)
							continue
						} 
						val attributesKey = info.getAttributesKey()
						if(attributesKey != null){
							val infoRange = info.textRange.shiftRight(range.startOffset)
							holder.newSilentAnnotation(INFORMATION).range(infoRange).textAttributes(attributesKey).create()
						}
					}
				}
			}
			CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
				if(!element.isQuoted()){
					val valueFieldExpression = ParadoxScriptValueFieldExpression.resolve(element.value, configGroup)
					if(valueFieldExpression.isEmpty()) return
					for(info in valueFieldExpression.infos) {
						val attributesKeyExpressions = info.getAttributesKeyExpressions(element)
						if(attributesKeyExpressions.isNotEmpty()) {
							//使用第一个匹配的expression的高亮
							val infoRange = info.textRange.shiftRight(range.startOffset)
							annotateExpression(element, infoRange, attributesKeyExpressions.first(), configGroup, holder)
							continue
						}
						val attributesKey = info.getAttributesKey()
						if(attributesKey != null){
							val infoRange = info.textRange.shiftRight(range.startOffset)
							holder.newSilentAnnotation(INFORMATION).range(infoRange).textAttributes(attributesKey).create()
						}
					}
				}
			}
			else -> {
				//特殊处理是modifier的情况
				val resolved = element.references.singleOrNull()?.resolve()
				val configType = resolved?.let { CwtConfigType.resolve(it) }
				val attributesKey = when {
					configType == CwtConfigType.Modifier -> Keys.MODIFIER_KEY
					else -> null
				}
				if(attributesKey != null) {
					holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
				}
			}
		}
	}
	
	private fun annotateTag(element: ParadoxScriptString, holder: AnnotationHolder): Boolean {
		//颜色高亮
		if(element.resolveTagConfig() == null) return false
		holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(Keys.TAG_KEY).create()
		return true
	}
}
