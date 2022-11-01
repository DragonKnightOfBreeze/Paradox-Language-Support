package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
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
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.variable")
					else -> element.name
				}
			}
			is ParadoxScriptProperty -> {
				//如果是定义，需要特殊处理
				val definitionInfo = element.definitionInfo
				if(definitionInfo != null) {
					when(location) {
						UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.definition")
						else -> definitionInfo.name
					}
				} else {
					when(location) {
						UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.property")
						else -> element.name
					}
				}
			}
			is ParadoxExpressionAwareElement -> {
				val complexEnumValueInfo = element.complexEnumValueInfo
				if(complexEnumValueInfo != null) {
					when(location) {
						UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.complexEnumValue")
						UsageViewNodeTextLocation.INSTANCE -> complexEnumValueInfo.name + ": " + complexEnumValueInfo.enumName
						else -> complexEnumValueInfo.name
					}
				} else {
					when(location) {
						UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.expression")
						else -> element.text //keep quotes
					}
				}
			}
			is ParadoxParameterElement -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.parameter")
					else -> element.name
				}
			}
			is ParadoxValueSetValueElement -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.valueSetValue")
					UsageViewNodeTextLocation.INSTANCE -> element.name + ": " + element.valueSetName
					else -> element.name
				}
			}
			else -> null
		}
	}
	
	override fun getHelpId(psiElement: PsiElement): String {
		return HelpID.FIND_OTHER_USAGES
	}
	
	override fun canFindUsagesFor(element: PsiElement): Boolean {
		return when(element){
			is ParadoxScriptVariable -> true
			is ParadoxScriptProperty -> true
			is ParadoxExpressionAwareElement -> true
			is ParadoxParameterElement -> true
			is ParadoxValueSetValueElement -> true
			else -> return false
		}
	}
	
	override fun getWordsScanner(): WordsScanner {
		return ParadoxScriptWordScanner()
	}
}
