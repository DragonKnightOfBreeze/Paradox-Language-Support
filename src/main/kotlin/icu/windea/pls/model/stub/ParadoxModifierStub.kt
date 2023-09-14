package icu.windea.pls.model.stub

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.model.*

data class ParadoxModifierStub(
    val name: String,
    val gameType: ParadoxGameType,
    val project: Project,
) : UserDataHolderBase() {
    val modificationTracker by lazy { support?.getModificationTracker(this) }
    
    companion object {
        val EMPTY = ParadoxModifierStub("", ParadoxGameType.placeholder(), getDefaultProject())
    }
}

fun ParadoxModifierStub.toPsiElement(parent: PsiElement): ParadoxModifierElement {
    return ParadoxModifierElement(parent, name, gameType, project).also { copyUserDataTo(it) }
}

fun ParadoxModifierElement.toStub(): ParadoxModifierStub {
    return ParadoxModifierStub(name, gameType, project).also { copyUserDataTo(it) }
}

