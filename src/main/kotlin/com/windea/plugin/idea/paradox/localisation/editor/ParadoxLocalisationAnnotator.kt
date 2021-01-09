package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.windea.plugin.idea.paradox.message
import com.windea.plugin.idea.paradox.localisation.highlighter.*
import com.windea.plugin.idea.paradox.localisation.intentions.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationAnnotator : Annotator, DumbAware {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			//is ParadoxLocalisationProperty -> annotateProperty(element, holder)
			is ParadoxLocalisationLocale -> annotateLocale(element, holder)
			is ParadoxLocalisationSerialNumber -> annotateSerialNumber(element, holder)
			is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
			is ParadoxLocalisationPropertyReference -> annotatePropertyReference(element, holder)
		}
	}
	
	//private fun annotateProperty(element: ParadoxLocalisationProperty, holder: AnnotationHolder) {
	//	
	//}
	
	private fun annotateLocale(element: ParadoxLocalisationLocale, holder: AnnotationHolder) {
		if(element.paradoxLocale == null) {
			holder.newAnnotation(ERROR, message("paradox.localisation.annotator.unsupportedLocale", element.name))
				.withFix(ChangeLocaleIntention)
				.create()
		}
	}
	
	private fun annotateSerialNumber(element: ParadoxLocalisationSerialNumber, holder: AnnotationHolder) {
		if(element.paradoxSerialNumber == null) {
			holder.newAnnotation(ERROR, message("paradox.localisation.annotator.unsupportedSerialNumber", element.name))
				.withFix(ChangeSerialNumberIntention)
				.create()
		}
	}
	
	private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
		//如果是颜色文本，则为颜色代码文本加上对应的颜色
		if(element.paradoxColor == null) {
			holder.newAnnotation(ERROR, message("paradox.localisation.annotator.unsupportedColor", element.name))
				.withFix(ChangeColorIntention)
				.create()
		} else {
			val e = element.colorCode
			if(e != null) annotateColor(element.name, holder, e.textRange)
		}
	}
	
	private fun annotatePropertyReference(element: ParadoxLocalisationPropertyReference, holder: AnnotationHolder) {
		//属性引用可能是变量，因此不注明无法解析的情况
		//val reference = element.reference?:return
		//if(reference.resolve() == null){
		//	holder.newAnnotation(ERROR,message("paradox.localisation.annotator.unresolvedProperty",element.name))
		//		.create()
		//	return
		//}
		//如果是属性引用，需要为属性引用参数加上对应的颜色
		val color = element.paradoxColor
		if(color != null) {
			val colorId = color.key
			val e = element.propertyReferenceParameter
			if(e != null) {
				val startOffset = e.startOffset
				annotateColor(colorId, holder, TextRange(startOffset, startOffset + 1))
			}
		}
	}
	
	private fun annotateColor(colorId: String, holder: AnnotationHolder, range: TextRange) {
		val attributesKey = ParadoxLocalisationAttributesKeys.COLOR_KEYS[colorId] ?: return
		holder.newSilentAnnotation(INFORMATION)
			.range(range).textAttributes(attributesKey)
			.create()
	}
}
