package icu.windea.pls.lang.actions

import com.intellij.ide.actions.CreateDirectoryCompletionContributor
import com.intellij.ide.actions.CreateDirectoryCompletionContributor.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiDirectory
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于在游戏或模组目录中创建目录时，提示可用项。
 */
class ParadoxCreateDirectoryCompletionContributor : CreateDirectoryCompletionContributor {
    val defaultVariants = setOf(
        "common",
        "events",
        "gfx",
        "interface",
        "localisation",
    )

    override fun getDescription(): String {
        return PlsBundle.message("create.directory.completion.description")
    }

    // 基于已有的包含脚本文件、本地化文件或者DDS/PNG/TGA文件的目录

    override fun getVariants(directory: PsiDirectory): Collection<Variant> {
        val fileInfo = directory.fileInfo ?: return emptySet()
        val path = fileInfo.path.path
        val gameType = fileInfo.rootInfo.gameType
        val pathPrefix = if (path.isEmpty()) "" else "$path/"
        val result = sortedSetOf<String>()
        processFromDefault(result, path)
        processFromIndex(result, directory, gameType, pathPrefix)
        return result.map { it.toVariant() }
    }

    private fun processFromDefault(result: MutableSet<String>, path: String) {
        if (path.isNotEmpty()) return
        result.addAll(defaultVariants)
    }

    private fun processFromIndex(result: MutableSet<String>, directory: PsiDirectory, gameType: ParadoxGameType, pathPrefix: String) {
        if (DumbService.isDumb(directory.project)) return
        val project = directory.project
        val scope = GlobalSearchScope.allScope(project)
        ProgressManager.checkCanceled()
        val indexId = PlsIndexKeys.FilePath
        FileBasedIndex.getInstance().processAllKeys(indexId, p@{ key ->
            FileBasedIndex.getInstance().processValues(indexId, key, null, pp@{ _, data ->
                if (data.gameType != gameType) return@pp true
                if (!data.included) return@pp true
                val p = data.directory.removePrefixOrNull(pathPrefix)
                if (p.isNotNullOrEmpty()) result.add(p)
                true
            }, scope)
            true
        }, project)
    }

    private fun String.toVariant() = Variant(this, null)
}
