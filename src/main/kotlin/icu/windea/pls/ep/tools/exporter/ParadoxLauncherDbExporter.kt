package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.orNull
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
import icu.windea.pls.model.ParadoxModSource
import icu.windea.pls.model.tools.ParadoxModSetInfo
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.or
import org.ktorm.dsl.update
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists

/**
 * 导出模组信息到官方启动器的数据库文件。
 *
 * 数据文件默认为游戏数据目录下的 `launcher-v2.sqlite`。
 *
 * 导出时会排除本地源的模组。
 *
 * 参见：[ParadoxLauncherExporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter.cs)
 */
open class ParadoxLauncherDbExporter : ParadoxDbBasedModExporter() {
    override val text: String = PlsBundle.message("mod.exporter.launcher")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModExporter.Result {
        // 仅导出启用的模组
        val mods = modSetInfo.mods.filter { it.enabled }
        if (mods.isEmpty()) {
            return ParadoxModExporter.Result(total = 0, actualTotal = 0)
        }

        // 连接 SQLite（launcher-v2.sqlite 或 beta）。
        val db = Database.connect("jdbc:sqlite:${filePath.toAbsolutePath()}", driver = "org.sqlite.JDBC")

        // 确定 position 字段语义：
        // - V2：TEXT，通常使用 (index + 4096 + 1) 的十进制字符串，长度 10，前导 0 填充
        // - V4+：INTEGER，从 0 开始的整数
        val isV4Plus = runCatching { db.sequenceOf(KnexMigrations).find { it.name eq Constants.sqlV4Id } != null }.getOrDefault(false)

        val playsetName = modSetInfo.name.ifBlank { Constants.defaultModSetName }

        // 将所有 playset 标记为非激活
        db.update(Playsets) { set(it.isActive, false) }

        // 获取或创建目标播放集，并标记为激活
        val playsets = db.sequenceOf(Playsets)
        var playset: PlaysetEntity? = playsets.find { it.name eq playsetName }
        if (playset == null) {
            playset = PlaysetEntity {
                this.id = UUID.randomUUID().toString().lowercase()
                this.name = playsetName
                this.isActive = true
            }
            playsets.add(playset)
        } else {
            db.update(Playsets) {
                set(it.isActive, true)
                where { it.id eq playset.id }
            }
        }

        // 清空原有的映射关系
        db.delete(PlaysetsMods) { it.playsetId eq playset.id }

        val modsSeq = db.sequenceOf(Mods)
        val mappings = db.sequenceOf(PlaysetsMods)
        var inserted = 0
        mods.forEachIndexed f@{ index, m ->
            val modInfo = ParadoxMetadataManager.getModInfoFromModDirectory(m.modDirectory) ?: return@f
            val displayName = m.name?.orNull() ?: modInfo.name
            val remoteId = m.remoteId?.orNull() ?: modInfo.remoteId?.orNull()
            val source = m.source ?: modInfo.source

            // 导出到官方启动器时，排除本地源的模组
            if (source == ParadoxModSource.Local) return@f
            val steamId = remoteId?.takeIf { source == ParadoxModSource.Steam }
            val pdxId = remoteId?.takeIf { source == ParadoxModSource.Paradox }
            if (steamId == null && pdxId == null) return@f

            // 查找或创建 mod 记录
            var mod: ModEntity? = modsSeq.find { (it.steamId eq remoteId) or (it.pdxId eq remoteId) }
            if (mod == null) {
                mod = ModEntity {
                    this.id = UUID.randomUUID().toString().lowercase()
                    this.pdxId = pdxId
                    this.steamId = steamId
                    this.displayName = displayName
                    this.source = source.id
                }
                modsSeq.add(mod)
            }

            val positionValue = ParadoxMetadataManager.formatLauncherPosition(index, isV4Plus)

            mappings.add(PlaysetsModEntity {
                this.playsetId = playset.id
                this.modId = mod.id
                this.position = positionValue
            })
            inserted++
        }

        return ParadoxModExporter.Result(total = mods.size, actualTotal = inserted)
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
