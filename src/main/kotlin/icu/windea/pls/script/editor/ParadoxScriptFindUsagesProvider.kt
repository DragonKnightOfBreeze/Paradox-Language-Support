package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFindUsagesProvider : FindUsagesProvider, ElementDescriptionProvider {
	companion object {
		val _variableDescription = PlsBundle.message("script.description.variable")
		val _propertyDescription = PlsBundle.message("script.description.property")
		val _definitionDescription = PlsBundle.message("script.description.definition")
	}
	
	override fun getType(element: PsiElement): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE)
	}
	
	override fun getDescriptiveName(element: PsiElement): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE)
	}
	
	override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewNodeTextLocation.INSTANCE)
	}
	
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		return when(element) {
			is ParadoxScriptVariable -> if(location == UsageViewTypeLocation.INSTANCE) _variableDescription else element.name
			is ParadoxScriptProperty -> {
				//如果是定义，需要特殊处理
				val definitionInfo = element.definitionInfo
				if(definitionInfo != null) {
					if(location == UsageViewTypeLocation.INSTANCE) _definitionDescription else definitionInfo.name
				} else {
					if(location == UsageViewTypeLocation.INSTANCE) _propertyDescription else element.name
				}
			}
			else -> null
		}
	}
	
	override fun getHelpId(psiElement: PsiElement): String {
		return HelpID.FIND_OTHER_USAGES
	}
	
	override fun canFindUsagesFor(element: PsiElement): Boolean {
		return element is ParadoxScriptNamedElement
	}
	
	override fun getWordsScanner(): WordsScanner {
		return ParadoxScriptWordScanner()
	}
}
