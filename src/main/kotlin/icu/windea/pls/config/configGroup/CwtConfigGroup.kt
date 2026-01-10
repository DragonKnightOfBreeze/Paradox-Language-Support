package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.model.ParadoxGameType
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 规则分组。保存了处理后的所有规则数据。
 *
 * 规则分组会在获取时就保证已经被创建，而其中的规则数据的初始化是在打开 IDE 或项目时异步进行的。
 *
 * @property project 对应的项目。如果是默认项目，则不能用于访问 PSI。
 * @property gameType 对应的游戏类型。如果是 [ParadoxGameType.Core]，则为共享的规则分组。
 *
 * @see CwtConfigGroupDataHolder
 * @see CwtConfigGroupService
 */
interface CwtConfigGroup : CwtConfigGroupDataHolder, UserDataHolder {
    val project: Project
    val gameType: ParadoxGameType

    val initialized: AtomicBoolean
    val changed: AtomicBoolean
    val initializer: CwtConfigGroupInitializer
    val modificationTracker: SimpleModificationTracker

    suspend fun init()

    object Keys : KeyRegistry()
}
