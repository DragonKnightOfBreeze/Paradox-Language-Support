package icu.windea.pls.config.configGroup

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.progress.impl.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*
import java.util.concurrent.*

private val logger = logger<CwtConfigGroupService>()

@Service(Service.Level.PROJECT)
class CwtConfigGroupService(private val project: Project) {
    private val cache = ConcurrentHashMap<String, CwtConfigGroup>()

    fun init() {
        //preload config groups
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            readAction { getConfigGroup(null) }
            ParadoxGameType.entries.forEach { gameType ->
                readAction { getConfigGroup(gameType) }
            }
        }
    }

    fun getConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        return cache.computeIfAbsent(gameType.id) { createConfigGroup(gameType) }
    }

    fun getConfigGroups(): Map<String, CwtConfigGroup> {
        return cache
    }

    fun createConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        val gameTypeId = gameType.id
        val projectName = if(project.isDefault) "default project" else "project ${project.name}"

        logger.info("Initializing config group '$gameTypeId' for $projectName...")
        val start = System.currentTimeMillis()

        val configGroup = doCreateConfigGroup(gameType)

        val end = System.currentTimeMillis()
        logger.info("Initialize config group '$gameTypeId' for $projectName finished in ${end - start} ms.")

        return configGroup
    }

    @Synchronized
    fun refreshConfigGroups(configGroups: Collection<CwtConfigGroup>) {
        //不替换configGroup，而是替换其中的userData
        if (configGroups.isEmpty()) return
        val progressTitle = PlsBundle.message("configGroup.refresh.progressTitle")
        val task = object : Task.Backgroundable(project, progressTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                configGroups.forEach { configGroup ->
                    val gameTypeId = configGroup.gameType.id
                    val projectName = if(project.isDefault) "default project" else "project ${project.name}"

                    logger.info("Refreshing config group '$gameTypeId'...")
                    val start = System.currentTimeMillis()

                    ReadAction.nonBlocking<Unit> {
                        val newConfigGroup = doCreateConfigGroup(configGroup.gameType)
                        newConfigGroup.copyUserDataTo(configGroup)
                        configGroup.modificationTracker.incModificationCount()
                    }.expireWhen { project.isDisposed }.wrapProgress(indicator).executeSynchronously()

                    val end = System.currentTimeMillis()
                    logger.info("Refresh config group '$gameTypeId' for $projectName finished in ${end - start} ms.")
                }

                //重新解析已打开的文件
                val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
                PlsCoreManager.reparseFiles(openedFiles)
            }

            override fun onSuccess() {
                val action = NotificationAction.createSimple(PlsBundle.message("configGroup.refresh.notification.action.reindex")) {
                    //重新解析并刷新（IDE之后会自动请求重新索引）
                    //TODO 1.2.0+ 需要考虑优化 - 重新索引可能不是必要的，也可能仅需要重新索引少数几个文件
                    val rootFilePaths = getRootFilePaths(configGroups)
                    val files = PlsCoreManager.findFilesByRootFilePaths(rootFilePaths)
                    PlsCoreManager.reparseFiles(files)
                }

                run {
                    val title = PlsBundle.message("configGroup.refresh.notification.finished.title")
                    val content = PlsBundle.message("configGroup.refresh.notification.finished.content")
                    createNotification(title, content, NotificationType.INFORMATION).addAction(action).notify(project)
                }
            }

            override fun onCancel() {
                run {
                    val title = PlsBundle.message("configGroup.refresh.notification.cancelled.title")
                    createNotification(title, "", NotificationType.INFORMATION).notify(project)
                }
            }
        }
        val progressIndicator = BackgroundableProcessIndicator(task)
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, progressIndicator)
    }

    private fun doCreateConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        val configGroup = CwtConfigGroup(gameType, project)
        val dataProviders = CwtConfigGroupDataProvider.EP_NAME.extensionList
        dataProviders.all f@{ dataProvider ->
            dataProvider.process(configGroup)
        }
        return configGroup
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
}
