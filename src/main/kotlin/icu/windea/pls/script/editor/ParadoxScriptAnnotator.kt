package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

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
		holder.newSilentAnnotation(INFORMATION)
			.range(element.propertyKey)
			.textAttributes(ParadoxScriptAttributesKeys.DEFINITION_KEY)
			.create()
		//TODO
	}
	
	private fun annotatePropertyKey(element: ParadoxScriptPropertyKey, holder: AnnotationHolder) {
		//颜色高亮
		val expression = element.propertyConfig?.keyExpression ?: return
		val attributesKey = when(expression.type) {
			CwtKeyExpression.Type.Localisation -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
			CwtKeyExpression.Type.SyncedLocalisation -> ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY
			CwtKeyExpression.Type.TypeExpression -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
			CwtKeyExpression.Type.TypeExpressionString -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
			CwtKeyExpression.Type.Enum -> ParadoxScriptAttributesKeys.ENUM_VALUE_REFERENCE_KEY
			else -> null //TODO
		} ?: return
		holder.newSilentAnnotation(INFORMATION)
			.textAttributes(attributesKey)
			.create()
	}
	
	private fun annotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		//颜色高亮
		//由于目前引用支持不完善，如果expression为null时需要进行回调解析引用
		val expression = element.valueConfig?.valueExpression ?: return fallbackAnnotateString(element, holder)
		//val expression = element.valueConfig?.valueExpression ?: return 
		val attributesKey = when(expression.type) {
			CwtValueExpression.Type.Localisation -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
			CwtValueExpression.Type.SyncedLocalisation -> ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY
			CwtValueExpression.Type.AbsoluteFilePath -> ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY
			CwtValueExpression.Type.FilePath -> ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY
			CwtValueExpression.Type.Icon -> ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY
			CwtValueExpression.Type.TypeExpression -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
			CwtValueExpression.Type.TypeExpressionString -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
			CwtValueExpression.Type.Enum -> ParadoxScriptAttributesKeys.ENUM_VALUE_REFERENCE_KEY
			CwtValueExpression.Type.AliasMatchLeft -> return fallbackAnnotateString(element,holder)
			else -> null //TODO
		} ?: return
		holder.newSilentAnnotation(INFORMATION)
			.textAttributes(attributesKey)
			.create()
	}
	
	private fun fallbackAnnotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		//NOTE 目前的版本不做任何处理
		//val name = element.value
		//val project = element.project
		//val attributesKey = when {
		//	existsDefinition(name, null, project) -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
		//	existsLocalisation(name, null, project) -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
		//	existsSyncedLocalisation(name, null, project) -> ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY
		//	else -> null
		//} ?: return
		//holder.newSilentAnnotation(INFORMATION)
		//	.textAttributes(attributesKey)
		//	.create()
	}
	
}
