package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.util.createKey
import icu.windea.pls.lang.fileInfo

object PlsIndexUtil {
    const val nameKeyPrefix = "name:"
    const val typeKeyPrefix = "type:"

    fun createLazyKey() = "__lazy__"
    fun createAllKey() = "__all__"
    fun createNameKey(name: String) = "$nameKeyPrefix$name"
    fun createTypeKey(type: String) = "$typeKeyPrefix$type"
    fun createNameTypeKey(name: String, type: String) = "id:$name\u0000$type"

    private val excludedDirectoryNames = listOf(
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

    fun isIncludedDirectory(file: VirtualFile): Boolean {
        if (!file.isDirectory) return false
        if (file.fileInfo == null) return false
        val parent = file.parent
        if (parent != null && parent.fileInfo != null && !isIncludedDirectory(parent)) return false
        val fileName = file.name
        if (fileName.startsWith('.')) return false // 排除隐藏目录
        if (fileName in excludedDirectoryNames) return false // 排除一些特定的目录
        return true
    }

    val indexInfoMarkerKey = createKey<Boolean>("paradox.index.info.marker")
}
