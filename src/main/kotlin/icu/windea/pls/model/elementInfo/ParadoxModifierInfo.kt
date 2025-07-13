package icu.windea.pls.model.elementInfo

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.model.*

data class ParadoxModifierInfo(
    override val name: String,
    override val gameType: ParadoxGameType,
    override val project: Project,
) : UserDataHolderBase(), ParadoxElementInfo {
    val modificationTracker by lazy { support?.getModificationTracker(this) }

    companion object {
        val EMPTY by lazy { ParadoxModifierInfo("", ParadoxGameType.placeholder(), getDefaultProject()) }
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
