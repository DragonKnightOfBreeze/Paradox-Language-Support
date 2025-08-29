package icu.windea.pls.lang.actions

import com.intellij.ide.actions.CreateDirectoryCompletionContributor
import com.intellij.ide.actions.CreateDirectoryCompletionContributor.Variant
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiDirectory
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.ParadoxIndexKeys

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

    //基于已有的包含脚本文件、本地化文件或者DDS/PNG/TGA文件的目录

    override fun getVariants(directory: PsiDirectory): Collection<Variant> {
        if (DumbService.isDumb(directory.project)) return emptySet()

        val fileInfo = directory.fileInfo ?: return emptySet()
        val path = fileInfo.path.path
        val gameType = fileInfo.rootInfo.gameType
        val pathPrefix = if (path.isEmpty()) "" else "$path/"
        val result = sortedSetOf<String>()
        if (path.isEmpty()) result.addAll(defaultVariants)
        val project = directory.project
        val scope = GlobalSearchScope.allScope(project)
        ProgressManager.checkCanceled()
        val indexId = ParadoxIndexKeys.FilePath
        FileBasedIndex.getInstance().processAllKeys(indexId, p@{ key ->
            FileBasedIndex.getInstance().processValues(indexId, key, null, pp@{ _, info ->
                if (info.gameType != gameType) return@pp true
                if (!info.included) return@pp true
                val p = info.directory.removePrefixOrNull(pathPrefix)
                if (p.isNotNullOrEmpty()) result.add(p)
                true
            }, scope)
            true
        }, project)
        return result.map { it.toVariant() }
    }

    private fun String.toVariant() = Variant(this, null)
}
