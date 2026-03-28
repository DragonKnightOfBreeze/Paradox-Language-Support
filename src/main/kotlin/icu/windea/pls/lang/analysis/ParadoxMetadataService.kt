package icu.windea.pls.lang.analysis

import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.json.JsonFileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.jsonMapper
import icu.windea.pls.ep.util.data.ParadoxModDescriptorData
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.util.data.ParadoxScriptDataResolver
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.metadata.ParadoxDescriptorModInfo
import icu.windea.pls.model.metadata.ParadoxLauncherSettingsJsonInfo
import icu.windea.pls.model.metadata.ParadoxMetadataJsonInfo
import icu.windea.pls.script.psi.ParadoxScriptElementFactory

object ParadoxMetadataService {
    val metadataFileNames = setOf("launcher-settings.json", "descriptor.mod", "metadata.json")

    // region launcher-settings.json

    // - launcher-settings.json
    // - launcher/launcher-settings.json

    fun isLauncherSettingsJsonFile(file: VirtualFile): Boolean {
        if (file.fileType !is JsonFileType) return false
        if (file.name != "launcher-settings.json") return false
        val parent1 = file.parent ?: return false
        if (parent1.rootInfo?.takeIf { it is ParadoxRootInfo.Game } != null) return true
        val parent2 = parent1.parent?.takeIf { it.name == "launcher" } ?: return false
        if (parent2.rootInfo?.takeIf { it is ParadoxRootInfo.Game } != null) return true
        return false
    }

    fun getLauncherSettingsJsonFile(rootFile: VirtualFile): VirtualFile? {
        if (rootFile.name == "launcher") return null
        rootFile.findChild("launcher-settings.json")?.takeIf { it.isFile }?.let { return it }
        rootFile.findFileByRelativePath("launcher/launcher-settings.json")?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun resolveLauncherSettingsJsonInfo(file: VirtualFile): ParadoxLauncherSettingsJsonInfo {
        // 直接解析 JSON

        return file.inputStream.use { jsonMapper.readValue(it) }
    }

    // endregion

    // region metadata.json

    // - .metadata/metadata.json

    fun isMetadataJsonFile(file: VirtualFile): Boolean {
        if (file.fileType !is JsonFileType) return false
        if (file.name != "metadata.json") return false
        val parent1 = file.parent?.takeIf { it.name == ".metadata" } ?: return false
        val parent2 = parent1.parent ?: return false
        if (parent2.rootInfo?.takeIf { it is ParadoxRootInfo.Mod } != null) return true
        return false
    }

    fun getMetadataJsonFile(rootFile: VirtualFile): VirtualFile? {

        rootFile.findFileByRelativePath(".metadata/metadata.json")?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun resolveMetadataJsonInfo(file: VirtualFile): ParadoxMetadataJsonInfo {
        // 直接解析 JSON

        return file.inputStream.use { jsonMapper.readValue(it) }
    }

    // endregion

    // region descriptor.mod

    // - descriptor.mod

    fun isDescriptorModFile(file: VirtualFile): Boolean {
        if (file.name != "descriptor.mod") return false
        val parent = file.parent ?: return false
        if (parent.rootInfo?.takeIf { it is ParadoxRootInfo.Mod } != null) return true
        return false
    }

    fun getDescriptorModFile(rootFile: VirtualFile): VirtualFile? {
        rootFile.findChild("descriptor.mod")?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun resolveDescriptorModInfo(file: VirtualFile): ParadoxDescriptorModInfo {
        // 需要先创建 `dummyFile` 再解析（直接解析的话会导致 `StackOverflowError`）
        // `createDummyFile` -> `ParadoxScriptData` -> `ParadoxModDescriptorData` -> `ParadoxDescriptorModInfo`

        val psiFile = ParadoxScriptElementFactory.createDummyFile(getDefaultProject(), file.inputStream.use { it.reader().readText() })
        val data = ParadoxScriptDataResolver.DEFAULT.resolveFile(psiFile)?.let { ParadoxModDescriptorData(it) }
        val name = data?.name ?: file.parent?.name ?: "" // 作为回退，使用模组目录名作为模组名
        val version = data?.version
        val picture = data?.picture
        val tags = data?.tags.orEmpty()
        val supportedVersion = data?.supportedVersion
        val remoteFileId = data?.remoteFileId
        val path = data?.path
        return ParadoxDescriptorModInfo(name, version, picture, tags, supportedVersion, remoteFileId, path)
    }

    // endregion
}
