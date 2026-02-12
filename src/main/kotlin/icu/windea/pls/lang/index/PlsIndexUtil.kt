package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.util.createKey
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.constants.PlsConstants

object PlsIndexUtil {
    fun createLazyKey() = "__lazy__"
    fun createAllKey() = "__all__"
    fun createNameKey(type: String) = "name:$type"
    fun createTypeKey(type: String) = "type:$type"
    fun createNameTypeKey(name: String, type: String) = "id:$name\u0000$type"

    private val excludeDirectoriesForFilePathIndex = listOf(
        "_CommonRedist",
        "binaries",
        "clausewitz",
        "crash_reporter",
        "curated_save_games",
        "jomini",
        "pdx_browser",
        "pdx_launcher",
        "pdx_online_assets",
        "previewer_assets",
        "tweakergui_assets",
    )

    fun includeForFilePathIndex(file: VirtualFile): Boolean {
        if (file.fileInfo == null) return false
        val parent = file.parent
        if (parent != null && parent.fileInfo != null && !includeForFilePathIndex(parent)) return false
        val fileName = file.name
        if (fileName.startsWith('.')) return false // 排除隐藏目录或文件
        if (file.isDirectory) {
            if (fileName in excludeDirectoriesForFilePathIndex) return false // 排除一些特定的目录
            return true
        }
        val fileExtension = fileName.substringAfterLast('.')
        if (fileExtension.isEmpty()) return false
        return fileExtension in PlsConstants.scriptFileExtensions
            || fileExtension in PlsConstants.localisationFileExtensions
            || fileExtension in PlsConstants.csvFileExtensions
            || fileExtension in PlsConstants.imageFileExtensions
    }

    val indexInfoMarkerKey = createKey<Boolean>("paradox.index.info.marker")
}
