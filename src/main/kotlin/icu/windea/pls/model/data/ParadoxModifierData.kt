package icu.windea.pls.model.data

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.model.*

data class ParadoxModifierData(
    val name: String,
    val gameType: ParadoxGameType,
    val project: Project,
) : UserDataHolderBase() {
    val modificationTracker by lazy { support?.getModificationTracker(this) }
    
    companion object {
        val EMPTY = ParadoxModifierData("", ParadoxGameType.placeholder(), getDefaultProject())
    }
}

fun ParadoxModifierData.toPsiElement(parent: PsiElement): ParadoxModifierElement {
    return ParadoxModifierElement(parent, name, gameType, project).also { copyUserDataTo(it) }
}

fun ParadoxModifierElement.toData(): ParadoxModifierData {
    return ParadoxModifierData(name, gameType, project).also { copyUserDataTo(it) }
}

