package icu.windea.pls.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.core.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.highlighter.ParadoxLocalisationAttributesKeys as Keys

@Suppress("UNUSED_PARAMETER")
class ParadoxLocalisationAnnotator : Annotator, DumbAware {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is ParadoxLocalisationProperty -> annotateProperty(element, holder)
			is ParadoxLocalisationPropertyReference -> annotatePropertyReference(element, holder)
			//is ParadoxLocalisationCommand -> annotateCommand(element, holder)
			is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
		}
	}
	
	private fun annotateProperty(element: ParadoxLocalisationProperty, holder: AnnotationHolder) {
		val localisationInfo = element.localisationInfo
		if(localisationInfo != null) annotateLocalisation(element, holder, localisationInfo)
	}
	
	private fun annotateLocalisation(element: ParadoxLocalisationProperty, holder: AnnotationHolder, localisationInfo: ParadoxLocalisationInfo) {
		//NOTE 并非特别必要，可能会影响性能
		////颜色高亮
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
				annotateColor(colorConfig, holder, TextRange(startOffset, startOffset + 1))
			}
		}
	}
	
	private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
		//颜色高亮
		val colorConfig = element.colorConfig
		if(colorConfig != null) {
			val location = element.colorId
			if(location != null) {
				annotateColor(colorConfig, holder, location.textRange)
			}
		}
	}
	
	private fun annotateColor(colorConfig: ParadoxTextColorConfig, holder: AnnotationHolder, range: TextRange) {
		//颜色高亮
		val attributesKey = Keys.getColorKey(colorConfig.color) ?: return
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
