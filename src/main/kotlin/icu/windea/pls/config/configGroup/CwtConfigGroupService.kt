package icu.windea.pls.config.configGroup

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.platform.util.progress.reportProgress
import com.intellij.util.application
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.listeners.CwtConfigGroupRefreshStatusListener
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.ide.analysis.ChronicleAnalysisManager
import icu.windea.pls.ide.notification.ChronicleNotificationGroups
import icu.windea.pls.lang.ParadoxLibraryService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.ChronicleProfilesSettings
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 规则分组的服务。主要用于获取与刷新规则分组，以及初始化其中的规则数据。
 */
@Suppress("UnstableApiUsage")
@Service(Service.Level.APP, Service.Level.PROJECT)
class CwtConfigGroupService(private val project: Project = getDefaultProject()) : Disposable {
    private var cache = createConfigGroups()

    suspend fun refreshBuiltInConfigFiles() {
        if (project.isDisposed) return
        val files = CwtConfigManager.getBuiltInConfigRootDirectories(project)
        if (files.isEmpty()) return
        if (ChronicleFacade.isUnitTestMode()) {
            files.forEach { VfsUtil.markDirtyAndRefresh(false, true, true, it) }
            return
        }
        // 必须先切换到 EDT
        withContext(Dispatchers.EDT) {
            // 显示可以取消的模态进度条
            val title = ChronicleBundle.message("configGroup.refresh.builtin.progress.title")
            runWithModalProgressBlocking(project, title) {
                files.forEach { VfsUtil.markDirtyAndRefresh(false, true, true, it) }
            }
        }
    }

    suspend fun initConfigGroups(configGroups: Collection<CwtConfigGroup>) {
        if (configGroups.isEmpty()) return
        if (project.isDisposed) return
        val start = System.currentTimeMillis()
        processConfigGroups(configGroups, false)
        val end = System.currentTimeMillis()
        val targetName = if (project.isDefault) "application" else "project '${project.name}'"
        logger.info("Initialized config groups for $targetName in ${end - start} ms.")
    }

    suspend fun refreshConfigGroups(configGroups: Collection<CwtConfigGroup>) {
        if (configGroups.isEmpty()) return
        if (project.isDisposed) return
        val start = System.currentTimeMillis()
        processConfigGroups(configGroups, true)
        val end = System.currentTimeMillis()
        val targetName = if (project.isDefault) "application" else "project '${project.name}'"
        logger.info("Refreshed config groups for $targetName in ${end - start} ms.")
    }

    private suspend fun processConfigGroups(configGroups: Collection<CwtConfigGroup>, refresh: Boolean) {
        if (project.isDefault) {
            val toProcess = configGroups.toSet()
            toProcess.forEachConcurrent { configGroup ->
                configGroup.init()
            }
        } else {
            val toProcess = buildSet {
                addAll(configGroups)
                // 之后也要初始化应用级别的规则数据
                configGroups.mapTo(this) { configGroup -> ChronicleFacade.getConfigGroup(configGroup.gameType) }
            }
            reportProgress(toProcess.size) { reporter ->
                toProcess.forEachConcurrent { configGroup ->
                    val default = configGroup.project.isDefault
                    val gameTypeId = configGroup.gameType.id
                    val step = when {
                        default && !refresh -> ChronicleBundle.message("configGroup.process.step.application", gameTypeId)
                        default -> ChronicleBundle.message("configGroup.process.step.application.refresh", gameTypeId)
                        !refresh -> ChronicleBundle.message("configGroup.process.step.project", gameTypeId)
                        else -> ChronicleBundle.message("configGroup.process.step.project.refresh", gameTypeId)
                    }
                    reporter.itemStep(step) {
                        configGroup.init()
                    }
                }
            }
        }
    }

    /**
     * 得到指定项目与游戏类型的规则分组。
     */
    fun getConfigGroup(gameType: ParadoxGameType): CwtConfigGroup {
        val configGroup = getConfigGroups().get(gameType)
        if (configGroup == null) {
            // return temporary empty config group to avoid NPE
            return CwtConfigGroup(project, gameType)
        }
        return configGroup
    }

    /**
     * 得到指定项目的所有规则分组。
     */
    fun getConfigGroups(): Map<ParadoxGameType, CwtConfigGroup> {
        // #184 不能将规则数据缓存到应用级别的服务中，否则会被不定期清空，因为需要改为缓存到应用的服务中
        if (project.isDefault) return getInstance().cache
        return cache
    }

    private fun createConfigGroups(): Map<ParadoxGameType, CwtConfigGroup> {
        val gameTypes = ParadoxGameType.getAll()
        val configGroups = buildMap(gameTypes.size) {
            for (gameType in gameTypes) {
                this[gameType] = CwtConfigGroup(project, gameType)
            }
        }
        return configGroups
    }

    /**
     * 检查指定项目与上下文的规则分组是否已加载完毕。
     *
     * @param context 用于获取游戏类型的上下文对象。
     */
    fun checkConfigGroupInitialized(context: Any?): Boolean {
        if (project.isDisposed) return false
        val gameType = selectGameType(context) ?: return true
        if (!getConfigGroup(ParadoxGameType.Core).initialized) return false
        if (gameType != ParadoxGameType.Core && !getConfigGroup(gameType).initialized) return false
        return true
    }

    fun initConfigGroupsAsync() {
        if (project.isDisposed) return
        val configGroups = getConfigGroups().values
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            if (project.isDefault) {
                // 静默执行
                initConfigGroups(configGroups)
            } else {
                // 显示不可取消的后台进度条
                val title = ChronicleBundle.message("configGroup.init.progress.title")
                withBackgroundProgress(project, title, TaskCancellation.nonCancellable()) {
                    initConfigGroups(configGroups)
                }
                // 规则数据加载完毕后，重新解析已打开的文件
                reparseAllOpenFiles()
                // 规则数据加载完毕后，异步刷新外部库
                refreshRootsForLibraries()
            }
        }
    }

    @Synchronized
    fun refreshConfigGroupsAsync(configGroups: Collection<CwtConfigGroup>) {
        if (configGroups.isEmpty()) return
        if (project.isDefault || project.isDisposed) return
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            // 显示可以取消的后台进度条
            val title = ChronicleBundle.message("configGroup.refresh.progress.title")
            withBackgroundProgress(project, title, TaskCancellation.cancellable()) {
                refreshConfigGroups(configGroups)
            }
            // 规则数据刷新完毕后，重新解析已打开的文件
            reparseAllOpenFiles()
            // 规则数据加载完毕后，异步刷新外部库
            refreshRootsForLibraries()
        }.invokeOnCompletion { e ->
            if (e is CancellationException) {
                val title = ChronicleBundle.message("configGroup.refresh.notification.cancelled.title")
                ChronicleNotificationGroups.global().createNotification(title, "", NotificationType.INFORMATION).notify(project)
            } else if (e == null) {
                updateRefreshStatus()
                val action = NotificationAction.createSimple(ChronicleBundle.message("configGroup.refresh.notification.action.reindex")) {
                    reparseAllFilesInRootFilePaths(configGroups)
                    refreshRootsForLibraries(force = true)
                }
                val title = ChronicleBundle.message("configGroup.refresh.notification.finished.title")
                val content = ChronicleBundle.message("configGroup.refresh.notification.finished.content")
                ChronicleNotificationGroups.global().createNotification(title, content, NotificationType.INFORMATION).addAction(action).notify(project)
            }
        }
    }

    fun reparseAllOpenFiles() {
        if (project.isDefault || project.isDisposed) return
        // 重新解析已打开的文件
        val allOpenFiles = ChronicleAnalysisManager.findAllOpenFiles()
        ChronicleAnalysisManager.reparseFiles(allOpenFiles)
    }

    fun reparseAllFilesInRootFilePaths(configGroups: Collection<CwtConfigGroup>) {
        if (project.isDefault || project.isDisposed) return
        // 重新解析涉及的根路径下的所有文件
        val gameTypes = configGroups.mapTo(mutableSetOf()) { it.gameType }
        val rootFilePaths = mutableSetOf<String>()
        ChronicleProfilesSettings.getInstance().state.gameDescriptorSettings.values
            .filter { it.finalGameType in gameTypes }
            .mapNotNullTo(rootFilePaths) { it.gameDirectory }
        ChronicleProfilesSettings.getInstance().state.modDescriptorSettings.values
            .filter { it.finalGameType in gameTypes }
            .mapNotNullTo(rootFilePaths) { it.modDirectory }
        val files = ChronicleAnalysisManager.findAllFilesByRootFilePaths(rootFilePaths)
        ChronicleAnalysisManager.reparseFiles(files)
    }

    fun refreshRootsForLibraries(force: Boolean = false) {
        if (project.isDefault || project.isDisposed) return
        // 异步刷新外部库
        CwtConfigGroupLibraryService.getInstance(project).refreshRootsAsync(force)
        ParadoxLibraryService.getInstance(project).refreshRootsAsync(force)
    }

    fun updateRefreshStatus() {
        if (project.isDefault || project.isDisposed) return
        // 通知规则分组的刷新状态发生更改
        application.messageBus.syncPublisher(CwtConfigGroupRefreshStatusListener.TOPIC).onChange(project)
    }

    override fun dispose() {
        // 清理规则分组缓存，避免内存泄露
        cache.values.forEach { it.clear() }
        cache = emptyMap()
    }

    companion object {
        private val logger = logger<CwtConfigGroupService>()

        @JvmStatic
        fun getInstance(): CwtConfigGroupService = service()

        @JvmStatic
        fun getInstance(project: Project?): CwtConfigGroupService = if (project == null || project.isDefault) getInstance() else project.service()
    }
}
