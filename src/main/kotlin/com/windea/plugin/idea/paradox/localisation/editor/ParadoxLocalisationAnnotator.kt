package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.highlighter.*
import com.windea.plugin.idea.paradox.localisation.psi.*

@Suppress("ControlFlowWithEmptyBody", "UNUSED_PARAMETER")
class ParadoxLocalisationAnnotator : Annotator, DumbAware {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is ParadoxLocalisationProperty -> annotateProperty(element, holder)
			is ParadoxLocalisationLocale -> annotateLocale(element, holder)
			is ParadoxLocalisationPropertyReference -> annotatePropertyReference(element, holder)
			is ParadoxLocalisationSequentialNumber -> annotateSequentialNumber(element, holder)
			is ParadoxLocalisationCommand -> annotateCommand(element, holder)
			is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
		}
	}
	
	private fun annotateProperty(element: ParadoxLocalisationProperty, holder: AnnotationHolder) {
		
	}
	
	private fun annotateLocale(element: ParadoxLocalisationLocale, holder: AnnotationHolder) {
		val paradoxLocale = element.paradoxLocale
		if(paradoxLocale == null) {
			holder.newAnnotation(ERROR, message("paradox.localisation.annotator.unsupportedLocale", element.name))
				.create()
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
		val paradoxColor = element.paradoxColor
		if(paradoxColor != null) {
			val colorId = paradoxColor.name
			val e = element.propertyReferenceParameter
			if(e != null) {
				val startOffset = e.startOffset
				annotateColor(colorId, holder, TextRange(startOffset, startOffset + 1))
			}
		}
	}
	
	private fun annotateSequentialNumber(element: ParadoxLocalisationSequentialNumber, holder: AnnotationHolder) {
		val paradoxSequentialNumber = element.paradoxSequentialNumber
		if(paradoxSequentialNumber == null) {
			holder.newAnnotation(ERROR, message("paradox.localisation.annotator.unsupportedSequentialNumber", element.name))
				.create()
		}
	}
	
	//TODO 不能严格验证
	private fun annotateCommand(element: ParadoxLocalisationCommand, holder: AnnotationHolder) {
		//验证commandScope是否存在且合法，不验证event_target
		//验证commandField是否合法（预定义，scopeVariable，scriptedLoc）
		//检查出错误时不再继续检查
		//val commandIdentifiers = element.commandIdentifierList
		//for((index, commandIdentifier) in commandIdentifiers.withIndex()) {
		//	if(commandIdentifier is ParadoxLocalisationCommandScope) {
		//		val name = commandIdentifier.name
		//		val paradoxCommandScope = commandIdentifier.paradoxCommandScope
		//		if(index == 0) {
		//			//primaryCommandScope, secondaryCommandScope, event_target
		//			if(paradoxCommandScope == null) {
		//				val message = message("paradox.localisation.annotator.unsupportedCommandScope", name)
		//				holder.newAnnotation(ERROR, message).range(commandIdentifier).create()
		//				break
		//			}
		//		} else {
		//			//secondaryCommandScope, event_target
		//			if(paradoxCommandScope == null) {
		//				val message = message("paradox.localisation.annotator.unsupportedCommandScope", name)
		//				holder.newAnnotation(ERROR, message).range(commandIdentifier).create()
		//				break
		//			}else if(!paradoxCommandScope.isSecondary) {
		//				val message = message("paradox.localisation.annotator.incorrectCommandScope.secondary", name)
		//				holder.newAnnotation(ERROR, message).range(commandIdentifier).create()
		//				break
		//			}
		//		}
		//	} else if(commandIdentifier is ParadoxLocalisationCommandField) {
		//		//commandField, scopeVariable, scriptedLoc
		//		val paradoxCommandField = commandIdentifier.paradoxCommandField
		//		if(paradoxCommandField == null){
		//			val message = message("paradox.localisation.annotator.unsupportedCommandField", name)
		//			holder.newAnnotation(ERROR, message).range(commandIdentifier).create()
		//			break
		//		}
		//	}
		//}
	}
	
	private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
		//如果是颜色文本，则为颜色代码文本加上对应的颜色
		val paradoxColor = element.paradoxColor
		if(paradoxColor == null) {
			holder.newAnnotation(ERROR, message("paradox.localisation.annotator.unsupportedColor", element.name))
				.create()
		} else {
			val e = element.colorId
			if(e != null) annotateColor(element.name, holder, e.textRange)
		}
	}
	
	private fun annotateColor(colorId: String, holder: AnnotationHolder, range: TextRange) {
		val attributesKey = ParadoxLocalisationAttributesKeys.COLOR_KEYS[colorId] ?: return
		holder.newSilentAnnotation(INFORMATION)
			.range(range).textAttributes(attributesKey)
			.create()
	}
}
