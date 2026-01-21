package icu.windea.pls.ep.analysis

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.ParadoxGameType

/**
 * 如果模组目录直接位于游戏数据目录下的 mod 子目录下，直接推断为对应的游戏类型。
 */
class ParadoxGameDataModPathBasedInferredGameTypeProvider : ParadoxInferredGameTypeProvider {
    override fun get(rootFile: VirtualFile): ParadoxGameType? {
        val parentDir = rootFile.parent
        val modDir = parentDir.takeIf { it.name == "mod" } ?: return null
        val gameDataDir = modDir.parent ?: return null
        val gameName = gameDataDir.name
        val gameType = ParadoxGameType.getAll().find { it.title == gameName } ?: return null
        if (PlsPathService.getInstance().getGameDataPath(gameName) != gameDataDir.toNioPath()) return null
        return gameType
    }
}

/**
 * 如果模组目录直接位于游戏创意工坊目录下，直接推断为对应的游戏类型。
 */
class ParadoxWorkshopPathBasedInferredGameTypeProvider : ParadoxInferredGameTypeProvider {
    override fun get(rootFile: VirtualFile): ParadoxGameType? {
        val parentDir = rootFile.parent
        val steamWorkshopDir = parentDir ?: return null
        val steamId = steamWorkshopDir.name
        val gameType = ParadoxGameType.getAll().find { it.steamId == steamId } ?: return null
        if (PlsPathService.getInstance().getSteamGameWorkshopPath(steamId) != steamWorkshopDir.toNioPath()) return null
        return gameType
    }
}
