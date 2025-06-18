package icu.windea.pls.integrations.lints

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.integrations.lints.tools.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

object PlsTigerLintManager {
    object Keys : KeyRegistry() {
        val cachedTigerLintResult by createKey<CachedValue<PlsTigerLintResult>>(Keys)
    }

    fun findTigerTool(gameType: ParadoxGameType): PlsTigerLintToolProvider? {
        return PlsLintToolProvider.EP_NAME.extensionList.findIsInstance<PlsTigerLintToolProvider> { it.isAvailable(gameType) }
    }

    fun getTigerLintResultForFile(file: VirtualFile, project: Project): PlsTigerLintResult? {
        val gameType = selectGameType(file) ?: return null
        if (findTigerTool(gameType) == null) return null
        return doGetTigerLintResultFromFileFromCache(file, project)
    }

    private fun doGetTigerLintResultFromFileFromCache(file: VirtualFile, project: Project): PlsTigerLintResult? {
        return CachedValuesManager.getManager(project).getCachedValue(file, Keys.cachedTigerLintResult, {
            val value = doGetTigerLintResultFromFile(file)
            value.withDependencyItems(file)
        }, false)
    }

    private fun doGetTigerLintResultFromFile(file: VirtualFile): PlsTigerLintResult? {
        //TODO 2.0.0-dev Tiger执行于根目录级别，而这里执行于单个文件级别，对于缓存需要做特别的处理，从而优化性能

        val fileInfo = selectFile(file)?.fileInfo ?: return null
        val rootFile = fileInfo.rootInfo.rootFile
        val allResult = getTigerLintResultForRootDirectory(rootFile) ?: return null
        val items = allResult.itemGroup[fileInfo.path.path]
        if (items.isNullOrEmpty()) return null
        return PlsTigerLintResult(items)
    }

    fun getTigerLintResultForRootDirectory(file: VirtualFile): PlsTigerLintResult? {
        val rootInfo = file.rootInfo ?: return null
        if (rootInfo !is ParadoxRootInfo.Mod) return null
        val gameType = rootInfo.gameType
        if (findTigerTool(gameType) == null) return null
        return doGetTigerLintResultFromRootDirectory(file) //这里目前不缓存结果……
    }

    private fun doGetTigerLintResultFromRootDirectory(file: VirtualFile): PlsTigerLintResult? {
        val gameType = selectGameType(file) ?: return null
        val tool = findTigerTool(gameType) ?: return null
        return tool.validateRootDirectory(file)
    }
}
