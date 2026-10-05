package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object ParadoxPsiPresentationService {
    @OptIn(ExperimentalContracts::class)
    fun getScriptedVariableName(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptScriptedVariable)
        }
        if (element !is ParadoxScriptScriptedVariable) return null
        // 3.0.4 do not check semantic here
        return element.name?.orNull()
    }

    @OptIn(ExperimentalContracts::class)
    fun getScriptedVariablePresentableName(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptScriptedVariable)
        }
        if (element !is ParadoxScriptScriptedVariable) return null
        // 3.0.4 do not check semantic here
        return ParadoxScriptedVariableManager.getPresentableName(element)?.orNull()
    }

    @OptIn(ExperimentalContracts::class)
    fun getDefinitionName(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxDefinitionElement)
        }
        if (element !is ParadoxDefinitionElement) return null
        return element.definitionInfo?.name?.orNull()
    }

    @OptIn(ExperimentalContracts::class)
    fun getDefinitionPresentableName(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxDefinitionElement)
        }
        if (element !is ParadoxDefinitionElement) return null
        return ParadoxDefinitionManager.getPresentableName(element)?.orNull()
    }

    @OptIn(ExperimentalContracts::class)
    fun getLocalisationName(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxDefinitionElement)
        }
        if (element !is ParadoxLocalisationProperty) return null
        // 3.0.4 do not check semantic here
        return element.name.orNull()
    }
}
