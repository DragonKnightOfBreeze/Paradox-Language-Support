package icu.windea.pls.model.analysis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 模组描述符信息（`descriptor.mod`）。
 */
data class ParadoxDescriptorModInfo(
    val name: String,
    val version: String? = null,
    val picture: String? = null,
    val tags: Set<String> = emptySet(),
    val supportedVersion: String? = null,
    val remoteFileId: String? = null,
    val path: String? = null
) : ParadoxRootMetadataInfo

/**
 * 模组元数据信息（`.metadata/metadata.json`）。
 */
@Serializable
data class ParadoxMetadataJsonInfo(
    val name: String,
    val id: String,
    val version: String? = null,
    @SerialName("game_id")
    val gameId: String? = null,
    val picture: String? = null,
    @SerialName("supported_game_version")
    val supportedGameVersion: String? = null,
    @SerialName("short_description")
    val shortDescription: String? = null,
    val tags: Set<String> = emptySet(),
    val relationships: Set<Relationship> = emptySet(),
    @SerialName("game_custom_data")
    val gameCustomData: Map<String, JsonElement> = emptyMap()
) : ParadoxRootMetadataInfo {
    @Serializable
    data class Relationship(
        @SerialName("rel_type")
        val relType: String = "dependency",
        val id: String,
        @SerialName("display_name")
        val displayName: String,
        @SerialName("resource_type")
        val resourceType: String = "mod",
        val version: String? = null
    )
}
