package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.ep.tools.model.KnexMigrations
import icu.windea.pls.ep.tools.model.ModEntity
import icu.windea.pls.ep.tools.model.Mods
import icu.windea.pls.ep.tools.model.PlaysetEntity
import icu.windea.pls.ep.tools.model.Playsets
import icu.windea.pls.ep.tools.model.PlaysetsModEntity
import icu.windea.pls.ep.tools.model.PlaysetsMods
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.update
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sequenceOf
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * 导出模组信息到官方启动器的数据库文件。
 *
 * 数据文件默认为游戏数据目录下的 `launcher-v2.sqlite`。
 *
 * 参见：[ParadoxLauncherExporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter.cs)
 */
open class ParadoxLauncherDbExporter : ParadoxDbBasedModExporter() {
    override val text: String = PlsBundle.message("mod.exporter.launcher")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModExporter.Result {
        // 连接 SQLite（launcher-v2.sqlite 或 beta）。
        val db = Database.connect("jdbc:sqlite:${filePath.toAbsolutePath()}", driver = "org.sqlite.JDBC")

        // 确定 position 字段语义：
        // - V2：TEXT，通常使用 (index + 4096 + 1) 的十进制字符串，长度 10，前导 0 填充
        // - V4+：INTEGER，从 0 开始的整数
        val isV4Plus = db.sequenceOf(KnexMigrations)
            .firstOrNull { KnexMigrations.name eq Constants.sqlV4Id } != null

        val playsetName = modSetInfo.name.ifBlank { Constants.defaultModSetName }

        // 将所有 playset 标记为非激活
        db.update(Playsets) { set(it.isActive, false) }

        // 获取或创建目标播放集，并标记为激活
        val playsets = db.sequenceOf(Playsets)
        var playset: PlaysetEntity? = playsets.find { Playsets.name eq playsetName }
        if (playset == null) {
            playset = PlaysetEntity { this.name = playsetName; this.isActive = true }
            playsets.add(playset)
            // 重新查询以获得自增 id
            playset = playsets.firstOrNull { Playsets.name eq playsetName }
        } else {
            db.update(Playsets) {
                set(it.isActive, true)
                where { it.id eq playset.id }
            }
        }
        val playsetId = playset?.id ?: return ParadoxModExporter.Result(0, 0)

        // 清空原有的映射关系
        db.delete(PlaysetsMods) { it.playsetId eq playsetId }

        // 仅导出启用的模组，解析远端 ID（优先 remoteId；否则解析 descriptor.mod 的 remote_file_id）
        val modsEnabled = modSetInfo.mods.filter { it.enabled }


        val modsSeq = db.sequenceOf(Mods)
        val mappings = db.sequenceOf(PlaysetsMods)
        var inserted = 0
        modsEnabled.forEachIndexed { index, m ->
            // 解析远端 ID：优先使用结构中提供的 remoteId，否则从 descriptor.mod 中读取
            val remoteId = m.remoteId ?: ParadoxMetadataManager.getRemoteFileIdFromModDir(m.modDirectory) ?: return@forEachIndexed
            // 查找或创建 mod 记录
            var mod: ModEntity? = modsSeq.find { Mods.steamId eq remoteId }
            if (mod == null) mod = modsSeq.find { Mods.pdxId eq remoteId }
            if (mod == null) {
                val displayName = m.name ?: ParadoxMetadataManager.getModDisplayNameFromDescriptor(m.modDirectory)
                modsSeq.add(ModEntity {
                    this.displayName = displayName
                    this.steamId = remoteId // 默认按 steamId 写出
                    this.pdxId = null
                })
                mod = modsSeq.firstOrNull { Mods.steamId eq remoteId }
            }
            val modId = mod?.id ?: return@forEachIndexed

            val positionValue = ParadoxMetadataManager.formatLauncherPosition(index, isV4Plus)

            mappings.add(PlaysetsModEntity {
                this.playsetId = playsetId
                this.modId = modId
                this.position = positionValue
            })
            inserted++
        }

        return ParadoxModExporter.Result(total = modsEnabled.size, actualTotal = inserted)
    }

    override fun createFileSaverDescriptor(gameType: ParadoxGameType): FileSaverDescriptor {
        return FileSaverDescriptor(PlsBundle.message("mod.exporter.launcher"), "", "sqlite")
    }

    override fun getSavedBaseDir(gameType: ParadoxGameType): Path? {
        // 游戏数据目录
        return PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.takeIf { it.exists() }
    }

    override fun getSavedFileName(gameType: ParadoxGameType): String? {
        return getDbFileName()
    }

    protected open fun getDbFileName() = Constants.launcherDbPath
}
