package icu.windea.pls.model.elementInfo

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.ep.modifier.ParadoxModifierSupport
import icu.windea.pls.ep.modifier.support
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.model.ParadoxGameType

data class ParadoxModifierInfo(
    override val name: String,
    override val gameType: ParadoxGameType,
    override val project: Project,
) : UserDataHolderBase(), ParadoxElementInfo {
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
