package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement

/**
 * 修正信息。
 *
 * @see ParadoxModifierLightElement
 * @see ParadoxModifierSupport
 */
class ParadoxModifierInfo(
    val name: String,
    val project: Project,
    val gameType: ParadoxGameType,
) : UserDataHolderBase() {
    // 3.0.1 optimize: use memory-friendly lazy property
    val modificationTracker: ModificationTracker? // region by lazy { computeModificationTracker() }
        get() = LazyValue.ofNullable({ _modificationTracker }, { _modificationTracker = it }) { computeModificationTracker() }
    @Volatile private var _modificationTracker = LazyValue.UNINITIALIZED // endregion

    private fun computeModificationTracker() = support?.getModificationTracker(this)

    override fun toString(): String {
        return "ParadoxModifierInfo(name=$name, project=$project, gameType=$gameType)"
    }

    companion object {
        @JvmField val EMPTY = ParadoxModifierInfo("", getDefaultProject(), ParadoxGameType.Core)
    }
}
