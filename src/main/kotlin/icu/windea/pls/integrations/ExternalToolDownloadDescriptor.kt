package icu.windea.pls.integrations

import icu.windea.pls.core.util.io.ArchiveType

/**
 * 外部工具下载描述。
 *
 * @property name 工具名称。
 * @property url 下载地址。
 * @property fileName 下载文件的名称。
 * @property executableName 可执行文件的名称。
 * @property archiveType 压缩包类型；为 [ArchiveType.None] 时表示直接下载文件。
 * @property sha256 可选的 SHA-256 校验值。
 * @property version 可选的版本标识，用于测试缓存判断。
 */
data class ExternalToolDownloadDescriptor(
    val name: String,
    val url: String,
    val fileName: String,
    val executableName: String,
    val archiveType: ArchiveType = ArchiveType.None,
    val sha256: String? = null,
    val version: String? = null,
)
