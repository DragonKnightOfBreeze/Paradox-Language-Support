package icu.windea.pls.config.configGroup

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.ep.config.configGroup.CwtFileBasedConfigGroupDataProvider

/**
 * 规则分组文件信息。
 *
 * @see CwtFileBasedConfigGroupDataProvider
 */
data class CwtConfigGroupFileInfo(
    val filePath: String,
    val file: VirtualFile,
    val source: CwtConfigGroupFileSource,
)

/**
 * 规则文件当地来源。
 *
 * @see CwtFileBasedConfigGroupDataProvider
 */
enum class CwtConfigGroupFileSource {
    BuiltIn,
    Remote,
    Local,
}
