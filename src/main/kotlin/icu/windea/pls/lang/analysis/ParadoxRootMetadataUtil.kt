package icu.windea.pls.lang.analysis

import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.core.data.JsonService
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.ep.util.data.ParadoxModDescriptorData
import icu.windea.pls.lang.util.data.ParadoxScriptDataResolver
import icu.windea.pls.model.analysis.ParadoxDescriptorModInfo
import icu.windea.pls.model.analysis.ParadoxLauncherSettingsJsonInfo
import icu.windea.pls.model.analysis.ParadoxMetadataJsonInfo
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText

object ParadoxRootMetadataUtil {
    val logger = thisLogger()

    // region launcher-settings.json

    // - launcher-settings.json
    // - launcher/launcher-settings.json

    fun getLauncherSettingsJsonPath(rootPath: Path): Path? {
        try {
            if (rootPath.name == "launcher") return null
            rootPath.resolve("launcher-settings.json").takeIf { it.isRegularFile() }?.let { return it }
            rootPath.resolve("launcher/launcher-settings.json").takeIf { it.isRegularFile() }?.let { return it }
            return null
        } catch (e: Exception) {
            logger.warn("Cannot resolve root metadata path from root path: ${rootPath}", e)
            throw e
        }
    }

    fun getLauncherSettingsJsonInfo(path: Path): ParadoxLauncherSettingsJsonInfo? {
        try {
            return JsonService.mapper.readValue(path.toFile())
        } catch (e: Exception) {
            logger.warn("Cannot resolve root metadata info from path: ${path}", e)
            return null
        }
    }

    // endregion

    // region metadata.json

    // - .metadata/metadata.json

    fun getMetadataJsonPath(rootPath: Path): Path? {
        try {
            rootPath.resolve(".metadata/metadata.json").takeIf { it.isRegularFile() }?.let { return it }
            return null
        } catch (e: Exception) {
            logger.warn("Cannot resolve root metadata path from root path: ${rootPath}", e)
            throw e
        }
    }

    fun getMetadataJsonInfo(path: Path): ParadoxMetadataJsonInfo? {
        try {
            return JsonService.mapper.readValue(path.toFile())
        } catch (e: Exception) {
            logger.warn("Cannot resolve root metadata info from path: ${path}", e)
            return null
        }
    }

    // endregion

    // region descriptor.mod

    // - descriptor.mod

    fun getDescriptorModPath(rootPath: Path): Path? {
        try {
            rootPath.resolve("descriptor.mod").takeIf { it.isRegularFile() }?.let { return it }
            return null
        } catch (e: Exception) {
            logger.warn("Cannot resolve root metadata path from root path: ${rootPath}", e)
            throw e
        }
    }

    fun getDescriptorModInfo(path: Path): ParadoxDescriptorModInfo? {
        try {
            val text = path.readText().trim().orNull() ?: return null
            return runSmartReadAction { resolveDescriptorModInfo(path, text) }
        } catch (e: Exception) {
            logger.warn("Cannot resolve root metadata info from path: ${path}", e)
            return null
        }
    }

    private fun resolveDescriptorModInfo(path: Path, text: String): ParadoxDescriptorModInfo? {
        val project = getDefaultProject()
        val file = ParadoxScriptElementFactory.createDummyFile(project, text)
        val scriptData = ParadoxScriptDataResolver.DEFAULT.resolveFile(file) ?: return null
        val data = ParadoxModDescriptorData(scriptData)
        val name = data.name?.orNull() ?: path.parent?.name?.orNull() ?: "" // 作为回退，使用模组目录名作为模组名
        val version = data.version?.orNull()
        val picture = data.picture?.orNull()
        val tags = data.tags.optimized()
        val supportedVersion = data.supportedVersion?.orNull()
        val remoteFileId = data.remoteFileId?.orNull()
        val path = data.path?.orNull()
        return ParadoxDescriptorModInfo(name, version, picture, tags, supportedVersion, remoteFileId, path)
    }

    // endregion
}
