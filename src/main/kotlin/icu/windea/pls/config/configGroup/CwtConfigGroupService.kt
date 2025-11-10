package icu.windea.pls.config.configGroup

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.withModalProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.util.application
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.lang.util.PlsAnalyzeManager
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

private val logger = logger<CwtConfigGroupService>()

/**
 * 规则分组的服务。主要用于获取与刷新规则分组，以及初始化其中的规则数据。
 */
@Service(Service.Level.APP, Service.Level.PROJECT)
@Suppress("UnstableApiUsage")
class CwtConfigGroupService(
    private val project: Project = getDefaultProject()
) : Disposable {
    object Keys : KeyRegistry() {
        val defaultConfigGroup = createKey<Map<ParadoxGameType, CwtConfigGroup>>("pls.default.configGroup")
    }

    private val cache = createConfigGroups()

    suspend fun init(configGroups: Collection<CwtConfigGroup>, project: Project) {
        val start = System.currentTimeMillis()
        configGroups.forEachConcurrent { configGroup ->
            configGroup.init()
            PlsFacade.getConfigGroup(configGroup.gameType).init() // 之后也要刷新默认项目的规则数据
        }
        val end = System.currentTimeMillis()
        val targetName = if (project.isDefault) "application" else "project '${project.name}'"
        logger.info("Initialized config groups for $targetName in ${end - start} ms.")
    }

    suspend fun refresh(configGroups: Collection<CwtConfigGroup>, project: Project) {
        val start = System.currentTimeMillis()
        configGroups.forEachConcurrent { configGroup ->
            configGroup.init()
            PlsFacade.getConfigGroup(configGroup.gameType).init() // 之后也要刷新默认项目的规则数据
        }
        val end = System.currentTimeMillis()
        val targetName = if (project.isDefault) "application" else "project '${project.name}'"
        logger.info("Refreshed config groups for $targetName in ${end - start} ms.")
    }

    fun initAsync(callback: () -> Unit = {}) {
        val configGroups = getConfigGroups().values

        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            if (!project.isDefault && PlsFacade.getInternalSettings().showModalOnInitConfigGroups) {
                // 显示不可取消的模态进度条
                val title = PlsBundle.message("configGroup.init.progressTitle")
                withModalProgress(ModalTaskOwner.project(project), title, TaskCancellation.nonCancellable()) {
                    init(configGroups, project)
                }
            } else {
                // 静默执行
                init(configGroups, project)
            }
            callback()
        }
    }

    /**
     * 得到指定项目与游戏类型的规则分组。
     */
    fun getConfigGroup(gameType: ParadoxGameType): CwtConfigGroup {
        return getConfigGroups().getValue(gameType)
    }

    /**
     * 得到指定项目的所有规则分组。
     */
    fun getConfigGroups(): Map<ParadoxGameType, CwtConfigGroup> {
        // #184
        // 不能将规则数据缓存到默认项目的服务对象中，否则会被不定期清空
        // 因此，目前改为缓存到应用的用户数据（默认项目的规则数据）或服务（对应项目的规则数据）中
        if (project.isDefault) {
            // `getOrPutUserData` 并不保证线程安全，因此这里要加锁
            val key = Keys.defaultConfigGroup
            return synchronized(key) {
                application.getOrPutUserData(key) { createConfigGroups() }
            }
        }
        return cache
    }

    private fun createConfigGroups(): MutableMap<ParadoxGameType, CwtConfigGroup> {
        // 直接创建所有游戏类型的规则分组（之后再预加载规则数据）
        val gameTypes = ParadoxGameType.getAll(withCore = true)
        val configGroups = ConcurrentHashMap<ParadoxGameType, CwtConfigGroup>(gameTypes.size)
        for (gameType in gameTypes) {
            configGroups[gameType] = CwtConfigGroup(project, gameType)
        }
        return configGroups
    }

    @Synchronized
    fun refreshConfigGroups(configGroups: Collection<CwtConfigGroup>) {
        if (project.isDefault) return
        if (configGroups.isEmpty()) return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            // 显示可以取消的模态进度条
            val title = PlsBundle.message("configGroup.refresh.progressTitle")
            withModalProgress(ModalTaskOwner.project(project), title, TaskCancellation.cancellable()) {
                refresh(configGroups, project)
            }
            // 重新解析已打开的文件
            val openedFiles = PlsAnalyzeManager.findOpenedFiles(onlyParadoxFiles = true)
            PlsAnalyzeManager.reparseFiles(openedFiles)
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
        val files = PlsAnalyzeManager.findFilesByRootFilePaths(rootFilePaths)
        PlsAnalyzeManager.reparseFiles(files)
    }

    private fun getRootFilePaths(configGroups: Collection<CwtConfigGroup>): Set<String> {
        val gameTypes = configGroups.mapTo(mutableSetOf()) { it.gameType }
        val rootFilePaths = mutableSetOf<String>()
        PlsFacade.getProfilesSettings().gameDescriptorSettings.values
            .filter { it.finalGameType in gameTypes }
            .mapNotNullTo(rootFilePaths) { it.gameDirectory }
        PlsFacade.getProfilesSettings().modDescriptorSettings.values
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
        // 清理规则分组数据
        cache.values.forEach { it.clear() }
        cache.clear()
    }
}
