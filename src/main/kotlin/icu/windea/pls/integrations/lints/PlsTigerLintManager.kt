package icu.windea.pls.integrations.lints

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
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

    //追踪相关配置（包括可执行文件路径和.conf配置文件）的更改
    val modificationTrackers = mutableMapOf<ParadoxGameType, SimpleModificationTracker>().withDefault { SimpleModificationTracker() }

    fun isEnabled(): Boolean = PlsFacade.getIntegrationsSettings().lint.enableTiger

    fun findTigerTool(gameType: ParadoxGameType): PlsTigerLintToolProvider? {
        if (!isEnabled()) return null
        return PlsLintToolProvider.EP_NAME.extensionList.findIsInstance<PlsTigerLintToolProvider> { it.isAvailable(gameType) }
    }

    fun checkAvailableFor(file: PsiFile): Boolean {
        if (!isEnabled()) return false
        if (file.language !is ParadoxBaseLanguage) return false
        val fileInfo = selectFile(file)?.fileInfo ?: return false
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.Mod) return false //目前的实现：仅适用于模组目录（中的文件）
        val gameType = rootInfo.gameType
        return findTigerTool(gameType) != null
    }

    fun getTigerLintResultForFile(file: PsiFile): PlsTigerLintResult? {
        //Tiger执行于根目录级别，而这里执行于单个文件级别，对于缓存需要做特别的处理，从而优化性能

        if (!isEnabled()) return null
        return doGetTigerLintResultForFileFromCache(file)
    }

    private fun doGetTigerLintResultForFileFromCache(file: PsiFile): PlsTigerLintResult? {
        //当当前文件发生变化时，或者相关配置发生变化时，刷新缓存

        val gameType = selectGameType(file) ?: return null
        return CachedValuesManager.getCachedValue(file, Keys.cachedTigerLintResult) {
            val value = doGetTigerLintResultForFile(file)
            val trackers = mutableListOf(file, modificationTrackers.getValue(gameType))
            value.withDependencyItems(trackers.toTypedArray())
        }
    }

    private fun doGetTigerLintResultForFile(file: PsiFile): PlsTigerLintResult? {
        val fileInfo = selectFile(file)?.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        val rootDirectory = runReadAction { rootInfo.rootFile.toPsiDirectory(file.project) } ?: return null
        val allResult = getTigerLintResultForRootDirectory(rootDirectory) ?: return null
        val result = allResult.fromPath(fileInfo.path.path)
        return result
    }

    fun getTigerLintResultForRootDirectory(rootDirectory: PsiDirectory): PlsTigerLintResult? {
        if (!isEnabled()) return null
        return synchronized(rootDirectory.virtualFile) {
            doGetTigerLintResultForRootDirectoryFromCache(rootDirectory)
        }
    }

    private fun doGetTigerLintResultForRootDirectoryFromCache(rootDirectory: PsiDirectory): PlsTigerLintResult? {
        //当当前文件（即根目录）发生变化时，或者任意脚本或本地化文件发生变化时（这个条件已经足够），或者相关配置发生变化时，刷新缓存

        val gameType = selectGameType(rootDirectory) ?: return null
        return CachedValuesManager.getCachedValue(rootDirectory, Keys.cachedTigerLintResult) {
            val value = doGetTigerLintResultForRootDirectory(rootDirectory)
            val trackers = mutableListOf(rootDirectory, modificationTrackers.getValue(gameType))
            if (value == null || value.error != null) { //如果执行Tiger检查工具失败，当任意脚本或本地化文件发生变化时，不会刷新缓存
                trackers += ParadoxModificationTrackers.FileTracker
            }
            value.withDependencyItems(trackers.toTypedArray())
        }
    }

    private fun doGetTigerLintResultForRootDirectory(rootDirectory: PsiDirectory): PlsTigerLintResult? {
        val gameType = selectGameType(rootDirectory) ?: return null
        val tool = findTigerTool(gameType) ?: return null
        val result = tool.validateRootDirectory(rootDirectory.virtualFile)
        if (result != null && result.error != null) {
            notifyWarningNotification(rootDirectory, tool, result.error)
        }
        return result
    }

    private fun notifyWarningNotification(rootDirectory: PsiDirectory, tool: PlsTigerLintToolProvider, e: Throwable) {
        val fileUrl = rootDirectory.virtualFile.presentableUrl
        val title = PlsBundle.message("lint.tiger.notification.warning.title", tool.name)
        val content = e.message?.let { message -> PlsBundle.message("lint.tiger.notification.warning.content", fileUrl, message) }
            ?: PlsBundle.message("lint.tiger.notification.warning.content1", fileUrl)
        createNotification(title, content, NotificationType.WARNING).notify(rootDirectory.project)
    }
}
