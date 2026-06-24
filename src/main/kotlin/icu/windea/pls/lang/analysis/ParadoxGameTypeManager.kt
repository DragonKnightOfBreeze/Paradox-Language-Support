package icu.windea.pls.lang.analysis

import icu.windea.pls.PlsBundle
import icu.windea.pls.base.data.ChronicleJsonService
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.analysis.ParadoxGameTypeMetadata
import java.nio.file.Path

object ParadoxGameTypeManager {
    private val metadataMap: Map<ParadoxGameType, ParadoxGameTypeMetadata> = createMetadataMap()

    private fun createMetadataMap(): Map<ParadoxGameType, ParadoxGameTypeMetadata> {
        val jsonData = ChronicleJsonService.gameTypeMetadataList
        val map = jsonData.associateBy { it.gameType }
        val gameTypes = ParadoxGameType.getAll()
        return buildMap {
            for (gameType in gameTypes) {
                val json = map[gameType]
                val metadata = json?.run {
                    ParadoxGameTypeMetadata(gameType, gameMainEntries, gameExtraEntries, modMainEntries, modExtraEntries, executablePaths)
                } ?: ParadoxGameTypeMetadata(gameType)
                put(gameType, metadata)
            }
        }.optimized()
    }

    fun getMetadata(gameType: ParadoxGameType): ParadoxGameTypeMetadata {
        return metadataMap.getValue(gameType)
    }

    fun getGameType(rootInfo: ParadoxRootInfo): ParadoxGameType {
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> rootInfo.metadata.gameType
            is ParadoxRootInfo.Mod -> rootInfo.metadata.gameTypeInfo?.gameType
                ?: PlsProfilesSettings.getInstance().state.modDescriptorSettings.get(rootInfo.rootFile.path)?.gameType
                ?: ParadoxGameType.getDefault()
            else -> rootInfo.gameType
        }
    }

    fun getGameVersion(rootInfo: ParadoxRootInfo): String? {
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> rootInfo.metadata.version
            is ParadoxRootInfo.Mod -> PlsProfilesSettings.getInstance().state.modSettings.get(rootInfo.rootFile.path)?.gameVersion
            else -> rootInfo.gameVersion
        }
    }

    fun getGameQualifiedName(gameType: ParadoxGameType, version: String?): String {
        return buildString {
            append(gameType.title)
            if (version.isNotNullOrEmpty()) {
                append("@").append(version)
            }
        }
    }

    fun getModQualifiedName(gameType: ParadoxGameType, name: String?, version: String?): String {
        return buildString {
            append(gameType.title).append(" Mod: ")
            append(name?.orNull() ?: PlsBundle.message("root.name.unnamed"))
            version?.orNull()?.let { version -> append("@").append(version) }
        }
    }

    fun processGamePath(gameType: ParadoxGameType, rootPath: Path, relPath: String, processor: (path: Path, entryPath: Path) -> Boolean): Boolean {
        val entries = gameType.metadata.gameEntries
        return if (entries.isEmpty()) {
            val r = rootPath.resolve(relPath)
            processor(r, rootPath)
        } else {
            entries.process { entry ->
                val entryPath = rootPath.resolve(entry)
                val r = entryPath.resolve(relPath)
                processor(r, entryPath)
            }
        }
    }

    @Suppress("unused")
    fun processModPath(gameType: ParadoxGameType, rootPath: Path, relPath: String, processor: (path: Path, entryPath: Path) -> Boolean): Boolean {
        val entries = gameType.metadata.modEntries
        return if (entries.isEmpty()) {
            val r = rootPath.resolve(relPath)
            processor(r, rootPath)
        } else {
            entries.process { entry ->
                val entryPath = rootPath.resolve(entry)
                val r = entryPath.resolve(relPath)
                processor(r, entryPath)
            }
        }
    }
}
