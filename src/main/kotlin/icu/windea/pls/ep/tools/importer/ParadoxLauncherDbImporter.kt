package icu.windea.pls.ep.tools.importer

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.ep.tools.model.ModEntity
import icu.windea.pls.ep.tools.model.Mods
import icu.windea.pls.ep.tools.model.PlaysetEntity
import icu.windea.pls.ep.tools.model.Playsets
import icu.windea.pls.ep.tools.model.PlaysetsMods
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModInfo
import icu.windea.pls.model.tools.ParadoxModSetInfo
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

/**
 * 从官方启动器的数据库文件导入模组信息。
 *
 * 数据文件默认为游戏数据目录下的 `launcher-v2.sqlite`。
 *
 * 参见：[ParadoxLauncherImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporter.cs)
 */
open class ParadoxLauncherDbImporter : ParadoxDbBasedModImporter() {
    override val text: String = PlsBundle.message("mod.importer.launcher")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModImporter.Result {
        val gameType = modSetInfo.gameType

        // 校验 Steam 创意工坊目录
        val workshopDirPath = PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)
            ?: throw IllegalStateException(PlsBundle.message("mod.importer.error.steamWorkshopDir0"))
        if (workshopDirPath.notExists()) {
            throw IllegalStateException(PlsBundle.message("mod.importer.error.steamWorkshopDir", workshopDirPath))
        }

        // 连接 SQLite（launcher-v2.sqlite）。
        val db = Database.connect("jdbc:sqlite:${filePath.toAbsolutePath()}", driver = "org.sqlite.JDBC")

        // 读取激活的播放集（playsets.isActive=1）。若不存在，则退回任意一个。
        val playsets = db.sequenceOf(Playsets)
        val activePlayset: PlaysetEntity? = playsets.filter { Playsets.isActive eq true }.firstOrNull()
            ?: playsets.firstOrNull()

        if (activePlayset == null) {
            // 数据库存在但没有任何播放集，返回空结果
            val empty = ParadoxModSetInfo(gameType, Constants.defaultModSetName, emptyList())
            return ParadoxModImporter.Result(total = 0, actualTotal = 0, newModSetInfo = empty)
        }

        // 读取播放集中的模组及其顺序。注意：不同版本 position 可能为 TEXT（V2）或 INTEGER（V4+），这里以字符串形式读取并解析为数值排序。
        val mappings = db.sequenceOf(PlaysetsMods)
            .filter { PlaysetsMods.playsetId eq activePlayset.id }
            .toList()
            // 统一按数值顺序：V2 字符串左零 -> 去零转 Int；V4+ INTEGER -> 转字符串再转 Int
            .sortedBy { ParadoxMetadataManager.parseLauncherV2PositionToInt(it.position) }

        val newModInfos = mutableListOf<ParadoxModInfo>()
        val existingModDirectories = modSetInfo.mods.mapNotNullTo(mutableSetOf()) { it.modDirectory?.orNull() }

        val modsSeq = db.sequenceOf(Mods)
        for (pm in mappings) {
            val mod: ModEntity = modsSeq.find { Mods.id eq pm.modId } ?: continue
            // 仅支持通过 Steam workshop 路径解析（与 Irony 行为一致）；
            // pdxId 对应 Paradox Mods（目录结构不同），此处暂不处理。
            val remoteId = mod.steamId
            val modDirectory = ParadoxMetadataManager.getModDirectoryFromSteamId(remoteId, workshopDirPath) ?: continue
            if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
            newModInfos.add(ParadoxModInfo(modDirectory, true))
        }

        val newModSetInfo = ParadoxModSetInfo(gameType, activePlayset.name, newModInfos)
        return ParadoxModImporter.Result(total = mappings.size, actualTotal = newModInfos.size, newModSetInfo = newModSetInfo)
    }

    override fun createFileChooserDescriptor(gameType: ParadoxGameType): FileChooserDescriptor {
        // 选择 launcher-v2.sqlite 文件
        return FileChooserDescriptorFactory.createSingleFileDescriptor("sqlite")
            .withTitle(PlsBundle.message("mod.importer.launcher"))
    }

    override fun getSelectedFile(gameType: ParadoxGameType): Path? {
        // 默认选择游戏数据目录下的数据库文件，否则回退到游戏数据目录
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.takeIf { it.exists() } ?: return null
        val dbPath = gameDataPath.resolve(getDbFileName())
        return if (dbPath.exists()) dbPath else gameDataPath
    }

    protected open fun getDbFileName() = Constants.launcherDbPath
}
