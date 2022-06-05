package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
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
		holder.newSilentAnnotation(INFORMATION)
			.range(element.propertyKey)
			.textAttributes(Keys.DEFINITION_KEY)
			.create()
	}
	
	private fun annotatePropertyKey(element: ParadoxScriptPropertyKey, holder: AnnotationHolder) {
		//颜色高亮
		val expression = element.getPropertyConfig()?.keyExpression ?: return
		val attributesKey = when(expression.type) {
			CwtDataTypes.Localisation -> Keys.LOCALISATION_REFERENCE_KEY
			CwtDataTypes.SyncedLocalisation -> Keys.SYNCED_LOCALISATION_REFERENCE_KEY
			CwtDataTypes.TypeExpression -> Keys.DEFINITION_REFERENCE_KEY
			CwtDataTypes.TypeExpressionString -> Keys.DEFINITION_REFERENCE_KEY
			CwtDataTypes.Enum -> Keys.ENUM_VALUE_REFERENCE_KEY
			else -> null //TODO
		} ?: return
		holder.newSilentAnnotation(INFORMATION)
			.textAttributes(attributesKey)
			.create()
	}
	
	private fun annotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		//特殊处理字符串需要被识别为标签的情况
		annotateTag(element, holder)
		
		//颜色高亮
		val valueConfig = element.getValueConfig()
		val expression = valueConfig?.valueExpression ?: return
		val attributesKey = when(expression.type) {
			CwtDataTypes.Localisation -> Keys.LOCALISATION_REFERENCE_KEY
			CwtDataTypes.SyncedLocalisation -> Keys.SYNCED_LOCALISATION_REFERENCE_KEY
			CwtDataTypes.AbsoluteFilePath -> Keys.PATH_REFERENCE_KEY
			CwtDataTypes.FilePath -> Keys.PATH_REFERENCE_KEY
			CwtDataTypes.Icon -> Keys.PATH_REFERENCE_KEY
			CwtDataTypes.TypeExpression -> Keys.DEFINITION_REFERENCE_KEY
			CwtDataTypes.TypeExpressionString -> Keys.DEFINITION_REFERENCE_KEY
			CwtDataTypes.Enum -> Keys.ENUM_VALUE_REFERENCE_KEY
			else -> null //TODO
		} ?: return
		holder.newSilentAnnotation(INFORMATION).textAttributes(attributesKey).create()
	}
	
	private fun annotateTag(element: ParadoxScriptString, holder: AnnotationHolder): Boolean {
		//颜色高亮
		element.resolveTagConfig()?.let { _ ->
			holder.newSilentAnnotation(INFORMATION).textAttributes(Keys.TAG_KEY).create()
			return true
		}
		return false
	}
}
