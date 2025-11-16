package icu.windea.pls.model.metadata

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 模组元数据信息（`.metadata/metadata.json`）。
 */
data class ParadoxMetadataJsonInfo(
    val name: String,
    val id: String,
    val version: String? = null,
    @JsonProperty("game_id")
    val gameId: String? = null,
    val picture: String? = null,
    @JsonProperty("supported_game_version")
    val supportedGameVersion: String? = null,
    @JsonProperty("short_description")
    val shortDescription: String? = null,
    val tags: Set<String> = emptySet(),
    val relationships: Set<Relationship> = emptySet(),
    @JsonProperty("game_custom_data")
    val gameCustomData: Map<String, Any?> = emptyMap()
) : ParadoxMetadataInfo {
    data class Relationship(
        @JsonProperty("rel_type")
        val relType: String = "dependency",
        val id: String,
        @JsonProperty("display_name")
        val displayName: String,
        @JsonProperty("resource_type")
        val resourceType: String = "mod",
        val version: String? = null
    )
}
