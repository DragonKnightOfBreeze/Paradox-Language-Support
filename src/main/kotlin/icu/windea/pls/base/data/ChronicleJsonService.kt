package icu.windea.pls.base.data

import com.fasterxml.jackson.module.kotlin.readValue
import icu.windea.pls.core.data.JsonService
import icu.windea.pls.core.toClasspathUrl

object ChronicleJsonService {
    private val mapper = JsonService.json5Mapper.copy().apply { registerAllModules() }

    private inline fun <reified T> getJsonDataFromClasspath(classpath: String): T {
        val url = classpath.toClasspathUrl()
        return url.openStream().use { mapper.readValue<T>(it) }
    }

    val gameTypeMetadataList: List<ParadoxGameTypeMetadataJson> by lazy { getJsonDataFromClasspath("/data/game_type_metadata_list.json5") }

    val configGroupDataList: List<CwtConfigGroupDataJson> by lazy { getJsonDataFromClasspath("/data/config_group_data_list.json5") }
}
