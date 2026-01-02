package icu.windea.pls.config.configGroup

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.lang.util.PlsDaemonManager
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * 规则分组的服务。主要用于获取与刷新规则分组，以及初始化其中的规则数据。
 */
@Suppress("UnstableApiUsage")
@Service(Service.Level.APP, Service.Level.PROJECT)
class CwtConfigGroupService(private val project: Project = getDefaultProject()) : Disposable {
    private var cache = createConfigGroups()

    suspend fun init(configGroups: Collection<CwtConfigGroup>, project: Project) {
        val start = System.currentTimeMillis()
        val configGroupsToInit = buildSet {
            addAll(configGroups)
            // 之后也要初始化默认项目的规则数据
            configGroups.forEach { configGroup -> add(PlsFacade.getConfigGroup(configGroup.gameType)) }
        }
        configGroupsToInit.forEachConcurrent { configGroup -> configGroup.init() }
        val end = System.currentTimeMillis()
        val targetName = if (project.isDefault) "application" else "project '${project.name}'"
        logger.info("Initialized config groups for $targetName in ${end - start} ms.")
    }

    suspend fun refresh(configGroups: Collection<CwtConfigGroup>, project: Project) {
        val start = System.currentTimeMillis()
        val configGroupsToInit = buildSet {
            addAll(configGroups)
            // 之后也要刷新默认项目的规则数据
            configGroups.forEach { configGroup -> add(PlsFacade.getConfigGroup(configGroup.gameType)) }
        }
        configGroupsToInit.forEachConcurrent { configGroup -> configGroup.init() }
        val end = System.currentTimeMillis()
        val targetName = if (project.isDefault) "application" else "project '${project.name}'"
        logger.info("Refreshed config groups for $targetName in ${end - start} ms.")
    }

    fun initAsync(callback: () -> Unit = {}) {
        val configGroups = getConfigGroups().values
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            if (project.isDefault) {
                // 静默执行
                init(configGroups, project)
            } else {
                // 显示不可取消的后台进度条
                val title = PlsBundle.message("configGroup.init.progressTitle")
                withBackgroundProgress(project, title, TaskCancellation.nonCancellable()) {
                    init(configGroups, project)
                }
                callback()
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
            return CwtConfigGroupImpl(project, gameType)
        }
        return configGroup
    }

    /**
     * 得到指定项目的所有规则分组。
     */
    fun getConfigGroups(): Map<ParadoxGameType, CwtConfigGroup> {
        // #184
        // 不能将规则数据缓存到默认项目的服务中，否则会被不定期清空，因为需要改为缓存到应用的服务中
        if (project.isDefault) return getInstance().cache
        return cache
    }

    private fun createConfigGroups(): Map<ParadoxGameType, CwtConfigGroup> {
        val gameTypes = ParadoxGameType.getAll(withCore = true)
        val configGroups = buildMap(gameTypes.size) {
            for (gameType in gameTypes) {
                this[gameType] = CwtConfigGroupImpl(project, gameType)
            }
        }
        return configGroups
    }

    @Synchronized
    fun refreshConfigGroups(configGroups: Collection<CwtConfigGroup>) {
        if (project.isDefault) return
        if (configGroups.isEmpty()) return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            // 显示可以取消的后台进度条
            val title = PlsBundle.message("configGroup.refresh.progressTitle")
            withBackgroundProgress(project, title, TaskCancellation.cancellable()) {
                refresh(configGroups, project)
            }
            // 重新解析已打开的文件
            val openedFiles = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true)
            PlsDaemonManager.reparseFiles(openedFiles)
        }.invokeOnCompletion { e ->
            if (e is CancellationException) {
                PlsFacade.createNotification(
                    NotificationType.INFORMATION,
                    PlsBundle.message("configGroup.refresh.notification.cancelled.title"),
                    ""
                ).notify(project)
            } else if (e == null) {
                updateRefreshFloatingToolbar()
                val action = NotificationAction.createSimple(PlsBundle.message("configGroup.refresh.notification.action.reindex")) {
                    reparseFilesInRootFilePaths(configGroups)
                }
                PlsFacade.createNotification(
                    NotificationType.INFORMATION,
                    PlsBundle.message("configGroup.refresh.notification.finished.title"),
                    PlsBundle.message("configGroup.refresh.notification.finished.content")
                ).addAction(action).notify(project)
            }
        }
    }

    private fun reparseFilesInRootFilePaths(configGroups: Collection<CwtConfigGroup>) {
        // 重新解析并刷新（IDE之后会自动请求重新索引）
        // TODO 1.2.0+ 需要考虑优化 - 重新索引可能不是必要的，也可能仅需要重新索引少数几个文件
        reparseFilesInRootFilePaths(configGroups)
        val rootFilePaths = getRootFilePaths(configGroups)
        val files = PlsDaemonManager.findFilesByRootFilePaths(rootFilePaths)
        PlsDaemonManager.reparseFiles(files)
    }

    private fun getRootFilePaths(configGroups: Collection<CwtConfigGroup>): Set<String> {
        val gameTypes = configGroups.mapTo(mutableSetOf()) { it.gameType }
        val rootFilePaths = mutableSetOf<String>()
        PlsProfilesSettings.getInstance().state.gameDescriptorSettings.values
            .filter { it.finalGameType in gameTypes }
            .mapNotNullTo(rootFilePaths) { it.gameDirectory }
        PlsProfilesSettings.getInstance().state.modDescriptorSettings.values
            .filter { it.finalGameType in gameTypes }
            .mapNotNullTo(rootFilePaths) { it.modDirectory }
        return rootFilePaths
    }

    fun updateRefreshFloatingToolbar() {
        if (project.isDefault) return
        val provider = FloatingToolbarProvider.EP_NAME.findExtensionOrFail(ConfigGroupRefreshFloatingProvider::class.java)
        provider.updateToolbarComponents(project)
    }

    override fun dispose() {
        // 清理规则分组数据，避免内存泄露
        cache.values.forEach { it.clear() }
        cache = emptyMap()
    }

    companion object {
        private val logger = logger<CwtConfigGroupService>()

        @JvmStatic
        fun getInstance(): CwtConfigGroupService = service()

        @JvmStatic
        fun getInstance(project: Project): CwtConfigGroupService = project.service()
    }
}
