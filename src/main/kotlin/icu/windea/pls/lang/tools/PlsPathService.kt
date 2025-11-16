package icu.windea.pls.lang.tools

import icu.windea.pls.lang.tools.impl.PlsPathServiceImpl
import java.nio.file.Path

interface PlsPathService {
    fun initAsync()

    /**
     * 得到 Steam 目录的路径。
     */
    fun getSteamPath(): Path?

    /**
     * 得到指定 ID 对应的 Steam 游戏目录的路径。
     */
    fun getSteamGamePath(steamId: String, gameName: String): Path?

    /**
     * 得到指定 ID 对应的 Steam 创意工坊目录的路径。
     */
    fun getSteamWorkshopPath(steamId: String): Path?

    /**
     * 得到指定游戏名对应的游戏数据目录的路径。
     */
    fun getGameDataPath(gameName: String): Path?

    /**
     * 在系统文件管理器中打开路径。
     */
    fun openPath(path:Path)

    /**
     * 复制路径到剪贴板。
     */
    fun copyPath(path: Path)

    companion object : PlsPathService by PlsPathServiceImpl()
}
