package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*

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
