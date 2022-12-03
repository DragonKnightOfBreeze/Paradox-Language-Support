package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveConfigs
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.highlighter.ParadoxScriptAttributesKeys as Keys

/**
 * 脚本文件的注解器。
 *
 * * 提供定义的特殊颜色高亮。（基于CWT规则）
 * * 提供定义元素的特殊颜色高亮。（基于CWT规则）
 * * 提供特殊标签的特殊颜色高亮。（基于扩展的CWT规则）
 */
class ParadoxScriptAnnotator : Annotator {
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
		val config = resolveConfigs(element).firstOrNull()
		if(config != null) {
			doAnnotateExpressionElement(element, element.textRange, null, config, holder)
		}
		
		val complexEnumValueInfo = element.complexEnumValueInfo
		if(complexEnumValueInfo != null) {
			annotateComplexEnumValue(element, holder, complexEnumValueInfo)
		}
	}
	
	private fun doAnnotateExpressionElement(
		element: ParadoxScriptExpressionElement,
		range: TextRange,
		rangeInElement: TextRange?,
		config: CwtConfig<*>,
		holder: AnnotationHolder
	) {
		val configExpression = config.expression ?: return
		
		//高亮特殊标签
		if(config is CwtValueConfig && config.isTagConfig) {
			holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(Keys.TAG_KEY).create()
		}
		
		//颜色高亮
		val configGroup = config.info.configGroup
		val text = rangeInElement?.substring(element.text) ?: element.text
		val isKey = element is ParadoxScriptPropertyKey
		when(configExpression.type) {
			CwtDataTypes.InlineLocalisation -> {
				if(!element.isQuoted()) {
					if(text.isParameterAwareExpression()) return
					val attributesKey = Keys.LOCALISATION_REFERENCE_KEY
					holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
				}
			}
			CwtDataTypes.Localisation -> {
				if(text.isParameterAwareExpression()) return
				val attributesKey = Keys.LOCALISATION_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.SyncedLocalisation -> {
				if(text.isParameterAwareExpression()) return
				val attributesKey = Keys.SYNCED_LOCALISATION_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.TypeExpression -> {
				if(text.isParameterAwareExpression()) return
				val attributesKey = Keys.DEFINITION_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.TypeExpressionString -> {
				if(text.isParameterAwareExpression()) return
				val attributesKey = Keys.DEFINITION_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.AbsoluteFilePath -> {
				if(text.isParameterAwareExpression()) return
				val attributesKey = Keys.PATH_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.FilePath -> {
				if(text.isParameterAwareExpression()) return
				val attributesKey = Keys.PATH_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.Icon -> {
				if(text.isParameterAwareExpression()) return
				val attributesKey = Keys.PATH_REFERENCE_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.Enum -> {
				if(text.isParameterAwareExpression()) return
				val enumName = configExpression.value ?: return
				val attributesKey = when {
					enumName == CwtConfigHandler.paramsEnumName -> Keys.ARGUMENT_KEY
					configGroup.enums[enumName] != null -> Keys.ENUM_VALUE_KEY
					configGroup.complexEnums[enumName] != null -> Keys.COMPLEX_ENUM_VALUE_KEY
					else -> Keys.ENUM_VALUE_KEY
				}
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
				if(text.isQuoted()) return
				if(config !is CwtDataConfig<*>) {
					val valueSetName = config.expression?.value ?: return
					val textAttributesKey = when(valueSetName) {
						"variable" -> Keys.VARIABLE_KEY
						else -> Keys.VALUE_SET_VALUE_KEY
					}
					holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(textAttributesKey).create()
					return
				}
				val textRange = TextRange.create(0, text.length)
				val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(text, textRange, config, configGroup, isKey) ?: return
				annotateComplexExpression(element, valueSetValueExpression, config, range, holder)
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				if(text.isQuoted()) return
				val textRange = TextRange.create(0, text.length)
				val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey) ?: return
				annotateComplexExpression(element, scopeFieldExpression, config, range, holder)
			}
			CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
				if(text.isQuoted()) return
				val textRange = TextRange.create(0, text.length)
				val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey) ?: return
				annotateComplexExpression(element, valueFieldExpression, config, range, holder)
			}
			CwtDataTypes.Modifier -> {
				if(text.isParameterAwareExpression()) return
				val attributesKey = Keys.MODIFIER_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.AliasName, CwtDataTypes.AliasKeysField -> {
				if(text.isParameterAwareExpression()) return
				val aliasName = configExpression.value ?: return
				val aliasMap = configGroup.aliasGroups.get(aliasName) ?: return
				val aliasSubName = CwtConfigHandler.getAliasSubName(text, false, aliasName, configGroup) ?: return
				val aliasConfig = aliasMap[aliasSubName]?.first() ?: return
				doAnnotateExpressionElement(element, range, rangeInElement, aliasConfig, holder)
			}
			CwtDataTypes.ConstantKey -> {
				if(text.isParameterAwareExpression()) return
				if(rangeInElement == null && element is ParadoxScriptPropertyKey) return //unnecessary
				val attributesKey = Keys.PROPERTY_KEY_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			CwtDataTypes.Constant -> {
				if(text.isParameterAwareExpression()) return
				if(rangeInElement == null && element is ParadoxScriptString) return //unnecessary
				val attributesKey = Keys.STRING_KEY
				holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
			}
			else -> return
		}
	}
	
	fun annotateComplexExpression(element: ParadoxScriptExpressionElement, expression: ParadoxComplexExpression, config: CwtConfig<*>, range: TextRange, holder: AnnotationHolder) {
		doAnnotateComplexExpression(element, expression, config, range, holder)
	}
	
	private fun doAnnotateComplexExpression(element: ParadoxScriptExpressionElement, expressionNode: ParadoxExpressionNode, config: CwtConfig<*>, range: TextRange, holder: AnnotationHolder) {
		val rangeToAnnotate = expressionNode.rangeInExpression.shiftRight(range.startOffset)
		val attributesKey = expressionNode.getAttributesKey()
		
		if(attributesKey != null) {
			if(expressionNode is ParadoxTokenExpressionNode) {
				//override default highlight by highlighter (property key or string)
				holder.newSilentAnnotation(INFORMATION).textAttributes(HighlighterColors.TEXT).create()
			}
			holder.newSilentAnnotation(INFORMATION).range(rangeToAnnotate).textAttributes(attributesKey).create()
		}
		val attributesKeyConfig = expressionNode.getAttributesKeyConfig(element)
		if(attributesKeyConfig != null) {
			doAnnotateExpressionElement(element, rangeToAnnotate, expressionNode.rangeInExpression, attributesKeyConfig, holder)
		}
		if(expressionNode.nodes.isNotEmpty()) {
			for(node in expressionNode.nodes) {
				doAnnotateComplexExpression(element, node, config, range, holder)
			}
		}
	}
}
