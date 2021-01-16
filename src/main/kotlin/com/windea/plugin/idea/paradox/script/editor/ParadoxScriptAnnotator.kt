@file:Suppress("UNCHECKED_CAST")

package com.windea.plugin.idea.paradox.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.core.settings.*
import com.windea.plugin.idea.paradox.localisation.highlighter.*
import com.windea.plugin.idea.paradox.script.highlighter.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptAnnotator : Annotator, DumbAware {
	private val state = ParadoxSettingsState.getInstance()
	
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is ParadoxScriptProperty -> annotateProperty(element, holder)
			is ParadoxScriptVariableReference -> annotateVariableReference(element, holder)
			is ParadoxScriptString -> annotateString(element, holder)
		}
	}
	
	private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
		//如果是定义，则加上下划线突出显示
		if(element.paradoxDefinitionInfo != null) {
			holder.newSilentAnnotation(INFORMATION)
				.range(element.propertyKey)
				.textAttributes(ParadoxScriptAttributesKeys.DEFINITION_KEY)
				.create()
		}
	}
	
	private fun annotateVariableReference(element: ParadoxScriptVariableReference, holder: AnnotationHolder) {
		//注明无法解析的情况
		val reference = element.reference
		if(reference.resolve() == null) {
			holder.newAnnotation(WARNING, message("paradox.script.annotator.unresolvedVariable", element.name))
				.create()
		}
	}
	
	private fun annotateString(element: ParadoxScriptString, holder: AnnotationHolder) {
		val name = element.value
		val project = element.project
		
		//注明所有对应名称的脚本属性，或者本地化属性（如果存在）
		val scriptProperties = findDefinitions(name, null, project).toTypedArray()
		if(scriptProperties.isNotEmpty()) {
			holder.newSilentAnnotation(INFORMATION)
				.textAttributes(ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY)
				//.gutterIconRenderer(ParadoxStringDefinitionGutterIconRenderer(name, scriptProperties))
				.create()
			return
		}
		val localisationProperties = findLocalisations(name, null, project).toTypedArray()
		if(localisationProperties.isNotEmpty()) {
			holder.newSilentAnnotation(INFORMATION)
				.textAttributes(ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY)
				//.gutterIconRenderer(ParadoxStringLocalisationGutterIconRenderer(name, localisationProperties))
				.create()
		}
	}
}
