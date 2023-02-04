package icu.windea.pls.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.highlighter.ParadoxLocalisationAttributesKeys as Keys

@Suppress("UNUSED_PARAMETER")
class ParadoxLocalisationAnnotator : Annotator {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is ParadoxLocalisationProperty -> annotateProperty(element, holder)
			is ParadoxLocalisationPropertyReference -> annotatePropertyReference(element, holder)
			is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
			is ParadoxLocalisationCommandScope -> annotateCommandScope(element, holder)
			is ParadoxLocalisationCommandField -> annotateCommandField(element, holder)
		}
	}
	
	private fun annotateProperty(element: ParadoxLocalisationProperty, holder: AnnotationHolder) {
		val localisationInfo = element.localisationInfo
		if(localisationInfo != null) annotateLocalisation(element, holder, localisationInfo)
	}
	
	private fun annotateLocalisation(element: ParadoxLocalisationProperty, holder: AnnotationHolder, localisationInfo: ParadoxLocalisationInfo) {
		//颜色高亮（并非特别必要，注释掉）
		//val category = localisationInfo.category
		//val attributesKey = when(category) {
		//	ParadoxLocalisationCategory.Localisation -> ParadoxLocalisationAttributesKeys.LOCALISATION_KEY
		//	ParadoxLocalisationCategory.SyncedLocalisation -> ParadoxLocalisationAttributesKeys.SYNCED_LOCALISATION_KEY
		//}
		//holder.newSilentAnnotation(INFORMATION)
		//	.range(element.propertyKey)
		//	.textAttributes(attributesKey)
		//	.create()
	}
	
	private fun annotatePropertyReference(element: ParadoxLocalisationPropertyReference, holder: AnnotationHolder) {
		//颜色高亮
		val colorConfig = element.colorConfig
		if(colorConfig != null) {
			val location = element.propertyReferenceParameter
			if(location != null) {
				val startOffset = location.startOffset
				val attributesKey = Keys.getColorKey(colorConfig.color) ?: return
				holder.newSilentAnnotation(INFORMATION).range(TextRange(startOffset, startOffset + 1)).textAttributes(attributesKey).create()
			}
		}
	}
	
	private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
		//颜色高亮
		val location = element.colorId ?: return
		val attributesKey = element.reference?.getTextAttributesKey() ?: return
		holder.newSilentAnnotation(INFORMATION).range(location).textAttributes(attributesKey).create()
	}
	
	private fun annotateCommandScope(element: ParadoxLocalisationCommandScope, holder: AnnotationHolder) {
		//颜色高亮
		val attributesKey = element.reference.getTextAttributesKey() ?: return
		holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKey).create()
	}
	
	private fun annotateCommandField(element: ParadoxLocalisationCommandField, holder: AnnotationHolder) {
		//颜色高亮
		val attributesKey = element.reference?.getTextAttributesKey() ?: return
		holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKey).create()
	}
}
