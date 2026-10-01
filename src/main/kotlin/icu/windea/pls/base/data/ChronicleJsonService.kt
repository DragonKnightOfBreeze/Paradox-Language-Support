package icu.windea.pls.base.data

import icu.windea.pls.core.data.JsonService
import icu.windea.pls.core.toClasspathUrl
import kotlinx.serialization.json.decodeFromStream

object ChronicleJsonService {
    private inline fun <reified T> getJsonDataFromClasspath(classpath: String): T {
        val url = classpath.toClasspathUrl()
        val inputStream = url.openStream()
        return inputStream.use { JsonService.json5.decodeFromStream(it) }
    }

    val gameTypeMetadataList: List<ParadoxGameTypeMetadataJson> by lazy { getJsonDataFromClasspath("/data/game_type_metadata_list.json5") }

    val configGroupDataList: List<CwtConfigGroupDataJson> by lazy { getJsonDataFromClasspath("/data/config_group_data_list.json5") }
}
