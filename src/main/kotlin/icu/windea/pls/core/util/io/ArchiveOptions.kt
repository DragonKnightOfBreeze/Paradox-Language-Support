package icu.windea.pls.core.util.io

import icu.windea.pls.core.toPathOrNull
import java.nio.file.Path

/**
 * 压缩包解压选项。
 *
 * @property sevenZipPath 用于解压不支持的 7z 编码器的外部 7z 可执行文件路径。
 * @property allowExternalSevenZip 是否允许自动查找外部 7z 工具。
 * @property deleteArchiveAfterExtract 解压后是否删除压缩包。
 */
data class ArchiveOptions(
    val sevenZipPath: Path? = System.getProperty("chronicle.external.tools.sevenZipPath")?.trim()?.toPathOrNull(),
    val allowExternalSevenZip: Boolean = true,
    val deleteArchiveAfterExtract: Boolean = true,
) {
    companion object {
        @JvmField val DEFAULT = ArchiveOptions()
    }
}
