package icu.windea.pls.config.configGroup

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.withModalProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.lang.settings.finalGameType
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.id
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

private val logger = logger<CwtConfigGroupService>()

@Suppress("UnstableApiUsage")
@Service(Service.Level.PROJECT)
class CwtConfigGroupService(private val project: Project) {
    private val cache = ConcurrentHashMap<String, CwtConfigGroup>()

    fun initAsync() {
        val configGroups = mutableSetOf<CwtConfigGroup>()
        configGroups.add(getConfigGroup(null))
        ParadoxGameType.entries.forEach { gameType -> configGroups.add(getConfigGroup(gameType)) }

        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            if (!project.isDefault && PlsFacade.getInternalSettings().showModalOnInitConfigGroups) {
                // 显示不可取消的模态进度条
                val title = PlsBundle.message("configGroup.init.progressTitle")
                withModalProgress(ModalTaskOwner.project(project), title, TaskCancellation.nonCancellable()) {
                    doInit(configGroups)
                }
            } else {
                // 静默执行
                doInit(configGroups)
            }
            if (!project.isDefault) {
                // 重新解析已打开的文件
                val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
                PlsCoreManager.reparseFiles(openedFiles)
            }
        }
    }

    private suspend fun doInit(configGroups: Collection<CwtConfigGroup>) {
        val projectTitle = if (project.isDefault) "default project" else "project '${project.name}'"
        logger.info("Initializing config groups for $projectTitle...")
        val start = System.currentTimeMillis()
        configGroups.forEachConcurrent { configGroup ->
            configGroup.init()
            getConfigGroupForDefaultProject(configGroup).init() // 之后也要刷新默认项目的规则数据
        }
        val end = System.currentTimeMillis()
        logger.info("Initialized config groups for $projectTitle in ${end - start} ms.")
    }

    fun getConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        return cache.computeIfAbsent(gameType.id) { createConfigGroup(gameType) }
    }

    fun getConfigGroups(): Map<String, CwtConfigGroup> {
        return cache
    }

    fun createConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        val configGroup = CwtConfigGroup(gameType, project)
        return configGroup
    }

    @Synchronized
    fun refreshConfigGroups(configGroups: Collection<CwtConfigGroup>) {
        if (configGroups.isEmpty()) return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            // 显示可以取消的模态进度条
            val title = PlsBundle.message("configGroup.refresh.progressTitle")
            withModalProgress(ModalTaskOwner.project(project), title, TaskCancellation.cancellable()) {
                doRefresh(configGroups)
            }
            // 重新解析已打开的文件
            val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
            PlsCoreManager.reparseFiles(openedFiles)
        }.invokeOnCompletion { e ->
            if (e is CancellationException) {
                PlsCoreManager.createNotification(
                    NotificationType.INFORMATION,
                    PlsBundle.message("configGroup.refresh.notification.cancelled.title"),
                    ""
                ).notify(project)
            } else if (e == null) {
                updateRefreshFloatingToolbar()

                val action = NotificationAction.createSimple(PlsBundle.message("configGroup.refresh.notification.action.reindex")) {
                    // 重新解析并刷新（IDE之后会自动请求重新索引）
                    // TODO 1.2.0+ 需要考虑优化 - 重新索引可能不是必要的，也可能仅需要重新索引少数几个文件
                    val rootFilePaths = getRootFilePaths(configGroups)
                    val files = PlsCoreManager.findFilesByRootFilePaths(rootFilePaths)
                    PlsCoreManager.reparseFiles(files)
                }
                PlsCoreManager.createNotification(
                    NotificationType.INFORMATION,
                    PlsBundle.message("configGroup.refresh.notification.finished.title"),
                    PlsBundle.message("configGroup.refresh.notification.finished.content")
                ).addAction(action).notify(project)
            }
        }
    }

    private suspend fun doRefresh(configGroups: Collection<CwtConfigGroup>) {
        val projectTitle = if (project.isDefault) "default project" else "project '${project.name}'"
        logger.info("Refreshing config groups for $projectTitle...")
        val start = System.currentTimeMillis()
        configGroups.forEachConcurrent { configGroup ->
            configGroup.init()
            getConfigGroupForDefaultProject(configGroup).init() // 之后也要刷新默认项目的规则数据
        }
        val end = System.currentTimeMillis()
        logger.info("Refreshed config groups for $projectTitle in ${end - start} ms.")
    }

    private fun getRootFilePaths(configGroups: Collection<CwtConfigGroup>): Set<String> {
        val gameTypes = configGroups.mapNotNullTo(mutableSetOf()) { it.gameType }
        val rootFilePaths = mutableSetOf<String>()
        PlsFacade.getProfilesSettings().gameDescriptorSettings.values.forEach f@{ settings ->
            val gameType = settings.gameType ?: return@f
            if (gameType !in gameTypes) return@f
            settings.gameDirectory?.let { gameDirectory -> rootFilePaths.add(gameDirectory) }
        }
        PlsFacade.getProfilesSettings().modDescriptorSettings.values.forEach f@{ settings ->
            val gameType = settings.finalGameType
            if (gameType !in gameTypes) return@f
            settings.modDirectory?.let { modDirectory -> rootFilePaths.add(modDirectory) }
        }
        return rootFilePaths
    }

    private fun getConfigGroupForDefaultProject(configGroup: CwtConfigGroup): CwtConfigGroup {
        return getDefaultProject().service<CwtConfigGroupService>().getConfigGroup(configGroup.gameType)
    }

    fun updateRefreshFloatingToolbar() {
        val provider = FloatingToolbarProvider.EP_NAME.findExtensionOrFail(ConfigGroupRefreshFloatingProvider::class.java)
        provider.updateToolbarComponents(project)
    }
}
