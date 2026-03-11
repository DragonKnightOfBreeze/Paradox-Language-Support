package icu.windea.pls.lang.actions

import com.intellij.ide.actions.CreateDirectoryCompletionContributor
import com.intellij.ide.actions.CreateDirectoryCompletionContributor.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiDirectory
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.model.ParadoxFileInfo

/**
 * 用于在游戏或模组目录中创建目录时，提示可用项（入口目录中的目录，可以多级）。
 *
 * 基于已有的包含脚本文件、本地化文件、CSV 文件或图片文件的路径，排除隐藏目录以及某些特定目录。
 *
 * NOTE: 普通目录的补全与入口目录的补全是上下文无关的（尽管游戏的实际行为可能确实是上下文有关的）。
 *
 * @see ParadoxEntryCreateDirectoryCompletionContributor
 */
class ParadoxCreateDirectoryCompletionContributor : CreateDirectoryCompletionContributor {
    private val defaultVariants = setOf(
        "common",
        "events",
        "gfx",
        "interface",
        "localisation",
    )

    override fun getDescription() = PlsBundle.message("create.directory.completion.description")

    override fun getVariants(directory: PsiDirectory): Collection<Variant> {
        val fileInfo = directory.fileInfo ?: return emptySet()
        if (!fileInfo.inMainOrExtraEntry) return emptySet() // 必须位于合法的入口目录中
        val result = sortedSetOf<String>()
        processFromDefault(result, fileInfo)
        processFromIndex(result, fileInfo, directory)
        return result.map { Variant(it, null) }
    }

    private fun processFromDefault(result: MutableSet<String>, fileInfo: ParadoxFileInfo) {
        val path = fileInfo.path.path
        if (path.isNotEmpty()) return
        val pathPrefix = if (path.isEmpty()) "" else "$path/"
        for (s in defaultVariants) {
            val p = s.removePrefixOrNull(pathPrefix)
            if (p.isNotNullOrEmpty()) result.add(p)
        }
    }

    private fun processFromIndex(result: MutableSet<String>, fileInfo: ParadoxFileInfo, directory: PsiDirectory) {
        ProgressManager.checkCanceled()
        if (DumbService.isDumb(directory.project)) return // skip for dumb mode
        val path = fileInfo.path.path
        val pathPrefix = if (path.isEmpty()) "" else "$path/"
        val gameType = fileInfo.rootInfo.gameType
        val project = directory.project
        val gameTypePrefix = "${gameType.id}:"
        val allKeys = FileBasedIndex.getInstance().getAllKeys(PlsIndexKeys.IncludedDirectory, project)
        for (key in allKeys) {
            ProgressManager.checkCanceled()
            if (!key.startsWith(gameTypePrefix)) continue
            val dir = key.removePrefix(gameTypePrefix)
            val p = dir.removePrefixOrNull(pathPrefix)
            if (p.isNotNullOrEmpty()) result.add(p)
        }
    }
}
