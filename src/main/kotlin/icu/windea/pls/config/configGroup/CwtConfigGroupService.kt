package icu.windea.pls.config.configGroup

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.progress.impl.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import java.util.concurrent.*

@Service(Service.Level.PROJECT)
class CwtConfigGroupService(
    val project: Project
) {
    private val logger = logger<CwtConfigGroupService>()
    private val cache = ConcurrentHashMap<String, CwtConfigGroup>()
    
    fun getConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        return cache.getOrPut(gameType.id) { createConfigGroup(gameType) }
    }
    
    fun getConfigGroups(): Map<String, CwtConfigGroup> {
        return cache
    }
    
    fun createConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        val gameTypeId = gameType.id
        
        logger.info("Initialize CWT config group '$gameTypeId'...")
        val start = System.currentTimeMillis()
        
        val configGroup = createConfigGroupInProgress(gameType)
        
        val end = System.currentTimeMillis()
        logger.info("Initialize CWT config group '$gameTypeId' finished in ${end - start} ms.")
        
        return configGroup
    }
    
    @Synchronized
    fun refreshConfigGroups(configGroups: Collection<CwtConfigGroup>) {
        //不替换configGroup，而是替换其中的userData
        if(configGroups.isEmpty()) return
        val progressTitle = PlsBundle.message("configGroup.refresh.progressTitle")
        val task = object : Task.Backgroundable(project, progressTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                configGroups.forEach { configGroup ->
                    val gameTypeId = configGroup.gameType.id
                    
                    logger.info("Refresh CWT config group '$gameTypeId'...")
                    val start = System.currentTimeMillis()
                    
                    ReadAction.nonBlocking(Callable {
                        val newConfigGroup = createConfigGroupInProgress(configGroup.gameType)
                        newConfigGroup.copyUserDataTo(configGroup)
                        configGroup.modificationTracker.incModificationCount()
                    }).expireWhen { project.isDisposed }.wrapProgress(indicator).executeSynchronously()
                    
                    val end = System.currentTimeMillis()
                    logger.info("Refresh CWT config group '$gameTypeId' finished in ${end - start} ms.")
                }
                
                //重新解析已打开的文件
                val openedFiles = ParadoxCoreHandler.findOpenedFiles()
                ParadoxCoreHandler.reparseFiles(openedFiles)
            }
            
            override fun onSuccess() {
                val action = NotificationAction.createSimple(PlsBundle.message("configGroup.refresh.notification.action.reindex")) {
                    //重新解析文件（IDE之后会自动请求重新索引）
                    //TODO 1.2.0+ 需要考虑优化 - 重新索引可能不是必要的，也可能仅需要重新索引少数几个文件
                    val rootFilePaths = getRootFilePaths(configGroups)
                    val files = ParadoxCoreHandler.findFilesByRootFilePaths(rootFilePaths)
                    ParadoxCoreHandler.reparseFiles(files)
                }
                
                NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
                    PlsBundle.message("configGroup.refresh.notification.finished.title"),
                    PlsBundle.message("configGroup.refresh.notification.finished.content"),
                    NotificationType.INFORMATION
                ).addAction(action).notify(project)
            }
            
            override fun onCancel() {
                NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
                    PlsBundle.message("configGroup.refresh.notification.cancelled.title"),
                    "",
                    NotificationType.INFORMATION
                ).notify(project)
            }
        }
        val progressIndicator = BackgroundableProcessIndicator(task)
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, progressIndicator)
    }
    
    private fun createConfigGroupInProgress(gameType: ParadoxGameType?): CwtConfigGroup {
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
        getProfilesSettings().gameDescriptorSettings.values.forEach f@{ settings ->
            val gameType = settings.gameType ?: return@f
            if(gameType !in gameTypes) return@f
            settings.gameDirectory?.let { rootFilePaths.add(it) }
        }
        getProfilesSettings().modDescriptorSettings.values.forEach f@{ settings ->
            val gameType = settings.finalGameType
            if(gameType !in gameTypes) return@f
            settings.modDirectory?.let { rootFilePaths.add(it) }
        }
        return rootFilePaths
    }
}
