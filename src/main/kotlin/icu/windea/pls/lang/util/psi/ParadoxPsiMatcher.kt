package icu.windea.pls.lang.util.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.selectFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
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
        return element is ParadoxScriptScriptedVariable && element.name?.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isDefinition(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptDefinitionElement)
        }
        return element is ParadoxScriptDefinitionElement // 定义名可以为空（即匿名）
    }

    @OptIn(ExperimentalContracts::class)
    fun isLocalisation(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationProperty)
        }
        return element is ParadoxLocalisationProperty && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isComplexEnumValueElement(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxComplexEnumValueElement)
        }
        return element is ParadoxComplexEnumValueElement && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isDynamicValueElement(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxDynamicValueElement)
        }
        return element is ParadoxDynamicValueElement && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isParameterElement(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxParameterElement)
        }
        return element is ParadoxParameterElement && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isLocalisationParameterElement(element: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationParameterElement)
        }
        return element is ParadoxLocalisationParameterElement && element.name.orNull() != null
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
