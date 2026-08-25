package icu.windea.pls.model.analysis

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ParadoxMetadataJsonInfo(
    val name: String,
    val id: String,
    val version: String? = null,
    val gameId: String? = null,
    val picture: String? = null,
    val supportedGameVersion: String? = null,
    val shortDescription: String? = null,
    val tags: Set<String> = emptySet(),
    val relationships: Set<Relationship> = emptySet(),
    val gameCustomData: Map<String, Any?> = emptyMap()
) : ParadoxRootMetadataInfo {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class Relationship(
        val relType: String = "dependency",
        val id: String,
        val displayName: String,
        val resourceType: String = "mod",
        val version: String? = null
    )
}
