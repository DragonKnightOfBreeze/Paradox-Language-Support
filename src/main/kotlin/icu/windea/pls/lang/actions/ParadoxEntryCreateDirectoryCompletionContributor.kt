package icu.windea.pls.lang.actions

import com.intellij.ide.actions.CreateDirectoryCompletionContributor
import com.intellij.ide.actions.CreateDirectoryCompletionContributor.*
import com.intellij.psi.PsiDirectory
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxFileInfo

/**
 * 用于在游戏或模组目录中创建目录时，提示可用的入口目录。
 *
 * 基于一组预定义的入口名。
 *
 * NOTE 2.1.5 入口目录的补全与普通目录的补全目前是上下文无关的（尽管游戏的实际行为可能确实是上下文有关的）。
 */
class ParadoxEntryCreateDirectoryCompletionContributor : CreateDirectoryCompletionContributor {
    override fun getDescription() = PlsBundle.message("create.directory.completion.entry.description")

    override fun getVariants(directory: PsiDirectory): Collection<Variant> {
        val fileInfo = directory.fileInfo ?: return emptySet()
        if (fileInfo.inMainOrExtraEntry) return emptySet() // 必须不位于合法的入口目录中（同时又位于根目录中）
        val result = sortedSetOf<String>()
        processFromPredefined(result, fileInfo)
        return result.map { Variant(it, null, PlsIcons.General.EntryDirectory) }
    }

    private fun processFromPredefined(result: MutableSet<String>, fileInfo: ParadoxFileInfo) {
        val path = fileInfo.path.path
        val pathPrefix = if (path.isEmpty()) "" else "$path/"
        val rootInfo = fileInfo.rootInfo
        val entries = rootInfo.mainEntries + rootInfo.extraEntries
        for (s in entries) {
            if (s.isEmpty()) continue
            if (s.contains('*')) continue // 跳过含通配符的入口
            val p = s.removePrefixOrNull(pathPrefix)
            if (p.isNotNullOrEmpty()) result.add(p)
        }
    }
}
