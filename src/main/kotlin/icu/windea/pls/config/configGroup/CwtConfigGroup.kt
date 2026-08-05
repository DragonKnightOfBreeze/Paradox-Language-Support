package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupPostProcessor
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupProcessor
import icu.windea.pls.model.ParadoxGameType

/**
 * 规则分组。保存了处理后的所有规则数据。
 *
 * 规则分组会在获取时就保证已经被创建，而其中的规则数据的初始化是在打开 IDE 或项目时异步进行的。
 *
 * 参考：
 * - 规则系统的说明文档：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 * - 规则格式的参考手册：[ref-config-format.md](https://windea.icu/Paradox-Language-Support/ref-config-format.md)
 *
 * @property project 对应的项目。如果是默认项目，则不能用于访问 PSI。
 * @property gameType 对应的游戏类型。如果是 [ParadoxGameType.Core]，则为通用的规则分组。
 * @property dataModel 底层的数据模型。如果规则分组已被清理，则会得到空模型。
 * @property initializer 底层的用于初始化的可变数据模型。如果规则分组已被清理，则会得到新创建的临时模型。
 *
 * @see CwtConfigGroupDataModel
 * @see CwtConfigGroupService
 * @see CwtConfigGroupProcessor
 * @see CwtConfigGroupPostProcessor
 * @see CwtConfigGroupFileProvider
 */
interface CwtConfigGroup : CwtConfigGroupDataModel, UserDataHolder {
    val project: Project
    val gameType: ParadoxGameType
    var initialized: Boolean
    var changed: Boolean
    val modificationTracker: SimpleModificationTracker
    val dataModel: CwtConfigGroupDataModel
    val initializer: CwtConfigGroupDataModelBase

    suspend fun init()

    fun clear()

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun create(project: Project, gameType: ParadoxGameType): CwtConfigGroup {
            return CwtConfigGroupBase(project, gameType)
        }
    }
}

