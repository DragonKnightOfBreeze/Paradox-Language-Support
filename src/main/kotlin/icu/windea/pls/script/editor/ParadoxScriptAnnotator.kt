package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptAnnotator : Annotator, DumbAware {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is ParadoxScriptProperty -> annotateProperty(element, holder)
			is ParadoxScriptVariableReference -> annotateVariableReference(element, holder)
			is ParadoxScriptPropertyKey -> annotatePropertyKey(element, holder)
			is ParadoxScriptString -> annotateString(element, holder)
		}
	}
	
	private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
		val definitionInfo = element.paradoxDefinitionInfo
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
	
	private fun annotateVariableReference(element: ParadoxScriptVariableReference, holder: AnnotationHolder) {
		//注明无法解析的情况
		val reference = element.reference
		if(reference.resolve() == null) {
			holder.newAnnotation(ERROR, message("paradox.script.annotator.unresolvedVariable", element.name))
				.create()
		}
	}
	
	private fun annotatePropertyKey(element: ParadoxScriptPropertyKey, holder: AnnotationHolder) {
		//颜色高亮
		val expression = element.expression ?: return
		val attributesKey = when(expression.type) {
			CwtKeyExpression.Type.TypeExpression -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
			CwtKeyExpression.Type.TypeExpressionString -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
			CwtKeyExpression.Type.Localisation -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
			CwtKeyExpression.Type.SyncedLocalisation -> ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY
			CwtKeyExpression.Type.EnumExpression -> ParadoxScriptAttributesKeys.ENUM_REFERENCE_KEY
			else -> null //TODO
		} ?: return
		holder.newSilentAnnotation(INFORMATION)
			.textAttributes(attributesKey)
			.create()
	}
	
	private fun annotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		//颜色高亮
		val expression = element.expression ?: return
		val attributesKey = when(expression.type) {
			CwtValueExpression.Type.TypeExpression -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
			CwtValueExpression.Type.TypeExpressionString -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
			CwtValueExpression.Type.Localisation -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
			CwtValueExpression.Type.SyncedLocalisation -> ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY
			CwtValueExpression.Type.EnumExpression -> ParadoxScriptAttributesKeys.ENUM_REFERENCE_KEY
			else -> null //TODO
		} ?: return
		holder.newSilentAnnotation(INFORMATION)
			.textAttributes(attributesKey)
			.create()
	}
}
