package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.refactoring.util.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFindUsagesProvider : FindUsagesProvider, ElementDescriptionProvider {
	override fun getType(element: PsiElement): String {
		return getElementDescription(element, UsageViewTypeLocation.INSTANCE).orEmpty()
	}
	
	override fun getDescriptiveName(element: PsiElement): String {
		return getElementDescription(element, UsageViewLongNameLocation.INSTANCE).orEmpty()
	}
	
	override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
		return getElementDescription(element, UsageViewNodeTextLocation.INSTANCE).orEmpty()
	}
	
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		if(element is RefactoringDescriptionLocation) return null
		return when(element) {
			is ParadoxScriptScriptedVariable -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.scriptedVariable")
					else -> element.name
				}
			}
			is ParadoxScriptProperty -> {
				//如果是定义，需要特殊处理
				val definitionInfo = element.definitionInfo
				if(definitionInfo != null) {
					when(location) {
						UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.definition")
						else -> definitionInfo.name.orAnonymous()
					}
				} else {
					when(location) {
						UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.property")
						else -> element.name
					}
				}
			}
			is ParadoxScriptStringExpressionElement -> {
				val complexEnumValueInfo = element.complexEnumValueInfo
				if(complexEnumValueInfo != null) {
					when(location) {
						UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.complexEnumValue")
						else -> complexEnumValueInfo.name
					}
				} else {
					when(location) {
						UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.expression")
						else -> element.name
					}
				}
			}
			is ParadoxTemplateExpressionElement -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.templateExpression")
					else -> element.name
				}
			}
			is ParadoxParameterElement -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.parameter")
					else -> element.name
				}
			}
			is ParadoxDynamicValueElement -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.dynamicValue")
					else -> element.name
				}
			}
			is ParadoxModifierElement -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("cwt.description.modifier")
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
		return when(element) {
			is ParadoxScriptScriptedVariable -> true
			is ParadoxScriptProperty -> element.definitionInfo != null
			is ParadoxScriptStringExpressionElement -> element.complexEnumValueInfo != null
			is ParadoxTemplateExpressionElement -> true
			is ParadoxParameterElement -> true
			is ParadoxDynamicValueElement -> true
			is ParadoxModifierElement -> true
			else -> false
		}
	}
	
	override fun getWordsScanner(): WordsScanner {
		return ParadoxScriptWordScanner()
	}
}
