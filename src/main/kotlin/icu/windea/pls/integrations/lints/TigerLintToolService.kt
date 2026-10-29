package icu.windea.pls.integrations.lints

import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiFile
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.base.notification.ChronicleNotificationGroups
import icu.windea.pls.base.settings.ChronicleProfilesSettings
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.TrackingCache
import icu.windea.pls.core.cache.trackedBy
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toUuidString
import icu.windea.pls.core.util.MergedModificationTracker
import icu.windea.pls.integrations.ChronicleIntegrationsBundle
import icu.windea.pls.integrations.lints.providers.TigerLintToolProvider
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.util.concurrent.ConcurrentHashMap

/**
 * Tiger 检查工具服务。
 *
 * 该服务为应用级别，不依赖项目或 PSI；检查结果通过 [TrackingCache] 按请求缓存。
 */
@Service
class TigerLintToolService : Disposable {
    // 带追踪的检查结果缓存
    private val cache = CacheBuilder("maximumSize=128,expireAfterAccess=30m,softValues")
        .build<TigerLintRequest, TigerLintResult>()
        .trackedBy { it.modificationTracker }
    // 追踪相关配置（包括可执行文件路径和 .conf 配置文件）的更改
    private val modificationTrackers = ConcurrentHashMap<ParadoxGameType, SimpleModificationTracker>()
    // 请求级别的检查结果同步锁
    private val lintResultLocks = ConcurrentHashMap<TigerLintRequest, Any>()

    fun isEnabled(): Boolean {
        return LintToolSettings.getInstance().enableTiger
    }

    /** @see icu.windea.pls.integrations.lints.providers.TigerLintToolProvider */
    fun findTool(gameType: ParadoxGameType): TigerLintToolProvider? {
        if (!isEnabled()) return null
        return LintToolProvider.EP_NAME.extensionList.findIsInstance<TigerLintToolProvider> { it.isAvailable(gameType) }
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
        val gamePath = modSettings?.finalGameDirectory?.orNull()
        val rootPath = rootFile.path
        val modPath = rootFile.findChild("descriptor.mod")?.path
            ?: rootFile.findChild(".metadata")?.findChild("metadata.json")?.path
            ?: rootPath
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
            rootPath = rootPath,
            modPath = modPath,
            outputPath = outputPath,
        )
    }

    /**
     * 获取请求对应的检查结果。相同请求会被合并，并按修改追踪器自动失效。
     */
    fun getTigerLintResult(request: TigerLintRequest): TigerLintResult? {
        if (!isEnabled()) return null
        val lock = lintResultLocks.getOrPut(request) { Any() }
        try {
            return synchronized(lock) { // 这里需要加锁（不要直接对 `VirtualFile` 加锁）
                cache.get(request) { loadTigerLintResult(it) }
            }
        } finally {
            lintResultLocks.remove(request, lock)
        }
    }

    private fun loadTigerLintResult(request: TigerLintRequest): TigerLintResult {
        val modificationTracker = MergedModificationTracker(
            request.rootFile,
            getModificationTracker(request.gameType),
            ChronicleModificationTrackers.ScriptFile,
            ChronicleModificationTrackers.LocalisationFile,
        )
        val tool = findTool(request.gameType)
        val result = if (tool == null) {
            val error = IllegalStateException("Tiger lint tool is not available. (game type: ${request.gameType})")
            notifyWarningNotification(request, error)
            TigerLintResult(request.name, error = error)
        } else {
            try {
                tool.validateRootDirectory(request)
            } catch (e: Exception) {
                checkCancellation(e)
                notifyWarningNotification(request, e)
                TigerLintResult(request.name, error = e)
            }
        }
        return result.copy(name = request.name, request = request, modificationTracker = modificationTracker)
    }

    private fun notifyWarningNotification(request: TigerLintRequest, e: Throwable) {
        val fileUrl = request.rootFile.presentableUrl
        val title = ChronicleIntegrationsBundle.message("lint.tiger.notification.warning.title", request.name)
        val content = e.message?.let { message -> ChronicleIntegrationsBundle.message("lint.tiger.notification.warning.content", fileUrl, message) }
            ?: ChronicleIntegrationsBundle.message("lint.tiger.notification.warning.content1", fileUrl)
        ChronicleNotificationGroups.global().createNotification(title, content, NotificationType.WARNING).notify(null)
    }

    override fun dispose() {
        cache.invalidateAll()
        cache.cleanUp()
        modificationTrackers.clear()
        lintResultLocks.clear()
    }

    companion object {
        @JvmStatic
        fun getInstance(): TigerLintToolService = service()
    }
}
