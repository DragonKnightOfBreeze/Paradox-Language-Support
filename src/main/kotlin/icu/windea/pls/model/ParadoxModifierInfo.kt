package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.values.LazyValue
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
    // 3.0.1: optimize: use memory-friendly lazy property
    val modificationTracker: ModificationTracker? // region by lazy { computeModificationTracker() }
        get() = LazyValue(_modificationTracker) { computeModificationTracker().also { _modificationTracker = it } }
    @Volatile private var _modificationTracker: Any? = LazyValue.UNINITIALIZED // endregion

    private fun computeModificationTracker() = support?.getModificationTracker(this)

    companion object {
        @JvmField val EMPTY = ParadoxModifierInfo("", ParadoxGameType.Core, getDefaultProject())
    }
}
