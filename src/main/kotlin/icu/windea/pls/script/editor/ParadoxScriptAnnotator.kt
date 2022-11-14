package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.highlighter.ParadoxScriptAttributesKeys as Keys

/**
 * 脚本文件的注解器。
 *
 * * 提供定义的特殊颜色高亮。（基于CWT规则）
 * * 提供定义元素的特殊颜色高亮。（基于CWT规则）
 * * 提供特殊标签的特殊颜色高亮。（基于扩展的CWT规则）
 */
class ParadoxScriptAnnotator : Annotator, DumbAware {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is ParadoxScriptProperty -> annotateProperty(element, holder)
			is ParadoxScriptExpressionElement -> annotateExpressionElement(element, holder)
		}
	}
	
	private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return annotateDefinition(element, holder, definitionInfo)
	}
	
	private fun annotateDefinition(element: ParadoxScriptProperty, holder: AnnotationHolder, definitionInfo: ParadoxDefinitionInfo) {
		//颜色高亮
		holder.newSilentAnnotation(INFORMATION).range(element.propertyKey).textAttributes(Keys.DEFINITION_KEY).create()
		val nameField = definitionInfo.typeConfig.nameField
		if(nameField != null) {
			//如果存在，高亮定义名对应的字符串（可能还有其他高亮）（这里不能使用PSI链接）
			val nameElement = element.findDefinitionProperty(nameField, true)?.findValue<ParadoxScriptString>()
			if(nameElement != null) {
				val nameString = definitionInfo.name.escapeXmlOrAnonymous()
				val typesString = definitionInfo.typesText
				val tooltip = PlsBundle.message("script.annotator.definitionName", nameString, typesString)
				holder.newSilentAnnotation(INFORMATION).range(nameElement)
					.tooltip(tooltip)
					.textAttributes(Keys.DEFINITION_NAME_KEY)
					.create()
			}
		}
	}
	
	private fun annotateComplexEnumValue(element: ParadoxScriptExpressionElement, holder: AnnotationHolder, complexEnumValueInfo: ParadoxComplexEnumValueInfo) {
		//高亮复杂枚举名对应的字符串（可能还有其他高亮）（这里不能使用PSI链接）
		val nameString = complexEnumValueInfo.name.escapeXmlOrAnonymous()
		val enumNameString = complexEnumValueInfo.enumName
		val tooltip = PlsBundle.message("script.annotator.complexEnumValueName", nameString, enumNameString)
		holder.newSilentAnnotation(INFORMATION).range(element)
			.tooltip(tooltip)
			.textAttributes(Keys.COMPLEX_ENUM_VALUE_NAME_KEY)
			.create()
	}
	
	private fun annotateExpressionElement(element: ParadoxScriptExpressionElement, holder: AnnotationHolder) {
		//不高亮带有参数的情况
		if(element.isParameterAwareExpression()) return
		
		val config = ParadoxCwtConfigHandler.resolveConfig(element)
		if(config != null) doAnnotateExpressionElement(element, element.textRange, config.expression, config, holder)
		
		val complexEnumValueInfo = element.complexEnumValueInfo
		if(complexEnumValueInfo != null) annotateComplexEnumValue(element, holder, complexEnumValueInfo)
	}
	
	private fun doAnnotateExpressionElement(
		element: ParadoxScriptExpressionElement,
		range: TextRange,
		expression: CwtDataExpression,
		config: CwtDataConfig<*>,
		holder: AnnotationHolder
	) {
		//高亮特殊标签
		if(config is CwtValueConfig && config.isTagConfig) {
			holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(Keys.TAG_KEY).create()
		}
		
		//颜色高亮
		val configGroup = config.info.configGroup
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
				val enumName = expression.value ?: return
				val attributesKey = when {
					enumName == CwtConfigHandler.paramsEnumName -> Keys.ARGUMENT_KEY
					configGroup.enums[enumName] != null -> Keys.ENUM_VALUE_KEY
					configGroup.complexEnums[enumName] != null -> Keys.COMPLEX_ENUM_VALUE_KEY
					else -> Keys.ENUM_VALUE_KEY
				}
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
				if(!element.isQuoted()){
					val expressionString = range.shiftLeft(element.textRange.startOffset).substring(element.text).unquote()
					val valueSetValueExpression = ParadoxScriptExpression.resolveValueSetValue(expressionString, configGroup)
					if(valueSetValueExpression.isEmpty()) return
					doAnnotateComplexExpression(element, valueSetValueExpression, config, range, holder)
				}
				val attributesKey = Keys.VALUE_SET_VALUE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				if(!element.isQuoted()) {
					val expressionString = range.shiftLeft(element.textRange.startOffset).substring(element.text).unquote()
					val scopeFieldExpression = ParadoxScriptExpression.resolveScopeField(expressionString, configGroup)
					if(scopeFieldExpression.isEmpty()) return
					doAnnotateComplexExpression(element, scopeFieldExpression, config, range, holder)
				}
			}
			CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
				if(!element.isQuoted()) {
					val expressionString = range.shiftLeft(element.textRange.startOffset).substring(element.text).unquote()
					val valueFieldExpression = ParadoxScriptExpression.resolveValueField(expressionString, configGroup)
					if(valueFieldExpression.isEmpty()) return
					doAnnotateComplexExpression(element, valueFieldExpression, config, range, holder)
				}
			}
			CwtDataTypes.Modifier -> {
				val attributesKey = Keys.MODIFIER_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			else -> pass()
		}
	}
	
	private fun doAnnotateComplexExpression(
		element: ParadoxScriptExpressionElement,
		valueSetValueExpression: ParadoxScriptComplexExpression,
		config: CwtDataConfig<*>,
		range: TextRange,
		holder: AnnotationHolder
	) {
		for(info in valueSetValueExpression.infos) {
			val attributesKeyExpressions = info.getAttributesKeyExpressions(element, config)
			if(attributesKeyExpressions.isNotEmpty()) {
				//使用第一个匹配的expression的高亮
				val infoRange = info.textRange.shiftRight(range.startOffset)
				doAnnotateExpressionElement(element, infoRange, attributesKeyExpressions.first(), config, holder)
				continue
			}
			val attributesKey = info.getAttributesKey()
			if(attributesKey != null) {
				val infoRange = info.textRange.shiftRight(range.startOffset)
				holder.newSilentAnnotation(INFORMATION).range(infoRange).textAttributes(attributesKey).create()
			}
		}
	}
}
