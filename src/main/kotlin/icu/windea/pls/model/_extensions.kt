@file:Suppress("unused")

package icu.windea.pls.model

import com.intellij.psi.PsiElement
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement

// region Game Types

fun ParadoxGameType?.orSpecific(): ParadoxGameType? = takeIf { it != ParadoxGameType.Core }

fun ParadoxGameType?.orDefault(): ParadoxGameType = this ?: ParadoxGameType.getDefault()

// endregion

// region Modifiers

fun ParadoxModifierInfo.toPsiElement(parent: PsiElement): ParadoxModifierLightElement {
    return ParadoxModifierLightElement(parent, name, gameType, project)
        .also { ParadoxModifierSupport.Keys.copy(this, it) }
}

fun ParadoxModifierLightElement.toInfo(): ParadoxModifierInfo {
    return ParadoxModifierInfo(name, gameType, project)
        .also { ParadoxModifierSupport.Keys.copy(this, it) }
}

// endregion

// region Parameters

fun ParadoxParameterInfo.toPsiElement(parent: PsiElement): ParadoxParameterLightElement {
    return ParadoxParameterLightElement(parent, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        .also { ParadoxParameterSupport.Keys.copy(this, it) }
}

fun ParadoxParameterLightElement.toInfo(): ParadoxParameterInfo {
    return ParadoxParameterInfo(name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        .also { ParadoxParameterSupport.Keys.copy(this, it) }
}

// endregion
