package icu.windea.pls.lang.util

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.util.jsonMapper
import icu.windea.pls.lang.analyze.ParadoxMetadataService
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.settings.ParadoxModDescriptorSettingsState
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.metadata.ParadoxDescriptorModInfo
import icu.windea.pls.model.metadata.ParadoxLauncherSettingsJsonInfo
import icu.windea.pls.model.metadata.ParadoxMetadataJsonInfo
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object ParadoxMetadataManager {
    // Files and Infos

    fun getLauncherSettingsJsonFile(rootFile: VirtualFile): VirtualFile? {
        return runReadActionSmartly { ParadoxMetadataService.getLauncherSettingsJsonFile(rootFile) }
    }

    fun getLauncherSettingsJsonInfo(file: VirtualFile): ParadoxLauncherSettingsJsonInfo? {
        try {
            return runReadActionSmartly { ParadoxMetadataService.resolveLauncherSettingsJsonInfo(file) }
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    fun getDescriptorModFile(rootFile: VirtualFile): VirtualFile? {
        return runReadActionSmartly { ParadoxMetadataService.getDescriptorModFile(rootFile) }
    }

    fun getDescriptorModInfo(file: VirtualFile): ParadoxDescriptorModInfo? {
        try {
            return runReadActionSmartly { ParadoxMetadataService.resolveDescriptorModInfo(file) }
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    fun getMetadataJsonFile(rootFile: VirtualFile): VirtualFile? {
        return runReadActionSmartly { ParadoxMetadataService.getMetadataJsonFile(rootFile) }
    }

    fun getMetadataJsonInfo(file: VirtualFile): ParadoxMetadataJsonInfo? {
        try {
            return runReadActionSmartly { ParadoxMetadataService.resolveMetadataJsonInfo(file) }
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    fun useDescriptorMod(gameType: ParadoxGameType): Boolean {
        if (gameType == ParadoxGameType.Core) return false
        return gameType !in ParadoxGameType.getAllUseMetadataJson()
    }

    @Suppress("unused")
    fun useMetadataJson(gameType: ParadoxGameType): Boolean {
        if (gameType == ParadoxGameType.Core) return false
        return gameType in ParadoxGameType.getAllUseMetadataJson()
    }

    // Get From Metadata

    fun getModDirectoryFromSteamId(steamId: String?, workshopDirPath: Path): String? {
        if (steamId.isNullOrEmpty()) return null
        val path = workshopDirPath.resolve(steamId)
        if (!path.exists()) return null
        val modDir = path.toVirtualFile(true) ?: return null
        val rootInfo = modDir.rootInfo
        if (rootInfo == null) return null // 必须是合法的模组根目录
        return modDir.path
    }

    fun getModDirectoryFromModDescriptorPathInGameData(path: String?, gameDataDirPath: Path): String? {
        if (path.isNullOrEmpty()) return null
        if (!path.endsWith(".mod", true)) return null // 检查后缀名
        val descriptorPath = gameDataDirPath.resolve(path)
        if (!descriptorPath.exists()) return null
        val descriptorFile = descriptorPath.toVirtualFile(true) ?: return null
        val descriptorInfo = getDescriptorModInfo(descriptorFile) ?: return null
        val modPath = descriptorInfo.path ?: return null
        val modDir = modPath.toVirtualFile() ?: return null
        val rootInfo = modDir.rootInfo
        if (rootInfo == null) return null // 必须是合法的模组根目录
        return modDir.path
    }

    /**
     * 从模组目录获取模组信息，从而统一获取各种需要进一步获取的信息。
     *
     * 注意：需要调用这个方法以确保模组信息被解析，相关的配置项（[ParadoxModDescriptorSettingsState]）被创建。
     */
    fun getModInfoFromModDirectory(modDirectory: String?): ParadoxRootInfo.Mod? {
        if (modDirectory.isNullOrEmpty()) return null
        val file = modDirectory.toVirtualFile() ?: return null
        val rootInfo = file.rootInfo ?: return null
        return rootInfo.castOrNull()
    }

    // Models

    /**
     * 自动探测官方启动器播放列表（playlist.json）的 position 字段类型是否为整数（对应 V3）。
     *
     * 若返回 true，表示 position 为整数（V3）；若返回 false，表示为字符串（V2）；若无法探测则返回 null。
     */
    fun detectLauncherPlaylistPositionIsInt(file: VirtualFile): Boolean? {
        return try {
            val root = file.inputStream.use { jsonMapper.readTree(it) }
            val modsNode = root.get("mods") ?: return null
            val first = modsNode.firstOrNull() ?: return null
            first.get("position")?.isInt
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            null
        }
    }

    /**
     * 自动探测官方启动器播放列表（playlist.json）的 position 字段类型（Path 版本）。
     */
    fun detectLauncherPlaylistPositionIsInt(path: Path): Boolean? {
        val file = path.toVirtualFile(true) ?: return null
        return detectLauncherPlaylistPositionIsInt(file)
    }

    /**
     * 将 V2/V4+ 的 position 统一解析为可比较的数值。
     *
     * - V2：字符串（通常左侧补零，部分版本使用十六进制 10 位小写，如 0x1001 => "0000001001"）。
     *   同时兼容十进制与十六进制：若包含 a-f/A-F，则按 16 进制解析，否则按 10 进制解析。
     * - V4+：数据库字段为 INTEGER，JDBC 读取为字符串时同样可以正确解析；
     * - 失败时返回 `Int.MAX_VALUE`，用于排序时落在末尾。
     */
    fun parseLauncherV2PositionToInt(pos: String?): Int {
        if (pos.isNullOrBlank()) return Int.MAX_VALUE
        val body = pos.trim().trimStart('0').ifEmpty { "0" }
        val dec = body.toIntOrNull(10)
        val hex = body.toIntOrNull(16)
        // V2 期望的序列从 4097 开始（index + 4096 + 1）
        val min = 4097
        val decOk = dec != null && dec >= min
        val hexOk = hex != null && hex >= min
        return when {
            decOk && hexOk -> dec // 两者都可行，优先认为来源是十进制（如 0000004097）
            decOk -> dec
            hexOk -> hex // 十进制不合理（如 0000001001 -> 1001），则采用十六进制（0x1001 -> 4097）
            else -> dec ?: hex ?: Int.MAX_VALUE
        }
    }

    /**
     * 根据版本生成 position 值。
     *
     * - V2：生成十位、左侧补零的十六进制字符串 `(index + 4097)`，小写：`toString(16).padStart(10, '0')`；
     * - V4+：使用从 0 开始的整数，转为字符串。
     */
    fun formatLauncherPosition(index: Int, isV4Plus: Boolean): String {
        return if (isV4Plus) index.toString() else (index + 1 + 4096).toString(16).padStart(10, '0')
    }

    /**
     * 扫描游戏数据目录下的 mod/\*.mod，解析其中的 path 字段，建立到相对路径（mod/xxx.mod）的映射。
     */
    fun buildDescriptorMapping(gameDataDirPath: Path): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val modDir = gameDataDirPath.resolve("mod")
        if (!modDir.exists()) return result
        try {
            Files.newDirectoryStream(modDir) { it.fileName.toString().endsWith(".mod", ignoreCase = true) }.use { ds ->
                for (p in ds) {
                    val vf = p.toVirtualFile(true) ?: continue
                    val info = getDescriptorModInfo(vf) ?: continue
                    val modPath = info.path ?: continue
                    result[modPath.normalizePath()] = "mod/${vf.name}"
                }
            }
        } catch (_: Exception) {
            // 忽略读取失败，尽力而为
        }
        return result
    }
}
