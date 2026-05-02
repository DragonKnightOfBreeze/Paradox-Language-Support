package icu.windea.pls.lang.tools

import com.intellij.openapi.components.serviceOrNull
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path

interface PlsPathService {
    fun initAsync()

    /**
     * 得到 Steam 目录的路径（可能不存在）。
     */
    fun getSteamPath(): Path?

    /**
     * 得到 [steamId] 和 [gameName] 对应的 Steam 游戏目录的路径（可能不存在）。
     */
    fun getSteamGamePath(steamId: String, gameName: String): Path?

    /**
     * 得到 [steamId] 对应的 Steam 创意工坊目录的路径（可能不存在）。
     */
    fun getSteamGameWorkshopPath(steamId: String): Path?

    /**
     * 得到 [gameType] 对应的游戏数据目录的路径（可能不存在）。
     */
    fun getGameDataPath(gameType: ParadoxGameType): Path?

    /**
     * 在系统文件管理器中打开路径。
     */
    fun openPath(path: Path)

    /**
     * 复制路径到剪贴板。
     */
    fun copyPath(path: Path)

    companion object {
        @JvmStatic
        fun getInstance(): PlsPathService = serviceOrNull() ?: PlsPathServiceImpl()
    }
}
