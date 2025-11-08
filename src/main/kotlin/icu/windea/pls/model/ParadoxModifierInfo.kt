package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.ep.resolve.modifier.support
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement

data class ParadoxModifierInfo(
    val name: String,
    val gameType: ParadoxGameType,
    val project: Project,
) : UserDataHolderBase() {
    val modificationTracker by lazy { support?.getModificationTracker(this) }

    companion object {
        val EMPTY by lazy { ParadoxModifierInfo("", ParadoxGameType.Core, getDefaultProject()) }
    }
}

fun ParadoxModifierInfo.toPsiElement(parent: PsiElement): ParadoxModifierElement {
    return ParadoxModifierElement(parent, name, gameType, project)
        .also { ParadoxModifierSupport.Keys.syncUserData(this, it) }
}

fun ParadoxModifierElement.toInfo(): ParadoxModifierInfo {
    return ParadoxModifierInfo(name, gameType, project)
        .also { ParadoxModifierSupport.Keys.syncUserData(this, it) }
}
