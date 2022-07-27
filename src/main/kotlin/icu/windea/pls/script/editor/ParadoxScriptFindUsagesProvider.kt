package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFindUsagesProvider : FindUsagesProvider, ElementDescriptionProvider {
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
			is ParadoxScriptVariable -> {
				if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("script.description.variable") else element.name
			}
			is IParadoxScriptParameter -> {
				if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("script.description.parameter") else element.name
			}
			is ParadoxScriptProperty -> {
				//如果是定义，需要特殊处理
				val definitionInfo = element.definitionInfo
				if(definitionInfo != null) {
					if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("script.description.definition") else definitionInfo.name
				} else {
					if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("script.description.property") else element.name
				}
			}
			is ParadoxScriptExpressionElement -> {
				when(element.getConfig()?.expression?.type) {
					CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
						if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("script.description.valueInValueSet") else element.value
					}
					else -> {
						if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("script.description.expression") else element.value
					}
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
