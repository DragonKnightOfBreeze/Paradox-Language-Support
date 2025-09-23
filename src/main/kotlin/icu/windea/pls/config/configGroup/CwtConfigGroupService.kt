package icu.windea.pls.config.configGroup

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.withModalProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.util.application
import fleet.multiplatform.shims.ConcurrentHashMap
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val logger = logger<CwtConfigGroupService>()

/**
 * 规则分组的服务。主要用于获取与刷新规则分组，以及初始化其中的规则数据。
 */
@Service
@Suppress("UnstableApiUsage")
class CwtConfigGroupService {
    fun initAsync(project: Project, callback: () -> Unit = {}) {
        val configGroups = getConfigGroups(project).values

        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            if (!project.isDefault && PlsFacade.getInternalSettings().showModalOnInitConfigGroups) {
                // 显示不可取消的模态进度条
                val title = PlsBundle.message("configGroup.init.progressTitle")
                withModalProgress(ModalTaskOwner.project(project), title, TaskCancellation.nonCancellable()) {
                    doInit(configGroups, project)
                }
            } else {
                // 静默执行
                doInit(configGroups, project)
            }
            if (!project.isDefault) {
                // 重新解析已打开的文件
                val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
                PlsCoreManager.reparseFiles(openedFiles)
            }
            callback()
        }
    }

    private suspend fun doInit(configGroups: Collection<CwtConfigGroup>, project: Project) {
        val targetName = if (project.isDefault) "application" else "project '${project.name}'"
        logger.info("Initializing config groups for $targetName...")
        val start = System.currentTimeMillis()
        configGroups.forEachConcurrent { configGroup ->
            configGroup.init()
            getConfigGroup(getDefaultProject(), configGroup.gameType).init() // 之后也要刷新默认项目的规则数据
        }
        val end = System.currentTimeMillis()
        logger.info("Initialized config groups for $targetName in ${end - start} ms.")
    }

    /**
     * 得到指定项目与游戏类型的规则分组。
     */
    fun getConfigGroup(project: Project, gameType: ParadoxGameType): CwtConfigGroup {
        return getConfigGroups(project).getValue(gameType)
    }

    /**
     * 得到指定项目的所有规则分组。
     */
    fun getConfigGroups(project: Project): Map<ParadoxGameType, CwtConfigGroup> {
        // #184
        // 不能将规则数据缓存到默认项目的服务对象中，否则会被不定期清空
        // 因此，目前改为缓存到应用（默认项目的规则数据）或项目（对应项目的规则数据）的用户数据中
        val componentManager = if (project.isDefault) application else project
        val configGroups = synchronized(this) { // `getOrPutUserData` 并不保证线程安全，因此这里要加锁
            componentManager.getOrPutUserData(PlsKeys.configGroups) { createConfigGroups(project) }
        }
        return configGroups
    }

    private fun createConfigGroups(project: Project): Map<ParadoxGameType, CwtConfigGroup> {
        // 直接创建所有游戏类型的规则分组（之后再预加载规则数据）
        val configGroups = ConcurrentHashMap<ParadoxGameType, CwtConfigGroup>()
        ParadoxGameType.getAll(withCore = true).forEach { gameType ->
            configGroups.put(gameType, CwtConfigGroup(project, gameType))
        }
        return configGroups
    }

    @Synchronized
    fun refreshConfigGroups(configGroups: Collection<CwtConfigGroup>, project: Project) {
        if (configGroups.isEmpty()) return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            // 显示可以取消的模态进度条
            val title = PlsBundle.message("configGroup.refresh.progressTitle")
            withModalProgress(ModalTaskOwner.project(project), title, TaskCancellation.cancellable()) {
                doRefresh(configGroups, project)
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
                updateRefreshFloatingToolbar(project)

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

    private suspend fun doRefresh(configGroups: Collection<CwtConfigGroup>, project: Project) {
        val targetName = if (project.isDefault) "application" else "project '${project.name}'"
        logger.info("Refreshing config groups for $targetName...")
        val start = System.currentTimeMillis()
        configGroups.forEachConcurrent { configGroup ->
            configGroup.init()
            getConfigGroup(getDefaultProject(), configGroup.gameType).init() // 之后也要刷新默认项目的规则数据
        }
        val end = System.currentTimeMillis()
        logger.info("Refreshed config groups for $targetName in ${end - start} ms.")
    }

    private fun getRootFilePaths(configGroups: Collection<CwtConfigGroup>): Set<String> {
        val gameTypes = configGroups.mapTo(mutableSetOf()) { it.gameType }
        val rootFilePaths = mutableSetOf<String>()
        PlsFacade.getProfilesSettings().gameDescriptorSettings.values.forEach f@{ settings ->
            val gameType = settings.finalGameType
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

    fun updateRefreshFloatingToolbar(project: Project) {
        val provider = FloatingToolbarProvider.EP_NAME.findExtensionOrFail(ConfigGroupRefreshFloatingProvider::class.java)
        provider.updateToolbarComponents(project)
    }
}
