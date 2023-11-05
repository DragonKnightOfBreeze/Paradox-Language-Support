package icu.windea.pls.lang.configGroup

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.progress.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.wm.ex.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.model.*
import java.util.concurrent.*

@Service(Service.Level.PROJECT)
class CwtConfigGroupService(
    val project: Project
) {
    private val cache = ConcurrentHashMap<String, CwtConfigGroup>()
    
    fun getConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        return cache.computeIfAbsent(gameType.id) { createConfigGroup(gameType) }
    }
    
    fun getConfigGroups(): Map<String, CwtConfigGroup> {
        return cache
    }
    
    fun refreshConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        //不替换configGroup，而是替换其中的userData
        val configGroup = cache.computeIfAbsent(gameType.id) { createConfigGroup(gameType) }
        if(!configGroup.changed.get()) return configGroup
        val newConfigGroup = createConfigGroup(gameType)
        newConfigGroup.copyUserDataTo(configGroup)
        newConfigGroup.modificationTracker.incModificationCount()
        return configGroup
    }
    
    private fun createConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        val info = CwtConfigGroupInfo(gameType.id)
        val configGroup = CwtConfigGroup(info, gameType, project)
        info.configGroup = configGroup
        initConfigGroup(configGroup)
        return configGroup
    }
    
    private fun initConfigGroup(configGroup: CwtConfigGroup) {
        synchronized(configGroup) {
            doInitConfigGroup(configGroup)
        }
    }
    
    private fun doInitConfigGroup(configGroup: CwtConfigGroup) {
        //需要考虑在背景进度指示器中显示初始化的进度（经测试，通常1s内即可完成）
        
        val name = configGroup.name
        thisLogger().info("Initialize CWT config group '$name'...")
        val start = System.currentTimeMillis()
        
        val processIndicator = when {
            configGroup.name == "core" || configGroup.project.isDefault -> EmptyProgressIndicator()
            else -> BackgroundableProcessIndicator(project, PlsBundle.message("configGroup.init", name), null, "", false)
        }
        val callable = Callable {
            val dataProviders = CwtConfigGroupDataProvider.EP_NAME.extensionList
            dataProviders.all f@{ dataProvider ->
                dataProvider.process(configGroup)
            }.also {
                if(processIndicator is ProgressIndicatorEx) processIndicator.processFinish()
            }
        }
        val action = ReadAction.nonBlocking(callable).expireWhen { project.isDisposed }.wrapProgress(processIndicator)
        action.executeSynchronously()
        
        val end = System.currentTimeMillis()
        thisLogger().info("Initialize CWT config group '$name' finished in ${end - start} ms.")
    }
}
