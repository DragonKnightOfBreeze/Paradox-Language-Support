package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

val ParadoxLocalisationLocale.localeId: PsiElement get() = findChild(LOCALE_ID)!!

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findChild(PROPERTY_KEY_TOKEN)!!

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findChild(PROPERTY_REFERENCE_ID)
val ParadoxLocalisationPropertyReference.propertyReferenceParameter: PsiElement? get() = findChild(PROPERTY_REFERENCE_PARAMETER_TOKEN)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findChild(ICON_ID)
val ParadoxLocalisationIcon.iconIdReference: ParadoxLocalisationPropertyReference?
	get() {
		forEachChild {
			if(it is ParadoxLocalisationPropertyReference) return it
			if(it.elementType == PIPE) return null
		}
		return null
	}
val ParadoxLocalisationIcon.iconFrame: PsiElement? get() = findChild(ICON_FRAME)
val ParadoxLocalisationIcon.iconFrameReference: ParadoxLocalisationPropertyReference?
	get() {
		var afterPipe = false
		forEachChild {
			if(afterPipe && it is ParadoxLocalisationPropertyReference) return it
			if(it.elementType == PIPE) afterPipe = true
		}
		return null
	}

val ParadoxLocalisationColorfulText.colorId: PsiElement? get() = findChild(COLOR_ID)

val ParadoxLocalisationCommandScope.commandScopeId: PsiElement get() = findChild(COMMAND_SCOPE_ID)!!

val ParadoxLocalisationCommandField.commandFieldId: PsiElement? get() = findChild(COMMAND_FIELD_ID)

val ParadoxLocalisationScriptedVariableReference.variableReferenceId: PsiElement get() = findChild(SCRIPTED_VARIABLE_REFERENCE_ID)!!

val ParadoxLocalisationStellarisFormatReference.stellarisFormatReferenceId: PsiElement? get() = findChild(STELLARIS_FORMAT_REFERENCE_ID)


fun hasLocalisationPropertiesBetween(start: PsiElement, end: PsiElement?): Boolean {
	val startElement = start.findParentInFile(true) { it.parent is ParadoxLocalisationPropertyList }
	val endElement = end?.findParentInFile(true) { it.parent is ParadoxLocalisationPropertyList }
	when {
		startElement == null && endElement == null -> return false
		startElement == null && endElement != null -> {
			val listElement = endElement.parent
			listElement.processChild {
				if(it is ParadoxLocalisationProperty) return true
				it != endElement
			}
		}
		startElement != null && endElement == null -> {
			val listElement = startElement.parent
			listElement.processChild(false) {
				if(it is ParadoxLocalisationProperty) return true
				it != startElement
			}
		}
		startElement != null && endElement != null -> {
			startElement.siblings().forEach {
				if(it is ParadoxLocalisationProperty) return true
				if(it == endElement) return false
			}
		}
	}
	return false
}

fun findLocalisationPropertiesBetween(start: PsiElement, end: PsiElement?): List<ParadoxLocalisationProperty> {
	val startElement = start.findParentInFile(true) { it.parent is ParadoxLocalisationPropertyList }
	val endElement = end?.findParentInFile(true) { it.parent is ParadoxLocalisationPropertyList }
	when {
		startElement == null && endElement == null -> return emptyList()
		startElement == null && endElement != null -> {
			val listElement = endElement.parent
			val result = mutableListOf<ParadoxLocalisationProperty>()
			listElement.processChild {
				if(it is ParadoxLocalisationProperty) result.add(it)
				it != endElement
			}
			return result
		}
		startElement != null && endElement == null -> {
			val listElement = startElement.parent
			val result = mutableListOf<ParadoxLocalisationProperty>()
			listElement.processChild(false) {
				if(it is ParadoxLocalisationProperty) result.add(it)
				it != startElement
			}
			return result
		}
		startElement != null && endElement != null -> {
			val result = mutableListOf<ParadoxLocalisationProperty>()
			startElement.siblings().forEach {
				if(it is ParadoxLocalisationProperty) result.add(it)
				if(it == endElement) return result
			}
			return result
		}
	}
	return emptyList()
}
