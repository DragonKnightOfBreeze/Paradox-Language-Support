package icu.windea.pls.integrations.lints

import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.toPsiDirectory
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.integrations.PlsIntegrationsBundle
import icu.windea.pls.integrations.lints.providers.TigerLintToolProvider
import icu.windea.pls.integrations.settings.PlsIntegrationsSettings
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.util.concurrent.ConcurrentHashMap

@Service
class TigerLintToolService : Disposable {
    // 检查结果的缓存键
    private val cachedTigerLintResultKey = createKey<CachedValue<TigerLintResult>>("cached.tiger.lintResult")
    // 追踪相关配置（包括可执行文件路径和 .conf 配置文件）的更改
    private val modificationTrackers = ConcurrentHashMap<ParadoxGameType, SimpleModificationTracker>()
    // 根目录级别的检查结果的同步锁
    private val lintResultLocks = ConcurrentHashMap<VirtualFile, Any>()

    fun isEnabled(): Boolean {
        return PlsIntegrationsSettings.getInstance().state.lint.enableTiger
    }

    /** @see icu.windea.pls.integrations.lints.providers.TigerLintToolProvider */
    fun findTool(gameType: ParadoxGameType): TigerLintToolProvider? {
        if (!isEnabled()) return null
        return LintToolProvider.EP_NAME.extensionList.findIsInstance<TigerLintToolProvider> { it.isAvailable(gameType) }
    }

    fun getModificationTracker(gameType: ParadoxGameType): SimpleModificationTracker {
        return modificationTrackers.computeIfAbsent(gameType) { SimpleModificationTracker() }
    }

    fun getLintResultLock(file: VirtualFile): Any {
        return lintResultLocks.computeIfAbsent(file) { Any() }
    }

    fun checkAvailableFor(file: PsiFile): Boolean {
        if (!isEnabled()) return false
        if (file.language !is ParadoxLanguage) return false
        val fileInfo = selectFile(file)?.fileInfo ?: return false
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.Mod) return false // 目前的实现：仅适用于模组目录（中的文件）
        val settings = PlsProfilesSettings.getInstance().getGameOrModSettings(rootInfo)
        if (settings?.options?.disableTiger == true) return false // 检查是否在游戏或模组设置中禁用
        val gameType = rootInfo.gameType
        return findTool(gameType) != null
    }

    fun getTigerLintResultForFile(file: PsiFile): TigerLintResult? {
        // Tiger 执行于根目录级别，而这里执行于单个文件级别，对于缓存需要做特别的处理，从而优化性能

        if (!isEnabled()) return null
        return resolveTigerLintResultForFileFromCache(file)
    }

    private fun resolveTigerLintResultForFileFromCache(file: PsiFile): TigerLintResult? {
        // 当当前文件发生变化时，或者相关配置发生变化时，刷新缓存

        val gameType = selectGameType(file) ?: return null
        return CachedValuesManager.getCachedValue(file, cachedTigerLintResultKey) {
            ProgressManager.checkCanceled()
            val value = resolveTigerLintResultForFile(file)
            val trackers = buildList {
                this += file
                this += getModificationTracker(gameType)
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun resolveTigerLintResultForFile(file: PsiFile): TigerLintResult? {
        val fileInfo = selectFile(file)?.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        val rootFile = rootInfo.rootFile ?: return null
        val rootDirectory = rootFile.toPsiDirectory(file.project) ?: return null
        ProgressManager.checkCanceled()
        val lock = getLintResultLock(rootDirectory.virtualFile)
        val allResult = synchronized(lock) { // 这里需要加锁（不要直接对 `VirtualFile` 加锁）
            getTigerLintResultForRootDirectoryFromCache(rootDirectory)
        } ?: return null
        val result = allResult.fromPath(fileInfo.path.path)
        return result
    }

    @Suppress("unused")
    fun getTigerLintResultForRootDirectory(rootDirectory: PsiDirectory): TigerLintResult? {
        if (!isEnabled()) return null
        ProgressManager.checkCanceled()
        val lock = getLintResultLock(rootDirectory.virtualFile)
        return synchronized(lock) { // 这里需要加锁（不要直接对 `VirtualFile` 加锁）
            getTigerLintResultForRootDirectoryFromCache(rootDirectory)
        }
    }

    private fun getTigerLintResultForRootDirectoryFromCache(rootDirectory: PsiDirectory): TigerLintResult? {
        // 当当前文件（即根目录）发生变化时，或者任意脚本或本地化文件发生变化时（这个条件已经足够），或者相关配置发生变化时，刷新缓存

        val gameType = selectGameType(rootDirectory) ?: return null
        return CachedValuesManager.getCachedValue(rootDirectory, cachedTigerLintResultKey) {
            ProgressManager.checkCanceled()
            val value = resolveTigerLintResultForRootDirectory(rootDirectory)
            val trackers = buildList {
                this += rootDirectory
                this += getModificationTracker(gameType)
                // 如果执行 Tiger 检查工具失败，当任意脚本或本地化文件发生变化时，不会刷新缓存
                if (value == null || value.error != null) {
                    this += ParadoxModificationTrackers.ScriptFile
                    this += ParadoxModificationTrackers.LocalisationFile
                }
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun resolveTigerLintResultForRootDirectory(rootDirectory: PsiDirectory): TigerLintResult? {
        val gameType = selectGameType(rootDirectory) ?: return null
        val tool = findTool(gameType) ?: return null
        val result = tool.validateRootDirectory(rootDirectory.virtualFile)
        if (result != null && result.error != null) {
            notifyWarningNotification(rootDirectory, tool, result.error)
        }
        return result
    }

    private fun notifyWarningNotification(rootDirectory: PsiDirectory, tool: TigerLintToolProvider, e: Throwable) {
        val fileUrl = rootDirectory.virtualFile.presentableUrl
        val title = PlsIntegrationsBundle.message("lint.tiger.notification.warning.title", tool.name)
        val content = e.message?.let { message -> PlsIntegrationsBundle.message("lint.tiger.notification.warning.content", fileUrl, message) }
            ?: PlsIntegrationsBundle.message("lint.tiger.notification.warning.content1", fileUrl)
        PlsFacade.createNotification(NotificationType.WARNING, title, content).notify(rootDirectory.project)
    }

    override fun dispose() {
        modificationTrackers.clear()
        lintResultLocks.clear()
    }

    companion object {
        @JvmStatic
        fun getInstance(): TigerLintToolService = service()
    }
}
