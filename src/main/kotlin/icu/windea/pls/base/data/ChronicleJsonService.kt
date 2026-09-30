package icu.windea.pls.base.data

import icu.windea.pls.core.data.JsonService
import icu.windea.pls.core.data.readJsonText
import icu.windea.pls.core.data.stripJson5Comments
import icu.windea.pls.core.toClasspathUrl

object ChronicleJsonService {
    private inline fun <reified T> getJsonDataFromClasspath(classpath: String): T {
        val url = classpath.toClasspathUrl()
        val text = url.openStream().use { it.readJsonText() }
        return JsonService.json5.decodeFromString(text.stripJson5Comments())
    }

    val gameTypeMetadataList: List<ParadoxGameTypeMetadataJson> by lazy { getJsonDataFromClasspath("/data/game_type_metadata_list.json5") }

    val configGroupDataList: List<CwtConfigGroupDataJson> by lazy { getJsonDataFromClasspath("/data/config_group_data_list.json5") }
}
