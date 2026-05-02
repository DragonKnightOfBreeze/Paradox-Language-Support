package icu.windea.pls.lang.analysis

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.collect.ImmutableMap
import icu.windea.pls.core.data.JsonModuleFactory
import icu.windea.pls.core.data.JsonService
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.analysis.ParadoxFallbackGameTypeMetadata
import icu.windea.pls.model.analysis.ParadoxGameTypeMetadata
import icu.windea.pls.model.analysis.ParadoxJsonBasedGameTypeMetadata
import icu.windea.pls.model.forParadoxGameTypeById

@Suppress("unused")
object ParadoxGameTypeManager {
    // should be declared on the top
    private val gameTypeMetadataMap: Map<ParadoxGameType, ParadoxGameTypeMetadata> = createGameTypeMetadataMap()

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

    fun getGameTypeMetadata(gameType: ParadoxGameType): ParadoxGameTypeMetadata {
        return gameTypeMetadataMap.getValue(gameType)
    }

    private fun createGameTypeMetadataMap(): Map<ParadoxGameType, ParadoxGameTypeMetadata> {
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
}
