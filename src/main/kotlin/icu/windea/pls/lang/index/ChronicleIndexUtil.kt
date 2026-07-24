package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.fileInfo

object ChronicleIndexUtil {
    const val nameKeyPrefix = "name:"
    const val typeKeyPrefix = "type:"
    const val idKeyPrefix = "id:"

    fun createLazyKey() = "__lazy__"
    fun createAllKey() = "__all__"
    fun createNameKey(name: String) = "$nameKeyPrefix$name"
    fun createTypeKey(type: String) = "$typeKeyPrefix$type"
    fun createNameTypeKey(name: String, type: String) = "$idKeyPrefix$name\u0000$type"

    val excludedDirectoryNames = listOf(
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

    fun isExcludedDirectory(file: VirtualFile): Boolean {
        if (!file.isDirectory) return false
        if (file.fileInfo == null) return true
        if (isExcludedDirectoryByName(file)) return true
        var current = file
        while (true) {
            current = current.parent ?: break
            if (current.fileInfo == null) break
            if (isExcludedDirectoryByName(current)) return true
        }
        return false
    }

    fun isExcludedDirectoryByName(file: VirtualFile): Boolean {
        val name = file.name
        if (name.startsWith('.')) return true // 排除隐藏目录
        if (name in excludedDirectoryNames) return true // 排除一些特定的目录
        return false
    }
}
