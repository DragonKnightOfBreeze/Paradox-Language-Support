@file:Suppress("unused")

package icu.windea.pls.model

import com.intellij.psi.PsiElement
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// region Game Types

@OptIn(ExperimentalContracts::class)
fun ParadoxGameType?.orSpecific(): ParadoxGameType? {
    contract {
        returnsNotNull() implies (this@orSpecific != null)
    }

    if (this == null || this == ParadoxGameType.Core) return null
    return this
}

fun ParadoxGameType?.orDefault(): ParadoxGameType {
    return this ?: ParadoxGameType.getDefault()
}

// endregion

// region Modifiers

fun ParadoxModifierInfo.toPsiElement(parent: PsiElement): ParadoxModifierLightElement {
    return ParadoxModifierLightElement(parent, name, gameType, project)
        .also { ParadoxModifierSupport.Keys.copy(this, it) }
}

fun ParadoxModifierLightElement.toInfo(): ParadoxModifierInfo {
    return ParadoxModifierInfo(name, project, gameType)
        .also { ParadoxModifierSupport.Keys.copy(this, it) }
}

// endregion

// region Parameters

fun ParadoxParameterInfo.toPsiElement(parent: PsiElement): ParadoxParameterLightElement {
    return ParadoxParameterLightElement(parent, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        .also { ParadoxParameterSupport.Keys.copy(this, it) }
}

fun ParadoxParameterLightElement.toInfo(): ParadoxParameterInfo {
    return ParadoxParameterInfo(name, contextName, contextIcon, contextKey, readWriteAccess, project, gameType)
        .also { ParadoxParameterSupport.Keys.copy(this, it) }
}

// endregion
