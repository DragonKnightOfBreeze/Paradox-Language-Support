package icu.windea.pls.ep.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.model.*

/**
 * 用于获取规则分组中的文件。
 */
interface CwtConfigGroupFileProvider {
    val type: Type

    val isEnabled: Boolean

    /**
     * 得到规则的根目录，其中所有规则分组目录的父目录。
     */
    fun getRootDirectory(project: Project): VirtualFile?

    /**
     * 基于游戏类型，得到规则的根目录中的对应的规则分组的目录的名字。
     */
    fun getDirectoryName(project: Project, gameType: ParadoxGameType?): String {
        return gameType.id
    }

    /**
     * 基于规则分组的目录的名字，得到对应的游戏类型。
     */
    fun getGameTypeIdFromDirectoryName(project: Project, directoryName: String): String? {
        return directoryName.takeIf { ParadoxGameType.canResolve(it) }
    }

    fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup?

    fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean

    fun getHintMessage(): String? = null

    fun getNotificationMessage(configGroup: CwtConfigGroup): String? = null

    enum class Type {
        BuiltIn, Remote, Local
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupFileProvider>("icu.windea.pls.configGroupFileProvider")
    }
}
