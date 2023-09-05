package icu.windea.pls.model

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.PsiElement
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.modifier.*

data class ParadoxModifierData(
    val name: String,
    val gameType: ParadoxGameType,
    val project: Project,
): UserDataHolderBase() {
    val modificationTracker by lazy { support?.getModificationTracker(this) }
    
    fun toModifierElement(element: PsiElement) : ParadoxModifierElement{
        return ParadoxModifierElement(element, name, gameType, project).also { copyCopyableDataTo(it) }
    }
    
    companion object {
        val EMPTY = ParadoxModifierData("", ParadoxGameType.placeholder(), getDefaultProject())
    }
    
    object Keys: KeyAware
}

val ParadoxModifierData.Keys.support by createKey<ParadoxModifierSupport>("paradox.modifier.data.support")
val ParadoxModifierData.Keys.modifierConfig by createKey<CwtModifierConfig>("paradox.modifier.data.modifierConfig")

var ParadoxModifierData.support by ParadoxModifierData.Keys.support
var ParadoxModifierElement.support by ParadoxModifierData.Keys.support
var ParadoxModifierData.modifierConfig by ParadoxModifierData.Keys.modifierConfig
var ParadoxModifierElement.modifierConfig by ParadoxModifierData.Keys.modifierConfig