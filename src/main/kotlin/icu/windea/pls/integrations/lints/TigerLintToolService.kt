package icu.windea.pls.integrations.lints

import com.intellij.execution.ExecutionException
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportRawProgress
import com.intellij.psi.PsiFile
import com.intellij.util.ExceptionUtil
import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.base.notification.ChronicleNotificationGroups
import icu.windea.pls.base.settings.ChronicleProfilesSettings
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.toUuidString
import icu.windea.pls.core.util.MergedModificationTracker
import icu.windea.pls.core.util.cache.CacheBuilder
import icu.windea.pls.core.util.cache.TrackingCache
import icu.windea.pls.core.util.cache.trackedBy
import icu.windea.pls.core.util.io.parsePath
import icu.windea.pls.integrations.ChronicleIntegrationsBundle
import icu.windea.pls.integrations.lints.providers.TigerLintToolProvider
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.analysis.ParadoxDescriptorModBasedModMetadata
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeoutException

/**
 * Tiger 检查工具服务。
 *
 * 该服务为应用级别，不依赖项目或 PSI；检查结果通过 [TrackingCache] 按请求缓存。
 *
 * 参见：[Tiger](https://github.com/amtep/tiger)
 *
 * @see TigerLintToolProvider
 */
@Service
@Suppress("UnstableApiUsage")
class TigerLintToolService : Disposable {
    // 带追踪的检查结果缓存
    private val cache = CacheBuilder("maximumSize=128,expireAfterAccess=30m,softValues")
        .build<TigerLintRequest, TigerLintResult>()
        .trackedBy { it.modificationTracker }
    // 追踪相关配置（包括可执行文件路径和 .conf 配置文件）的更改
    private val modificationTrackers = ConcurrentHashMap<ParadoxGameType, SimpleModificationTracker>()
    // 请求级别的检查结果同步锁（避免重复检查）
    private val lintResultLocks = ConcurrentHashMap<TigerLintRequest, Any>()
    // 请求级别的检查结果状态标记（直到成功之前，避免多次发送警告通知，目前不绑定到 project）
    private val lintResultStatuses = ConcurrentHashMap<TigerLintRequest, Boolean>()

    /** @see TigerLintToolProvider */
    fun findTool(gameType: ParadoxGameType): TigerLintToolProvider? {
        if (!isEnabled()) return null
        return LintToolProvider.EP_NAME.extensionList.findIsInstance<TigerLintToolProvider> { it.isAvailable(gameType) }
    }

    /**
     * 备注：调用方应确保不在 EDT/readAction 中等待该操作。
     *
     * @see TigerLintToolProvider.validateRootDirectory
     */
    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    suspend fun validateRootDirectory(request: TigerLintRequest): TigerLintResult {
        val gameType = request.gameType
        val tool = findTool(gameType) ?: throw UnsupportedOperationException("Unsupported: Tiger lint tool is not available for ${gameType.id}.")
        warnIfIncorrectContext(tool)
        return with(tool) {
            doValidateRootDirectory(request)
        }
    }

    context(tool: TigerLintToolProvider)
    private suspend fun doValidateRootDirectory(request: TigerLintRequest): TigerLintResult {
        try {
            val result = tool.validateRootDirectory(request)
            logOperation(tool) { "validate (modPath=${request.modPath})" }
            return result
        } catch (e: Exception) {
            checkCancellation(e)
            logOperation(tool, e) { "validate (modPath=${request.modPath})" }
            throw e
        }
    }

    private fun warnIfIncorrectContext(tool: TigerLintToolProvider): Boolean {
        // 3.0.5 warn if current context is incorrect (EDT or readAction), and skip operation
        val application = ApplicationManager.getApplication()
        val message = when {
            application.isDispatchThread -> "on EDT"
            application.holdsReadLock() -> "under readAction"
            else -> return false
        }
        val detail = "[${tool.name}] Tiger lint tool is used $message."
        if (ChronicleCapabilities.Test.strictContext) throw IllegalStateException(detail)
        if (!warnedStacks.add(ExceptionUtil.currentStackTrace())) return true
        logger.warn(detail)
        return true
    }

    private fun logOperation(tool: TigerLintToolProvider, error: Exception? = null, operation: () -> String) {
        if (error != null) {
            if (!warnedStacks.add(ExceptionUtil.currentStackTrace())) return
            logger.warn("[${tool.name}] Execute tiger lint tool failed. Operation: ${operation()}.")
        } else {
            if (!logger.isDebugEnabled) return
            logger.debug("[${tool.name}] Execute tiger lint tool success. Operation: ${operation()}.")
        }
    }

    fun isEnabled(): Boolean {
        return LintToolSettings.getInstance().enableTiger
    }

    fun getModificationTracker(gameType: ParadoxGameType): SimpleModificationTracker {
        return modificationTrackers.getOrPut(gameType) { SimpleModificationTracker() }
    }

    /**
     * 收集文件的 Tiger 检查请求。
     *
     * 该方法可能访问 PSI，只能在读操作中调用。
     */
    fun collectRequest(file: PsiFile): TigerLintRequest? {
        if (!isEnabled()) return null
        if (file.language !is ParadoxLanguage) return null
        val fileInfo = selectFile(file)?.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.Mod) return null // 目前的实现：仅适用于模组目录（中的文件）
        val settings = ChronicleProfilesSettings.getInstance().getGameOrModSettings(rootInfo)
        if (settings?.options?.disableTiger == true) return null // 检查是否在游戏或模组设置中禁用
        val gameType = rootInfo.gameType
        val tool = findTool(gameType) ?: return null
        val executablePath = tool.getExecutablePath() ?: return null
        val confPath = tool.getConfPath()
        val workingDirectory = executablePath.parent ?: return null
        val rootFile = rootInfo.rootFile
        val modSettings = ChronicleProfilesSettings.getInstance().state.modSettings.get(rootFile.path)
        val gamePath = modSettings?.finalGameDirectory.parsePath()
        val modPath = when (val modMetadata = rootInfo.metadata) {
            // `.mod` file (generally `descriptor.mod` under the mod directory)
            is ParadoxDescriptorModBasedModMetadata -> modMetadata.infoPath
            // the mod root directory
            else -> rootInfo.rootFile.toNioPathOrNull() ?: return null
        }
        val outputUuid = rootFile.url.toUuidString()
        val outputPath = ChronicleDataPathService.getInstance().lintResultsPath.resolve("${tool.name}-result@$outputUuid.json")
        return TigerLintRequest(
            name = tool.name,
            gameType = gameType,
            executablePath = executablePath,
            confPath = confPath,
            workingDirectory = workingDirectory,
            rootFile = rootFile,
            gamePath = gamePath,
            modPath = modPath,
            outputPath = outputPath,
        )
    }

    /**
     * 获取请求对应的检查结果。相同请求会被合并，并按修改追踪器自动失效。
     */
    fun getTigerLintResult(request: TigerLintRequest, project: Project): TigerLintResult? {
        if (!isEnabled()) return null
        val lock = lintResultLocks.getOrPut(request) { Any() }
        val result = try {
            synchronized(lock) { // 这里需要加锁（不要直接对 `VirtualFile` 加锁）
                cache.get(request) { computeTigerLintResult(it, project) }
            }
        } finally {
            lintResultLocks.remove(request, lock)
        }
        return processTigerLintResult(result, project)
    }

    private fun computeTigerLintResult(request: TigerLintRequest, project: Project): TigerLintResult {
        val modificationTracker = MergedModificationTracker(
            request.rootFile,
            getModificationTracker(request.gameType),
            ChronicleModificationTrackers.ScriptFile,
            ChronicleModificationTrackers.LocalisationFile,
        )
        val result = try {
            collectTigerLintResult(request, project)
        } catch (e: Exception) {
            checkCancellation(e)
            TigerLintResult(request, error = e)
        }
        return result.copy(modificationTracker = modificationTracker)
    }

    private fun collectTigerLintResult(request: TigerLintRequest, project: Project): TigerLintResult {
        // 3.0.5 加上状态指示，因为检查结果的缓存失效时机并不是那么显而易见
        val name = request.name
        val fileUrl = request.rootFile.presentableUrl
        val progressTitle = ChronicleIntegrationsBundle.message("lint.tiger.validate.progress.title", name)
        val progressText = ChronicleIntegrationsBundle.message("lint.tiger.validate.progress.text", name, fileUrl)
        val taskCancellation = TaskCancellation.nonCancellable()
        return if (ApplicationManager.getApplication().isDispatchThread) {
            runWithModalProgressBlocking(ModalTaskOwner.project(project), progressTitle, taskCancellation) {
                reportRawProgress { rawReporter ->
                    rawReporter.text(progressText)
                    validateRootDirectory(request)
                }
            }
        } else {
            runBlockingCancellable {
                withBackgroundProgress(project, progressTitle, taskCancellation) {
                    reportRawProgress { rawReporter ->
                        rawReporter.text(progressText)
                        validateRootDirectory(request)
                    }
                }
            }
        }
    }

    private fun processTigerLintResult(result: TigerLintResult, project: Project): TigerLintResult {
        val request = result.request
        val error = result.error
        val status = error != null
        val oldStatus = lintResultStatuses.put(request, status) ?: true
        if (error != null && oldStatus) {
            // 3.0.5 if previous result is failed, do not notify again
            notifyWarningNotification(request, error, project)
        }
        return result
    }

    private fun notifyWarningNotification(request: TigerLintRequest, error: Exception, project: Project) {
        val name = request.name
        val fileUrl = request.rootFile.presentableUrl
        val title = ChronicleIntegrationsBundle.message("lint.tiger.notification.warning.title", name)
        val content = error.message?.let { message -> ChronicleIntegrationsBundle.message("lint.tiger.notification.warning.content", fileUrl, message) }
            ?: ChronicleIntegrationsBundle.message("lint.tiger.notification.warning.content1", fileUrl)
        ChronicleNotificationGroups.global().createNotification(title, content, NotificationType.WARNING).notify(project)
    }

    override fun dispose() {
        cache.invalidateAll()
        cache.cleanUp()
        modificationTrackers.clear()
        lintResultLocks.clear()
        lintResultStatuses.clear()
    }

    companion object {
        private val logger = logger<TigerLintToolProvider>()
        private val warnedStacks = ConcurrentHashMap.newKeySet<String>()

        @JvmStatic
        fun getInstance(): TigerLintToolService = service()
    }
}
