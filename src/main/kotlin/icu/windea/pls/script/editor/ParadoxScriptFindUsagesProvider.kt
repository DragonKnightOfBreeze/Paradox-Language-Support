package icu.windea.pls.script.editor

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.util.RefactoringDescriptionLocation
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewTypeLocation
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

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
        if (element is RefactoringDescriptionLocation) return null
        return when (element) {
            is ParadoxScriptScriptedVariable -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.scriptedVariable")
                    else -> element.name
                }
            }
            is ParadoxScriptProperty -> {
                //如果是定义，需要特殊处理
                val definitionInfo = element.definitionInfo
                if (definitionInfo != null) {
                    when (location) {
                        UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.definition")
                        else -> definitionInfo.name.or.anonymous()
                    }
                } else {
                    when (location) {
                        UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.property")
                        else -> element.name
                    }
                }
            }
            is ParadoxScriptStringExpressionElement -> {
                val complexEnumValueInfo = element.complexEnumValueInfo
                if (complexEnumValueInfo != null) {
                    when (location) {
                        UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.complexEnumValue")
                        else -> complexEnumValueInfo.name
                    }
                } else {
                    when (location) {
                        UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.expression")
                        else -> element.name
                    }
                }
            }
            is ParadoxParameterElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.parameter")
                    else -> element.name
                }
            }
            is ParadoxDynamicValueElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.dynamicValue")
                    else -> element.name
                }
            }
            is ParadoxModifierElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("cwt.config.description.modifier")
                    else -> element.name
                }
            }
            else -> null
        }
    }

    override fun getHelpId(psiElement: PsiElement): String {
        return "reference.dialogs.findUsages.other"
    }

    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return when (element) {
            is ParadoxScriptScriptedVariable -> true
            is ParadoxScriptProperty -> element.definitionInfo != null
            is ParadoxScriptStringExpressionElement -> element.complexEnumValueInfo != null
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
