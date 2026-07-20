package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.ep.resolve.modifier.support

/**
 * 修正信息。
 *
 * @see ParadoxModifierSupport
 */
data class ParadoxModifierInfo(
    val name: String,
    val gameType: ParadoxGameType,
    val project: Project,
) : UserDataHolderBase() {
    val modificationTracker by lazy { support?.getModificationTracker(this) }

    companion object {
        val EMPTY = ParadoxModifierInfo("", ParadoxGameType.Core, getDefaultProject())
    }
}
