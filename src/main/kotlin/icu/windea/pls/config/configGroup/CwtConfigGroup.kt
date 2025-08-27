package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.id
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 规则分组。
 * @property gameType 对应的游戏类型。如果为null，则会得到共享的规则分组。
 * @property project 对应的项目。如果不需要访问PSI，可以直接传入默认项目。
 */
class CwtConfigGroup(
    val gameType: ParadoxGameType?,
    val project: Project,
) : UserDataHolderBase() {
    val changed = AtomicBoolean()
    val modificationTracker = SimpleModificationTracker()

    val files: MutableMap<String, CwtFileConfig> = mutableMapOf()

    override fun equals(other: Any?): Boolean {
        return this === other || (other is CwtConfigGroup && gameType == other.gameType && project == other.project)
    }

    override fun hashCode(): Int {
        return Objects.hash(gameType, project)
    }

    override fun toString(): String {
        return "CwtConfigGroup(gameType=${gameType.id}, project=$project)"
    }

    object Keys : KeyRegistry()
}
