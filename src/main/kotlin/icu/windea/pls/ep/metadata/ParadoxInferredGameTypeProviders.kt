package icu.windea.pls.ep.metadata

import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.model.*

class ParadoxGameDataModPathBasedInferredGameTypeProvider : ParadoxInferredGameTypeProvider {
    override fun getGameType(rootFile: VirtualFile): ParadoxGameType? {
        //如果模组目录直接位于游戏数据目录下的mod子目录下，直接推断为对应的游戏类型
        val parentDir = rootFile.parent
        val modDir = parentDir.takeIf { it.name == "mod" } ?: return null
        val gameDataDir = modDir.parent ?: return null
        val gameName = gameDataDir.name
        val gameType = ParadoxGameType.entries.find { it.title == gameName } ?: return null
        if (PlsFacade.getDataProvider().getGameDataPath(gameName) != gameDataDir.toNioPath()) return null
        return gameType
    }
}

class ParadoxWorkshopPathBasedInferredGameTypeProvider : ParadoxInferredGameTypeProvider {
    override fun getGameType(rootFile: VirtualFile): ParadoxGameType? {
        //如果模组目录直接位于游戏创意工坊目录下，直接推断为对应的游戏类型
        val parentDir = rootFile.parent
        val steamWorkshopDir = parentDir ?: return null
        val steamId = steamWorkshopDir.name
        val gameType = ParadoxGameType.entries.find { it.steamId == steamId } ?: return null
        if (PlsFacade.getDataProvider().getSteamWorkshopPath(steamId) != steamWorkshopDir.toNioPath()) return null
        return gameType
    }
}
