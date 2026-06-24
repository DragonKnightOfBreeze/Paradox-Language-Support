package icu.windea.pls.ep.analysis

import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.tools.SpecialPathService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxGameTypeInfo
import java.nio.file.Path
import kotlin.io.path.name

/**
 * 如果模组目录直接位于游戏数据目录下的 mod 子目录下，直接推断为对应的游戏类型。
 */
class ParadoxGameDataModPathBasedInferredGameTypeProvider : ParadoxInferredGameTypeProvider {
    private val gameTypeMap = ParadoxGameType.getAllSpecific().associateBy { it.title }

    override fun getInferredGameTypeInfo(rootPath: Path): ParadoxGameTypeInfo? {
        val parentPath = rootPath.parent ?: return null
        val modPath = parentPath.takeIf { it.name == "mod" } ?: return null
        val gameDataPath = modPath.parent ?: return null
        val gameName = gameDataPath.name
        val gameType = gameTypeMap[gameName] ?: return null
        if (gameDataPath != SpecialPathService.getInstance().getGameDataPath(gameType)) return null
        return ParadoxGameTypeInfo(gameType, PlsBundle.lazyMessage("gameType.message.gameDataMod"))
    }
}

/**
 * 如果模组目录直接位于游戏创意工坊目录下，直接推断为对应的游戏类型。
 */
class ParadoxWorkshopPathBasedInferredGameTypeProvider : ParadoxInferredGameTypeProvider {
    private val gameTypeMap = ParadoxGameType.getAllSpecific().associateBy { it.steamId }

    override fun getInferredGameTypeInfo(rootPath: Path): ParadoxGameTypeInfo? {
        val parentPath = rootPath.parent ?: return null
        val steamWorkshopPath = parentPath
        val steamId = steamWorkshopPath.name
        val gameType = gameTypeMap[steamId] ?: return null
        if (steamWorkshopPath != SpecialPathService.getInstance().getSteamGameWorkshopPath(steamId)) return null
        return ParadoxGameTypeInfo(gameType, PlsBundle.lazyMessage("gameType.message.workshop"))
    }
}
