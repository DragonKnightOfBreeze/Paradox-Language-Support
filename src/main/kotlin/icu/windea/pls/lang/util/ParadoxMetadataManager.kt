package icu.windea.pls.lang.util

import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.util.data.ParadoxScriptDataResolver
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLauncherSettingsInfo
import icu.windea.pls.model.ParadoxModDescriptorInfo
import icu.windea.pls.model.ParadoxModMetadataInfo
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.stringValue
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object ParadoxMetadataManager {
    val metadataFileNames = setOf(
        "launcher-settings.json",
        "descriptor.mod",
        "metadata.json",
    )

    fun getLauncherSettingsFile(rootFile: VirtualFile): VirtualFile? {
        //relative paths:
        //* launcher-settings.json
        //* launcher/launcher-settings.json

        if (rootFile.name == "launcher") return null
        rootFile.findChild("launcher-settings.json")
            ?.takeIf { it.isFile }?.let { return it }
        rootFile.findFileByRelativePath("launcher/launcher-settings.json")
            ?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun getLauncherSettingsInfo(file: VirtualFile): ParadoxLauncherSettingsInfo? {
        try {
            return runReadAction { doGetLauncherSettingsInfo(file) }
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    private fun doGetLauncherSettingsInfo(file: VirtualFile): ParadoxLauncherSettingsInfo {
        return ObjectMappers.jsonMapper.readValue(file.inputStream)
    }

    fun getModDescriptorFile(rootFile: VirtualFile): VirtualFile? {
        //relative paths:
        //* descriptor.mod

        rootFile.findChild("descriptor.mod")
            ?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun getModDescriptorInfo(file: VirtualFile): ParadoxModDescriptorInfo? {
        try {
            return runReadAction { doGetModDescriptorInfo(file) }
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    private fun doGetModDescriptorInfo(file: VirtualFile): ParadoxModDescriptorInfo {
        //val psiFile = file.toPsiFile<ParadoxScriptFile>(getDefaultProject()) ?: return null //会导致StackOverflowError
        val psiFile = ParadoxScriptElementFactory.createDummyFile(getDefaultProject(), file.inputStream.reader().readText())
        val data = ParadoxScriptDataResolver.resolve(psiFile)
        val name = data?.getData("name")?.value?.stringValue() ?: file.parent?.name ?: "" //如果没有name属性，则使用根目录名
        val version = data?.getData("version")?.value?.stringValue()
        val picture = data?.getData("picture")?.value?.stringValue()
        val tags = data?.getAllData("tags")?.mapNotNull { it.value?.stringValue() }?.toSet().orEmpty()
        val supportedVersion = data?.getData("supported_version")?.value?.stringValue()
        val remoteFileId = data?.getData("remote_file_id")?.value?.stringValue()
        val path = data?.getData("path")?.value?.stringValue()
        return ParadoxModDescriptorInfo(name, version, picture, tags, supportedVersion, remoteFileId, path)
    }

    fun getModMetadataFile(rootFile: VirtualFile): VirtualFile? {
        //relative paths:
        //* .metadata/metadata.json

        rootFile.findFileByRelativePath(".metadata/metadata.json")
            ?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun getModMetadataInfo(file: VirtualFile): ParadoxModMetadataInfo? {
        try {
            return runReadAction { doGetModMetadataInfo(file) }
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    private fun doGetModMetadataInfo(file: VirtualFile): ParadoxModMetadataInfo {
        return ObjectMappers.jsonMapper.readValue(file.inputStream)
    }

    // Descriptors

    fun useDescriptorMod(gameType: ParadoxGameType): Boolean {
        // TODO 2.0.5+ 提取 ModDescriptorType，避免硬编码
        return gameType != ParadoxGameType.Vic3
    }

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
        val descriptorInfo = getModDescriptorInfo(descriptorFile) ?: return null
        val modPath = descriptorInfo.path ?: return null
        val modDir = modPath.toVirtualFile() ?: return null
        val rootInfo = modDir.rootInfo
        if (rootInfo == null) return null // 必须是合法的模组根目录
        return modDir.path
    }

    /**
     * 从模组目录解析 steam 的 `remote_file_id`（descriptor.mod 中的 `remote_file_id`）。
     *
     * 失败时返回 null。
     */
    fun getRemoteFileIdFromModDir(modDir: String?): String? {
        if (modDir.isNullOrEmpty()) return null
        val vfile = modDir.toVirtualFile() ?: return null
        val descriptor = getModDescriptorFile(vfile) ?: return null
        val info = getModDescriptorInfo(descriptor) ?: return null
        return info.remoteFileId
    }

    /**
     * 尝试基于模组目录下的 descriptor.mod 解析显示名称（displayName）。
     *
     * 若目录或文件不存在，或解析失败，返回 null。
     */
    fun getModDisplayNameFromDescriptor(modDir: String?): String? {
        if (modDir.isNullOrEmpty()) return null
        val vfile = modDir.toVirtualFile() ?: return null
        val descriptor = getModDescriptorFile(vfile) ?: return null
        val info = getModDescriptorInfo(descriptor) ?: return null
        return info.name
    }

    // Models

    /**
     * 自动探测官方启动器播放列表（playlist.json）的 position 字段类型是否为整数（对应 V3）。
     *
     * 若返回 true，表示 position 为整数（V3）；若返回 false，表示为字符串（V2）；若无法探测则返回 null。
     */
    fun detectLauncherPlaylistPositionIsInt(file: VirtualFile): Boolean? {
        return try {
            val root = ObjectMappers.jsonMapper.readTree(file.inputStream)
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
        val vfile = path.toVirtualFile(true) ?: return null
        return detectLauncherPlaylistPositionIsInt(vfile)
    }

    /**
     * 将 V2/V4+ 的 position 统一解析为可比较的数值。
     *
     * - V2：字符串（通常左侧补零），会先 `trimStart('0')` 再 `toIntOrNull()`；
     * - V4+：数据库字段为 INTEGER，JDBC 读取为字符串时同样可以正确解析；
     * - 失败时返回 `Int.MAX_VALUE`，用于排序时落在末尾。
     */
    fun parseLauncherV2PositionToInt(pos: String?): Int {
        if (pos.isNullOrEmpty()) return Int.MAX_VALUE
        return pos.trim().trimStart('0').toIntOrNull() ?: Int.MAX_VALUE
    }

    /**
     * 根据版本生成 position 值。
     *
     * - V2：生成十位、左侧补零的十进制字符串 `(index + 4097)`；
     * - V4+：使用从 0 开始的整数，转为字符串。
     */
    fun formatLauncherPosition(index: Int, isV4Plus: Boolean): String {
        return if (isV4Plus) index.toString() else (index + 1 + 4096).toString(10).padStart(10, '0')
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
                    val info = getModDescriptorInfo(vf) ?: continue
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
