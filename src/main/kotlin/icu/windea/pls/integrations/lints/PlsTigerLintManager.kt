package icu.windea.pls.integrations.lints

import com.intellij.notification.NotificationType
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.toPsiDirectory
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.integrations.lints.PlsTigerLintResult.*
import icu.windea.pls.integrations.lints.tools.PlsLintToolProvider
import icu.windea.pls.integrations.lints.tools.PlsTigerLintToolProvider
import icu.windea.pls.integrations.settings.PlsIntegrationsSettings
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import kotlin.reflect.KMutableProperty0

object PlsTigerLintManager {
    object Keys : KeyRegistry() {
        val cachedTigerLintResult by createKey<CachedValue<PlsTigerLintResult>>(Keys)
        val tigerLintResultLock by createKey<Any>(Keys)
    }

    // 追踪相关配置（包括可执行文件路径和 .conf 配置文件）的更改
    val modificationTrackers = mutableMapOf<ParadoxGameType, SimpleModificationTracker>().withDefault { SimpleModificationTracker() }

    fun isEnabled(): Boolean = PlsIntegrationsSettings.getInstance().state.lint.enableTiger

    fun findTigerTool(gameType: ParadoxGameType): PlsTigerLintToolProvider? {
        if (!isEnabled()) return null
        return PlsLintToolProvider.EP_NAME.extensionList.findIsInstance<PlsTigerLintToolProvider> { it.isAvailable(gameType) }
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
        return findTigerTool(gameType) != null
    }

    fun getTigerLintResultForFile(file: PsiFile): PlsTigerLintResult? {
        // Tiger 执行于根目录级别，而这里执行于单个文件级别，对于缓存需要做特别的处理，从而优化性能

        if (!isEnabled()) return null
        return doGetTigerLintResultForFileFromCache(file)
    }

    private fun doGetTigerLintResultForFileFromCache(file: PsiFile): PlsTigerLintResult? {
        // 当当前文件发生变化时，或者相关配置发生变化时，刷新缓存

        val gameType = selectGameType(file) ?: return null
        return CachedValuesManager.getCachedValue(file, Keys.cachedTigerLintResult) {
            val value = doGetTigerLintResultForFile(file)
            val trackers = listOf(file, modificationTrackers.getValue(gameType))
            value.withDependencyItems(trackers)
        }
    }

    private fun doGetTigerLintResultForFile(file: PsiFile): PlsTigerLintResult? {
        val fileInfo = selectFile(file)?.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val rootFile = rootInfo.rootFile
        val rootDirectory = rootFile.toPsiDirectory(file.project) ?: return null
        val lock = getTigerLintResultLock(rootDirectory)
        val allResult = synchronized(lock) { // 这里需要加锁（不要直接对 `VirtualFile` 加锁）
            doGetTigerLintResultForRootDirectoryFromCache(rootDirectory)
        } ?: return null
        val result = allResult.fromPath(fileInfo.path.path)
        return result
    }

    @Suppress("unused")
    fun getTigerLintResultForRootDirectory(rootDirectory: PsiDirectory): PlsTigerLintResult? {
        if (!isEnabled()) return null
        val lock = getTigerLintResultLock(rootDirectory)
        return synchronized(lock) { // 这里需要加锁（不要直接对 `VirtualFile` 加锁）
            doGetTigerLintResultForRootDirectoryFromCache(rootDirectory)
        }
    }

    private fun doGetTigerLintResultForRootDirectoryFromCache(rootDirectory: PsiDirectory): PlsTigerLintResult? {
        // 当当前文件（即根目录）发生变化时，或者任意脚本或本地化文件发生变化时（这个条件已经足够），或者相关配置发生变化时，刷新缓存

        val gameType = selectGameType(rootDirectory) ?: return null
        return CachedValuesManager.getCachedValue(rootDirectory, Keys.cachedTigerLintResult) {
            val value = doGetTigerLintResultForRootDirectory(rootDirectory)
            val trackers = buildList {
                this += rootDirectory
                this += modificationTrackers.getValue(gameType)
                // 如果执行 Tiger 检查工具失败，当任意脚本或本地化文件发生变化时，不会刷新缓存
                if (value == null || value.error != null) {
                    this += ParadoxModificationTrackers.ScriptFile
                    this += ParadoxModificationTrackers.LocalisationFile
                }
            }
            value.withDependencyItems(trackers)
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

    private fun getTigerLintResultLock(rootDirectory: PsiDirectory): Any {
        return rootDirectory.virtualFile.getOrPutUserData(Keys.tigerLintResultLock) { Any() }
    }

    private fun notifyWarningNotification(rootDirectory: PsiDirectory, tool: PlsTigerLintToolProvider, e: Throwable) {
        val fileUrl = rootDirectory.virtualFile.presentableUrl
        val title = PlsBundle.message("lint.tiger.notification.warning.title", tool.name)
        val content = e.message?.let { message -> PlsBundle.message("lint.tiger.notification.warning.content", fileUrl, message) }
            ?: PlsBundle.message("lint.tiger.notification.warning.content1", fileUrl)
        PlsFacade.createNotification(NotificationType.WARNING, title, content).notify(rootDirectory.project)
    }

    /**
     * 按严重度（[severity]）和置信度（[confidence]）得到代码检查使用的高亮级别。
     *
     * 优先使用用户在设置中配置的 Tiger 高亮映射（Severity x Confidence），否则回退到默认映射。
     *
     * @see icu.windea.pls.integrations.settings.PlsIntegrationsSettings.TigerHighlightState
     * @see icu.windea.pls.lang.inspections.lints.PlsTigerLintAnnotator
     * @see icu.windea.pls.lang.inspections.lints.PlsTigerLintInspection
     */
    fun getHighlightSeverity(confidence: Confidence, severity: Severity): PlsLintHighlightSeverity {
        return runCatching { getConfiguredHighlightSeverity(confidence, severity).get() }
            .getOrElse { getDefaultHighlightSeverity(confidence, severity) }
    }

    /**
     * 按严重度（[severity]）和置信度（[confidence]），得到代码检查使用的已配置的高亮级别对应的配置项的 Kotlin 属性。
     *
     * @see icu.windea.pls.integrations.settings.PlsIntegrationsSettings.TigerHighlightState
     * @see icu.windea.pls.lang.inspections.lints.PlsTigerLintAnnotator
     * @see icu.windea.pls.lang.inspections.lints.PlsTigerLintInspection
     */
    fun getConfiguredHighlightSeverity(confidence: Confidence, severity: Severity): KMutableProperty0<PlsLintHighlightSeverity> {
        val mapping = PlsIntegrationsSettings.getInstance().state.lint.tigerHighlight
        return when (severity) {
            Severity.TIPS -> when (confidence) {
                Confidence.WEAK -> mapping::tipsWeak
                Confidence.REASONABLE -> mapping::tipsReasonable
                Confidence.STRONG -> mapping::tipsStrong
            }
            Severity.UNTIDY -> when (confidence) {
                Confidence.WEAK -> mapping::untidyWeak
                Confidence.REASONABLE -> mapping::untidyReasonable
                Confidence.STRONG -> mapping::untidyStrong
            }
            Severity.WARNING -> when (confidence) {
                Confidence.WEAK -> mapping::warningWeak
                Confidence.REASONABLE -> mapping::warningReasonable
                Confidence.STRONG -> mapping::warningStrong
            }
            Severity.ERROR -> when (confidence) {
                Confidence.WEAK -> mapping::errorWeak
                Confidence.REASONABLE -> mapping::errorReasonable
                Confidence.STRONG -> mapping::errorStrong
            }
            Severity.FATAL -> when (confidence) {
                Confidence.WEAK -> mapping::fatalWeak
                Confidence.REASONABLE -> mapping::fatalReasonable
                Confidence.STRONG -> mapping::fatalStrong
            }
        }
    }

    /**
     * 按严重度（[severity]）和置信度（[confidence]），得到代码检查使用的默认高亮级别。
     *
     * @see icu.windea.pls.lang.inspections.lints.PlsTigerLintAnnotator
     * @see icu.windea.pls.lang.inspections.lints.PlsTigerLintInspection
     */
    fun getDefaultHighlightSeverity(confidence: Confidence, severity: Severity): PlsLintHighlightSeverity {
        return when (severity) {
            Severity.FATAL -> PlsLintHighlightSeverity.ERROR
            Severity.ERROR -> PlsLintHighlightSeverity.ERROR
            Severity.WARNING -> PlsLintHighlightSeverity.WARNING
            Severity.UNTIDY -> when (confidence) {
                Confidence.STRONG -> PlsLintHighlightSeverity.WARNING
                else -> PlsLintHighlightSeverity.WEAK_WARNING
            }
            Severity.TIPS -> when (confidence) {
                Confidence.STRONG -> PlsLintHighlightSeverity.WEAK_WARNING
                else -> PlsLintHighlightSeverity.INFORMATION
            }
        }
    }

    fun getConfidenceDisplayName(confidence: Confidence): String {
        return when (confidence) {
            Confidence.WEAK -> PlsBundle.message("lint.tiger.confidence.weak")
            Confidence.REASONABLE -> PlsBundle.message("lint.tiger.confidence.reasonable")
            Confidence.STRONG -> PlsBundle.message("lint.tiger.confidence.strong")
        }
    }

    fun getSeverityDisplayName(severity: Severity): String {
        return when (severity) {
            Severity.TIPS -> PlsBundle.message("lint.tiger.severity.tips")
            Severity.UNTIDY -> PlsBundle.message("lint.tiger.severity.untidy")
            Severity.WARNING -> PlsBundle.message("lint.tiger.severity.warning")
            Severity.ERROR -> PlsBundle.message("lint.tiger.severity.error")
            Severity.FATAL -> PlsBundle.message("lint.tiger.severity.fatal")
        }
    }
}
