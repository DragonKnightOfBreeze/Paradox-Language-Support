package icu.windea.pls.lang.util.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.matchesPath
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.selectFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptParameter
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * 用于按照类型或语义匹配输入的 PSI。
 */
@Suppress("unused")
object ParadoxPsiMatcher {
    // region Type Sensitive Matchers

    @OptIn(ExperimentalContracts::class)
    fun isScriptedVariable(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptScriptedVariable)
        }
        return element is ParadoxScriptScriptedVariable
    }

    @OptIn(ExperimentalContracts::class)
    fun isDefinition(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptDefinitionElement)
        }
        return element is ParadoxScriptDefinitionElement
    }

    @OptIn(ExperimentalContracts::class)
    fun isLocalisation(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationProperty)
        }
        return element is ParadoxLocalisationProperty
    }

    @OptIn(ExperimentalContracts::class)
    fun isComplexEnumValueElement(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxComplexEnumValueElement)
        }
        return element is ParadoxComplexEnumValueElement
    }

    @OptIn(ExperimentalContracts::class)
    fun isDynamicValueElement(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxDynamicValueElement)
        }
        return element is ParadoxDynamicValueElement
    }

    @OptIn(ExperimentalContracts::class)
    fun isParameterElement(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxParameterElement)
        }
        return element is ParadoxScriptParameter
    }

    // endregion

    // region Semantic Matchers

    fun isLocalScriptedVariable(element: ParadoxScriptScriptedVariable): Boolean {
        val path = selectFile(element)?.fileInfo?.path?.path ?: return false
        return !"common/scripted_variables".matchesPath(path)
    }

    fun isGlobalScriptedVariable(element: ParadoxScriptScriptedVariable): Boolean {
        val path = selectFile(element)?.fileInfo?.path?.path ?: return false
        return "common/scripted_variables".matchesPath(path)
    }

    // fun isInvocationReference(element: PsiElement, referenceElement: PsiElement): Boolean {
    //     if (element !is ParadoxScriptProperty) return false
    //     if (referenceElement !is ParadoxScriptPropertyKey) return false
    //     val name = element.definitionInfo?.name?.orNull() ?: return false
    //     if (name != referenceElement.text.unquote()) return false
    //     return true
    // }

    // endregion
}
