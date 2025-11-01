package icu.windea.pls.model

/**
 * 模组描述符信息。
 */
data class ParadoxModDescriptorInfo(
    val name: String,
    val version: String? = null,
    val picture: String? = null,
    val tags: Set<String> = emptySet(),
    val supportedVersion: String? = null,
    val remoteFileId: String? = null,
    val path: String? = null
)
