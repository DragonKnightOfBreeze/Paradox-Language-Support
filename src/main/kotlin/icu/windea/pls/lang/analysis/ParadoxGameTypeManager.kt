package icu.windea.pls.lang.analysis

import com.fasterxml.jackson.module.kotlin.readValue
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.data.JsonModuleFactory
import icu.windea.pls.core.data.JsonService
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.analysis.ParadoxDefaultGameTypeMetadata
import icu.windea.pls.model.analysis.ParadoxFallbackGameTypeMetadata
import icu.windea.pls.model.analysis.ParadoxGameTypeMetadata
import icu.windea.pls.model.forParadoxGameTypeById
import java.nio.file.Path

@Suppress("unused")
object ParadoxGameTypeManager {
    private val gameTypesUseMetadataJson: List<ParadoxGameType> = createGameTypesUseMetadataJson()
    private val gameTypesUseDescriptorMod: List<ParadoxGameType> = createGameTypesUseDescriptorMod()
    private val metadataMap: Map<ParadoxGameType, ParadoxGameTypeMetadata> = createMetadataMap()

    private fun createGameTypesUseMetadataJson(): List<ParadoxGameType> {
        return buildList {
            add(ParadoxGameType.Vic3)
            add(ParadoxGameType.Eu5)
        }.optimized()
    }

    private fun createGameTypesUseDescriptorMod(): List<ParadoxGameType> {
        return buildList {
            addAll(ParadoxGameType.getAll())
            removeAll(gameTypesUseMetadataJson)
        }.optimized()
    }

    fun getGameTypesUseDescriptorMod(): List<ParadoxGameType> {
        return gameTypesUseDescriptorMod
    }

    fun getGameTypesUseMetadataJson(): List<ParadoxGameType> {
        return gameTypesUseMetadataJson
    }

    fun useDescriptorMod(gameType: ParadoxGameType): Boolean {
        return gameType in gameTypesUseDescriptorMod
    }

    fun useMetadataJson(gameType: ParadoxGameType): Boolean {
        return gameType in gameTypesUseMetadataJson
    }

    private fun createMetadataMap(): Map<ParadoxGameType, ParadoxGameTypeMetadata> {
        val mapper = JsonService.json5Mapper.copy().apply { registerModule(JsonModuleFactory.forParadoxGameTypeById()) }
        val url = "/data/game_type_metadata_list.json5".toClasspathUrl()
        val list = url.openStream().use { mapper.readValue<List<ParadoxDefaultGameTypeMetadata>>(it) }
        val map = list.associateBy { it.gameType }
        val gameTypes = ParadoxGameType.getAll(withCore = true)
        return buildMap {
            for (gameType in gameTypes) {
                val metadata = map.getOrElse(gameType) { ParadoxFallbackGameTypeMetadata(gameType) }
                put(gameType, metadata)
            }
        }.optimized()
    }

    fun getMetadata(gameType: ParadoxGameType): ParadoxGameTypeMetadata {
        return metadataMap.getValue(gameType)
    }

    fun getGameGameType(rootInfo: ParadoxRootInfo.Game): ParadoxGameType {
        return rootInfo.metadata.gameType
    }

    fun getModGameType(rootInfo: ParadoxRootInfo.Mod): ParadoxGameType {
        return rootInfo.metadata.gameType
            ?: PlsProfilesSettings.getInstance().state.modDescriptorSettings.get(rootInfo.rootFile.path)?.gameType
            ?: ParadoxGameType.getDefault()
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
