package icu.windea.pls.lang.analysis

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.collect.ImmutableMap
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.data.JsonModuleFactory
import icu.windea.pls.core.data.JsonService
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.analysis.ParadoxFallbackGameTypeMetadata
import icu.windea.pls.model.analysis.ParadoxGameTypeMetadata
import icu.windea.pls.model.analysis.ParadoxJsonBasedGameTypeMetadata
import icu.windea.pls.model.forParadoxGameTypeById

@Suppress("unused")
object ParadoxGameTypeManager {
    // should be declared on the top
    private val metadataMap: Map<ParadoxGameType, ParadoxGameTypeMetadata> = createMetadataMap()

    private val gameTypesUseMetadataJson: List<ParadoxGameType> = listOf(ParadoxGameType.Vic3, ParadoxGameType.Eu5)
    private val gameTypesUseDescriptorMod: List<ParadoxGameType> = buildList {
        addAll(ParadoxGameType.getAll())
        removeAll(gameTypesUseMetadataJson)
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

    fun getMetadata(gameType: ParadoxGameType): ParadoxGameTypeMetadata {
        return metadataMap.getValue(gameType)
    }

    private fun createMetadataMap(): Map<ParadoxGameType, ParadoxGameTypeMetadata> {
        val mapper = JsonService.json5Mapper.copy().apply { registerModule(JsonModuleFactory.forParadoxGameTypeById()) }
        val url = "/data/game_type_metadata_list.json5".toClasspathUrl()
        val list = url.openStream().use { mapper.readValue<List<ParadoxJsonBasedGameTypeMetadata>>(it) }
        val map = list.associateBy { it.gameType }
        val builder = ImmutableMap.builder<ParadoxGameType, ParadoxGameTypeMetadata>()
        val gameTypes = ParadoxGameType.getAll(withCore = true)
        for (gameType in gameTypes) {
            val metadata = map.getOrElse(gameType) { ParadoxFallbackGameTypeMetadata(gameType) }
            builder.put(gameType, metadata)
        }
        return builder.build()
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
}
