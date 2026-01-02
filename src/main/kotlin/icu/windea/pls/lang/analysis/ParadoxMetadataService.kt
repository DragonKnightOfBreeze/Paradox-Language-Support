package icu.windea.pls.lang.analysis

import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.jsonMapper
import icu.windea.pls.ep.util.data.ParadoxModDescriptorData
import icu.windea.pls.lang.util.data.ParadoxScriptDataResolver
import icu.windea.pls.model.metadata.ParadoxDescriptorModInfo
import icu.windea.pls.model.metadata.ParadoxLauncherSettingsJsonInfo
import icu.windea.pls.model.metadata.ParadoxMetadataJsonInfo
import icu.windea.pls.script.psi.ParadoxScriptElementFactory

object ParadoxMetadataService {
    val metadataFileNames = setOf("launcher-settings.json", "descriptor.mod", "metadata.json")

    fun getLauncherSettingsJsonFile(rootFile: VirtualFile): VirtualFile? {
        // - `launcher-settings.json`
        // - `launcher/launcher-settings.json`

        if (rootFile.name == "launcher") return null
        rootFile.findChild("launcher-settings.json")
            ?.takeIf { it.isFile }?.let { return it }
        rootFile.findFileByRelativePath("launcher/launcher-settings.json")
            ?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun resolveLauncherSettingsJsonInfo(file: VirtualFile): ParadoxLauncherSettingsJsonInfo {
        return file.inputStream.use { jsonMapper.readValue(it) }
    }

    fun getDescriptorModFile(rootFile: VirtualFile): VirtualFile? {
        // - `descriptor.mod`

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

    fun getMetadataJsonFile(rootFile: VirtualFile): VirtualFile? {
        // - `.metadata/metadata.json`

        rootFile.findFileByRelativePath(".metadata/metadata.json")?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun resolveMetadataJsonInfo(file: VirtualFile): ParadoxMetadataJsonInfo {
        return file.inputStream.use { jsonMapper.readValue(it) }
    }
}
