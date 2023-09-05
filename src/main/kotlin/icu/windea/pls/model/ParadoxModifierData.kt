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
        return ParadoxModifierElement(element, name, gameType, project).also { copyUserDataTo(it) }
    }
    
    companion object {
        val EMPTY = ParadoxModifierData("", ParadoxGameType.placeholder(), getDefaultProject())
    }
}