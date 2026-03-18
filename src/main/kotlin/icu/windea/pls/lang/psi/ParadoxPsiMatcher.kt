package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.definitionCandidateInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * 用于按照类型或语义匹配输入的 PSI。
 */
@Suppress("unused")
object ParadoxPsiMatcher {
    // region Type Sensitive Matchers

    /**
     * 是否是非匿名的封装变量。
     */
    @OptIn(ExperimentalContracts::class)
    fun isScriptedVariable(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptScriptedVariable)
        }
        return element is ParadoxScriptScriptedVariable && element.name?.orNull() != null
    }

    /**
     * 是否是（可以获取类型的）本地化。
     */
    @OptIn(ExperimentalContracts::class)
    fun isLocalisation(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationProperty)
        }
        return element is ParadoxLocalisationProperty && element.type != null && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isComplexEnumValueElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxComplexEnumValueLightElement)
        }
        return element is ParadoxComplexEnumValueLightElement && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isDynamicValueElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxDynamicValueLightElement)
        }
        return element is ParadoxDynamicValueLightElement && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isParameterElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxParameterLightElement)
        }
        return element is ParadoxParameterLightElement && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isLocalisationParameterElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationParameterLightElement)
        }
        return element is ParadoxLocalisationParameterLightElement && element.name.orNull() != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isModifierElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxModifierLightElement)
        }
        return element is ParadoxModifierLightElement && element.name.orNull() != null
    }

    // endregion

    // region Semantic Matchers

    @OptIn(ExperimentalContracts::class)
    fun isLocalScriptedVariable(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptScriptedVariable)
        }
        if (!isScriptedVariable(element)) return false
        val path = selectFile(element)?.fileInfo?.path?.path ?: return false
        return !"common/scripted_variables".matchesPath(path)
    }

    @OptIn(ExperimentalContracts::class)
    fun isGlobalScriptedVariable(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptScriptedVariable)
        }
        if (!isScriptedVariable(element)) return false
        val path = selectFile(element)?.fileInfo?.path?.path ?: return false
        return "common/scripted_variables".matchesPath(path)
    }

    @OptIn(ExperimentalContracts::class)
    fun isDefinitionCandidate(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        return element is ParadoxDefinitionElement && element.definitionCandidateInfo != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isDefinition(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxDefinitionElement)
        }
        return element is ParadoxDefinitionElement && element.definitionInfo != null // 定义名可以为空（即匿名）
    }

    @OptIn(ExperimentalContracts::class)
    fun isDefinitionInjection(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        return element is ParadoxScriptProperty && element.definitionInjectionInfo != null
    }

    @OptIn(ExperimentalContracts::class)
    fun isNormalLocalisation(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationProperty)
        }
        return isLocalisation(element) && element.type == ParadoxLocalisationType.Normal
    }

    @OptIn(ExperimentalContracts::class)
    fun isSyncedLocalisation(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationProperty)
        }
        return isLocalisation(element) && element.type == ParadoxLocalisationType.Synced
    }

    @OptIn(ExperimentalContracts::class)
    fun isInvocationReference(element: PsiElement?, referenceElement: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (referenceElement !is ParadoxScriptPropertyKey) return false
        val name = element.definitionInfo?.name?.orNull() ?: return false
        if (name != referenceElement.text.unquote()) return false
        return true
    }

    @OptIn(ExperimentalContracts::class)
    fun isInlineScriptFile(element: PsiElement?, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptFile)
        }
        if (element !is ParadoxScriptFile) return false
        if (ParadoxInlineScriptManager.getInlineScriptExpression(element) == null) return false
        return true
    }

    @OptIn(ExperimentalContracts::class)
    fun isInlineScriptUsage(element: PsiElement?, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (gameType == null) return false
        if (!ParadoxInlineScriptManager.isMatched(element.name, gameType)) return false
        if (!ParadoxInlineScriptManager.isAvailable(element)) return false
        return true
    }

    @OptIn(ExperimentalContracts::class)
    fun isDefinitionInjectionUsage(element: PsiElement?, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (gameType == null) return false
        if (!ParadoxDefinitionInjectionManager.isMatched(element.name, gameType)) return false
        if (!ParadoxDefinitionInjectionManager.isAvailable(element)) return false
        return true
    }

    // endregion
}
