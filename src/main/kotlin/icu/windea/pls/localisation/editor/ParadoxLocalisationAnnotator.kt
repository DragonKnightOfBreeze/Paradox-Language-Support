package icu.windea.pls.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationAnnotator : Annotator, DumbAware {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			//is ParadoxLocalisationProperty -> annotateProperty(element, holder)
			is ParadoxLocalisationLocale -> annotateLocale(element, holder)
			is ParadoxLocalisationPropertyReference -> annotatePropertyReference(element, holder)
			is ParadoxLocalisationSequentialNumber -> annotateSequentialNumber(element, holder)
			//is ParadoxLocalisationCommand -> annotateCommand(element, holder)
			is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
		}
	}
	
	//private fun annotateProperty(element: ParadoxLocalisationProperty, holder: AnnotationHolder) {
	//	val localisationInfo = element.paradoxLocalisationInfo
	//	if(localisationInfo != null) annotateLocalisation(element,holder,localisationInfo)
	//}
	
	//private fun annotateLocalisation(element:ParadoxLocalisationProperty,holder:AnnotationHolder,localisationInfo:ParadoxLocalisationInfo){
	//	//NOTE 并非特别必要，可能会严重影响性能，暂时注释掉
	//	//颜色高亮
	//	val category = localisationInfo.category
	//	val attributesKey = when(category){
	//		ParadoxLocalisationCategory.Localisation -> ParadoxLocalisationAttributesKeys.LOCALISATION_KEY
	//		ParadoxLocalisationCategory.SyncedLocalisation -> ParadoxLocalisationAttributesKeys.SYNCED_LOCALISATION_KEY
	//	}
	//	holder.newSilentAnnotation(INFORMATION)
	//		.range(element.propertyKey)
	//		.textAttributes(attributesKey)
	//		.create()
	//}
	
	private fun annotateLocale(element: ParadoxLocalisationLocale, holder: AnnotationHolder) {
		//注明不支持的情况
		val locale = element.localeConfig
		if(locale == null) {
			holder.newAnnotation(ERROR, PlsBundle.message("localisation.annotator.unsupportedLocale", element.name))
				.range(element.localeId)
				.create()
		}
	}
	
	private fun annotatePropertyReference(element: ParadoxLocalisationPropertyReference, holder: AnnotationHolder) {
		//注明无法解析的情况
		//NOTE 属性引用可能是变量，因此不注明无法解析的情况
		//val reference = element.reference?:return
		//if(reference.resolve() == null){
		//	holder.newAnnotation(ERROR,message("localisation.annotator.unresolvedProperty",element.name))
		//		.create()
		//	return
		//}
		//颜色高亮
		val color = element.colorConfig
		if(color != null) {
			val colorId = color.name
			val e = element.propertyReferenceParameter
			if(e != null) {
				val startOffset = e.startOffset
				annotateColor(colorId, holder, TextRange(startOffset, startOffset + 1))
			}
		}
	}
	
	private fun annotateSequentialNumber(element: ParadoxLocalisationSequentialNumber, holder: AnnotationHolder) {
		//颜色高亮
		val sequentialNumber = element.sequentialNumberInfo
		if(sequentialNumber == null) {
			holder.newAnnotation(ERROR, PlsBundle.message("localisation.annotator.unsupportedSequentialNumber", element.name))
				.range(element.sequentialNumberId ?: element)
				.create()
		}
	}
	
	private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
		//注明不支持的情况 & 颜色高亮
		val colorConfig = element.colorConfig
		if(colorConfig == null) {
			holder.newAnnotation(ERROR, PlsBundle.message("localisation.annotator.unsupportedColor", element.name))
				.range(element.colorId ?: element)
				.create()
		} else {
			val e = element.colorId
			if(e != null) annotateColor(element.name, holder, e.textRange)
		}
	}
	
	private fun annotateColor(colorId: String, holder: AnnotationHolder, range: TextRange) {
		//颜色高亮
		val attributesKey = ParadoxLocalisationAttributesKeys.COLOR_KEYS[colorId] ?: return
		holder.newSilentAnnotation(INFORMATION)
			.range(range).textAttributes(attributesKey)
			.create()
	}
	
	//NOTE 不能严格验证
	//private fun annotateCommand(element: ParadoxLocalisationCommand, holder: AnnotationHolder) {
	//	//验证commandScope是否存在且合法，不验证event_target
	//	//验证commandField是否合法（预定义，scopeVariable，scriptedLoc）
	//	//检查出错误时不再继续检查
	//	val commandIdentifiers = element.commandIdentifierList
	//	for((index, commandIdentifier) in commandIdentifiers.withIndex()) {
	//		if(commandIdentifier is ParadoxLocalisationCommandScope) {
	//			val name = commandIdentifier.name
	//			val paradoxCommandScope = commandIdentifier.paradoxCommandScope
	//			if(index == 0) {
	//				//primaryCommandScope, secondaryCommandScope, event_target
	//				if(paradoxCommandScope == null) {
	//					val message = message("localisation.annotator.unsupportedCommandScope", name)
	//					holder.newAnnotation(ERROR, message).range(commandIdentifier).create()
	//					break
	//				}
	//			} else {
	//				//secondaryCommandScope, event_target
	//				if(paradoxCommandScope == null) {
	//					val message = message("localisation.annotator.unsupportedCommandScope", name)
	//					holder.newAnnotation(ERROR, message).range(commandIdentifier).create()
	//					break
	//				}else if(!paradoxCommandScope.isSecondary) {
	//					val message = message("localisation.annotator.incorrectCommandScope.secondary", name)
	//					holder.newAnnotation(ERROR, message).range(commandIdentifier).create()
	//					break
	//				}
	//			}
	//		} else if(commandIdentifier is ParadoxLocalisationCommandField) {
	//			//commandField, scopeVariable, scriptedLoc
	//			val paradoxCommandField = commandIdentifier.paradoxCommandField
	//			if(paradoxCommandField == null){
	//				val message = message("localisation.annotator.unsupportedCommandField", name)
	//				holder.newAnnotation(ERROR, message).range(commandIdentifier).create()
	//				break
	//			}
	//		}
	//	}
	//}
}
