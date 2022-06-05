package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
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
		holder.newSilentAnnotation(INFORMATION).range(element.propertyKey).textAttributes(Keys.DEFINITION_KEY).create()
	}
	
	private fun annotatePropertyKey(element: ParadoxScriptPropertyKey, holder: AnnotationHolder) {
		val propertyConfig = element.getPropertyConfig()
		if(propertyConfig != null) annotateKeyExpression(element, holder, propertyConfig)
		if(propertyConfig == null && element.definitionElementInfo.isValid) annotateUnresolvedKeyExpression(element, holder)
	}
	
	private fun annotateKeyExpression(element: ParadoxScriptPropertyKey, holder: AnnotationHolder, propertyConfig: CwtPropertyConfig) {
		//颜色高亮
		val expression = propertyConfig.keyExpression 
		val attributesKey = when(expression.type) {
			CwtDataTypes.Localisation -> Keys.LOCALISATION_REFERENCE_KEY
			CwtDataTypes.SyncedLocalisation -> Keys.SYNCED_LOCALISATION_REFERENCE_KEY
			CwtDataTypes.TypeExpression -> Keys.DEFINITION_REFERENCE_KEY
			CwtDataTypes.TypeExpressionString -> Keys.DEFINITION_REFERENCE_KEY
			CwtDataTypes.Enum -> Keys.ENUM_VALUE_KEY
			else -> null
		}
		if(attributesKey != null) {
			holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKey).create()
		}
		val references = element.references
		for(reference in references) {
			val resolved = reference.resolve() ?: continue
			val configType = CwtConfigType.resolve(resolved) ?: continue
			val attributesKey1 =  when{
				configType == CwtConfigType.Link -> Keys.SCOPE_LINK_KEY
				configType == CwtConfigType.Modifier -> Keys.MODIFIER_KEY
				else -> null
			}
			if(attributesKey1 != null) {
				holder.newSilentAnnotation(INFORMATION).range(reference.rangeInElement.shiftRight(element.textRange.startOffset)) .textAttributes(attributesKey1).create()
			}
		}
	}
	
	private fun annotateUnresolvedKeyExpression(element: ParadoxScriptPropertyKey, holder: AnnotationHolder) {
		if(getInternalSettings().annotateUnresolvedKeyExpression){
			holder.newAnnotation(ERROR, PlsBundle.message("script.internal.unresolvedKeyExpression", element.text)).range(element).create()
		}
	}
	
	private fun annotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		//特殊处理字符串需要被识别为标签的情况
		if(annotateTag(element, holder)) return
		
		val valueConfig = element.getValueConfig()
		if(valueConfig != null) annotateValueExpression(element, holder, valueConfig)
		if(valueConfig == null && element.definitionElementInfo.isValid) annotateUnresolvedValueExpression(element, holder)
	}
	
	private fun annotateValueExpression(element: ParadoxScriptString, holder: AnnotationHolder, valueConfig: CwtValueConfig) {
		//颜色高亮
		val expression = valueConfig.valueExpression
		val attributesKey = when(expression.type) {
			CwtDataTypes.Localisation -> Keys.LOCALISATION_REFERENCE_KEY
			CwtDataTypes.SyncedLocalisation -> Keys.SYNCED_LOCALISATION_REFERENCE_KEY
			CwtDataTypes.AbsoluteFilePath -> Keys.PATH_REFERENCE_KEY
			CwtDataTypes.FilePath -> Keys.PATH_REFERENCE_KEY
			CwtDataTypes.Icon -> Keys.PATH_REFERENCE_KEY
			CwtDataTypes.TypeExpression -> Keys.DEFINITION_REFERENCE_KEY
			CwtDataTypes.TypeExpressionString -> Keys.DEFINITION_REFERENCE_KEY
			CwtDataTypes.Enum -> Keys.ENUM_VALUE_KEY
			else -> null //TODO
		}
		if(attributesKey != null) {
			holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKey).create()
		}
	}
	
	private fun annotateUnresolvedValueExpression(element: ParadoxScriptString, holder: AnnotationHolder) {
		if(getInternalSettings().annotateUnresolvedValueExpression){
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
