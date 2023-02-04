package icu.windea.pls.config.core.config

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import java.nio.file.*

/**
 * @property rootFile 游戏或模组的根目录。
 * @property gameRootFile 游戏文件的根目录。可能等同于rootFile，也可能是其game子目录。
 */
sealed interface ParadoxRootInfo {
    val rootFile: VirtualFile
    val gameRootFile: VirtualFile
    val rootType: ParadoxRootType
    val gameType: ParadoxGameType
    val isAvailable: Boolean
    
    val rootPath: Path
    val gameRootPath: Path
    
    companion object {
        val values = mutableSetOf<ParadoxRootInfo>()
    }
}

class ParadoxGameRootInfo(
    override val rootFile: VirtualFile,
    val launcherSettingsFile: VirtualFile,
    override val rootType: ParadoxRootType,
    val launcherSettingsInfo: ParadoxLauncherSettingsInfo,
) : ParadoxRootInfo {
	override val gameType: ParadoxGameType = launcherSettingsInfo.gameId.let { ParadoxGameType.resolve(it) } ?: throw IllegalStateException()
    override val gameRootFile: VirtualFile 
		get() {
            if(rootType != ParadoxRootType.Game) return rootFile
			val dlcPath = launcherSettingsInfo.dlcPath
			val path = launcherSettingsFile.toNioPath().resolve(dlcPath)
			return VfsUtil.findFile(path, true) ?: throw IllegalStateException()
		}
    
    override val rootPath: Path = rootFile.toNioPath()
    override val gameRootPath: Path = gameRootFile.toNioPath()
    
    override val isAvailable get() = launcherSettingsFile.isValid
    
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
    val gameDataPath: String, // %USER_DOCUMENTS%/Paradox Interactive/${gameDisplayName}
    val dlcPath: String,
    val exePath: String,
    val exeArgs: List<String>
) {
    companion object Resolver {
        fun resolve(file: VirtualFile): ParadoxLauncherSettingsInfo? {
            try {
                return jsonMapper.readValue(file.inputStream)
            } catch(e: Exception) {
                return null
            }
        }
    }
}

class ParadoxModRootInfo(
    override val rootFile: VirtualFile,
    val descriptorFile: VirtualFile,
    val markerFile: VirtualFile?,
    override val rootType: ParadoxRootType,
    val descriptorInfo: ParadoxDescriptorInfo
) : ParadoxRootInfo {
	override val gameType: ParadoxGameType = markerFile?.let { ParadoxGameType.resolve(it) } ?: getSettings().defaultGameType
    override val gameRootFile: VirtualFile get() = rootFile
    
    override val rootPath: Path = rootFile.toNioPath()
    override val gameRootPath: Path = rootPath
    
    override val isAvailable get() = descriptorFile.isValid && (markerFile?.isValid != false)
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxRootInfo && rootFile == other.rootFile
    }
    
    override fun hashCode(): Int {
        return rootFile.hashCode()
    }
}

class ParadoxDescriptorInfo(
    val name: String,
    val version: String? = null,
    val picture: String? = null,
    val tags: Set<String>? = null,
    val supportedVersion: String? = null,
    val remoteFileId: String? = null,
    val path: String? = null,
    val isModDescriptor: Boolean = true
) {
    //see: descriptor.cwt
    
    companion object Resolver {
        fun resolve(file: VirtualFile): ParadoxDescriptorInfo? {
            val psiFile = file.toPsiFile<ParadoxScriptFile>(getDefaultProject()) ?: return null
            val rootBlock = psiFile.findChild<ParadoxScriptRootBlock>() ?: return null
            var name: String? = null
            var version: String? = null
            var picture: String? = null
            var tags: Set<String>? = null
            var supportedVersion: String? = null
            var remoteFileId: String? = null
            var path: String? = null
            rootBlock.processProperty(includeConditional = false) { property ->
                when(property.name) {
                    "name" -> name = property.findValue<ParadoxScriptString>()?.stringValue
                    "version" -> version = property.findValue<ParadoxScriptString>()?.stringValue
                    "picture" -> picture = property.findValue<ParadoxScriptString>()?.stringValue
                    "tags" -> tags = property.findBlockValues<ParadoxScriptString>().mapTo(mutableSetOf()) { it.stringValue }
                    "supported_version" -> supportedVersion = property.findValue<ParadoxScriptString>()?.stringValue
                    "remote_file_id" -> remoteFileId = property.findValue<ParadoxScriptString>()?.stringValue
                    "path" -> path = property.findValue<ParadoxScriptString>()?.stringValue
                }
                true
            }
            val nameToUse = name ?: file.parent?.name.orAnonymous() //如果没有name属性，则使用根目录名
            return ParadoxDescriptorInfo(nameToUse, version, picture, tags, supportedVersion, remoteFileId, path, isModDescriptor = true)
        }
    }
}