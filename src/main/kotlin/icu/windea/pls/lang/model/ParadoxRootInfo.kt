package icu.windea.pls.lang.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import java.nio.file.*

/**
 * @property rootFile 游戏或模组的根目录。
 * @property gameRootFile 游戏文件的根目录。可能等同于rootFile，也可能是其game子目录。
 */
sealed class ParadoxRootInfo {
    abstract val rootFile: VirtualFile
    abstract val gameRootFile: VirtualFile
    abstract val gameType: ParadoxGameType
    
    abstract val rootPath: Path
    abstract val gameRootPath: Path
    
    val gameEntry: String? by lazy { rootPath.relativize(gameRootPath).toString().takeIfNotEmpty() }
    
    abstract val qualifiedName: String
    abstract val isAvailable: Boolean
    
    companion object {
        //rootPath - rootInfo
        val values = mutableMapOf<String, ParadoxRootInfo>()
    }
}

class ParadoxGameRootInfo(
    override val rootFile: VirtualFile,
    val launcherSettingsFile: VirtualFile,
    val launcherSettingsInfo: ParadoxLauncherSettingsInfo,
) : ParadoxRootInfo() {
    override val gameType: ParadoxGameType = doGetGameType()
    override val gameRootFile: VirtualFile = doGetGameRootFile()
    
    private fun doGetGameType(): ParadoxGameType {
        return launcherSettingsInfo.gameId.let { ParadoxGameType.resolve(it) } ?: throw IllegalStateException()
    }
    
    private fun doGetGameRootFile(): VirtualFile {
        val dlcPath = launcherSettingsInfo.dlcPath
        val path = launcherSettingsFile.toNioPath().parent.resolve(dlcPath).normalize().toAbsolutePath()
        return VfsUtil.findFile(path, true) ?: throw IllegalStateException()
    }
    
    override val rootPath: Path = rootFile.toNioPath()
    override val gameRootPath: Path = gameRootFile.toNioPath()
    
    override val qualifiedName: String
        get() = "${gameType.description}@${launcherSettingsInfo.rawVersion}"
    override val isAvailable: Boolean
        get() = launcherSettingsFile.isValid
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxRootInfo && rootFile == other.rootFile
    }
    
    override fun hashCode(): Int {
        return rootFile.hashCode()
    }
}

class ParadoxLauncherSettingsInfo(
    val gameId: String,
    val version: String,
    val rawVersion: String,
    val gameDataPath: String = "%USER_DOCUMENTS%/Paradox Interactive/${ParadoxGameType.resolve(gameId)!!}",
    val dlcPath: String = "",
    val exePath: String,
    val exeArgs: List<String>
)

class ParadoxModRootInfo(
    override val rootFile: VirtualFile,
    val descriptorFile: VirtualFile,
) : ParadoxRootInfo() {
    val descriptorInfo: ParadoxModDescriptorInfo get() = ParadoxCoreHandler.getDescriptorInfo(descriptorFile)
    
    override val gameType: ParadoxGameType get() = doGetGameType()
    override val gameRootFile: VirtualFile get() = rootFile
    
    private fun doGetGameType(): ParadoxGameType {
        val allModSettings = getProfilesSettings()
        val modDirectory = rootFile.path
        return allModSettings.modDescriptorSettings.get(modDirectory)?.gameType
            ?: getSettings().defaultGameType
    }
    
    override val rootPath: Path = rootFile.toNioPath()
    override val gameRootPath: Path = rootPath
    
    override val qualifiedName: String
        get() = buildString {
            append(gameType.description).append(" Mod: ")
            append(descriptorInfo.name)
            if(descriptorInfo.version != null) append("@").append(descriptorInfo.version)
        }
    override val isAvailable: Boolean
        get() = descriptorFile.isValid
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxRootInfo && rootFile == other.rootFile
    }
    
    override fun hashCode(): Int {
        return rootFile.hashCode()
    }
}

class ParadoxModDescriptorInfo(
    val name: String,
    val version: String? = null,
    val picture: String? = null,
    val tags: Set<String>? = null,
    val supportedVersion: String? = null,
    val remoteFileId: String? = null,
    val path: String? = null
)