package icu.windea.pls.lang.configGroup

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.wm.ex.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import java.util.concurrent.*

@Service(Service.Level.PROJECT)
class CwtConfigGroupService(
    val project: Project
) {
    private val cache = ConcurrentHashMap<String, CwtConfigGroup>()
    
    fun getConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        return cache.computeIfAbsent(gameType.id) { createConfigGroup(gameType, false) }
    }
    
    fun getConfigGroups(): Map<String, CwtConfigGroup> {
        return cache
    }
    
    fun refreshConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        //不替换configGroup，而是替换其中的userData
        val configGroup = cache.computeIfAbsent(gameType.id) { createConfigGroup(gameType, false) }
        if(!configGroup.changed.get()) return configGroup
        synchronized(configGroup) {
            if(!configGroup.changed.get()) return configGroup
            val newConfigGroup = createConfigGroup(gameType, true)
            newConfigGroup.copyUserDataTo(configGroup)
            newConfigGroup.changed.set(false)
            newConfigGroup.modificationTracker.incModificationCount()
        }
        return configGroup
    }
    
    private fun createConfigGroup(gameType: ParadoxGameType?, refresh: Boolean): CwtConfigGroup {
        val info = CwtConfigGroupInfo(gameType.id)
        val configGroup = CwtConfigGroup(info, gameType, project)
        info.configGroup = configGroup
        handleConfigGroup(configGroup, refresh)
        return configGroup
    }
    
    private fun handleConfigGroup(configGroup: CwtConfigGroup, refresh: Boolean) {
        //需要考虑在背景进度指示器中显示初始化的进度（经测试，通常1s内即可完成）
        
        val name = configGroup.name
        if(refresh) {
            thisLogger().info("Refresh CWT config group '$name'...")
        } else {
            thisLogger().info("Initialize CWT config group '$name'...")
        }
        val start = System.currentTimeMillis()
        
        if(configGroup.name != "core" && !configGroup.project.isDefault) {
            val progressTitle = if(refresh) {
                PlsBundle.message("configGroup.refresh", name)
            } else {
                PlsBundle.message("configGroup.init", name)
            }
            configGroup.progressIndicator = BackgroundableProcessIndicator(project, progressTitle, null, "", false)
        }
        val callable = Callable {
            try {
                val dataProviders = CwtConfigGroupDataProvider.EP_NAME.extensionList
                dataProviders.all f@{ dataProvider ->
                    dataProvider.process(configGroup)
                }
            } finally {
                configGroup.progressIndicator?.castOrNull<ProgressIndicatorEx>()?.processFinish()
                configGroup.progressIndicator = null
            }
        }
        var action = ReadAction.nonBlocking(callable).expireWhen { project.isDisposed }
        configGroup.progressIndicator?.apply { action = action.wrapProgress(this) }
        action.executeSynchronously()
        
        val end = System.currentTimeMillis()
        if(refresh) {
            thisLogger().info("Refresh CWT config group '$name' finished in ${end - start} ms.")
        } else {
            thisLogger().info("Initialize CWT config group '$name' finished in ${end - start} ms.")
        }
    }
}
