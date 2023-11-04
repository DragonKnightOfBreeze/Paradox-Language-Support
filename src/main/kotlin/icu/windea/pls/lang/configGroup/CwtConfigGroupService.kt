package icu.windea.pls.lang.configGroup

import com.google.common.cache.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.impl.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*
import java.util.concurrent.*

@Service(Service.Level.PROJECT)
class CwtConfigGroupService(
    val project: Project
) {
    val cache = CacheBuilder.newBuilder().buildCache<String, CwtConfigGroup> { createConfigGroup(ParadoxGameType.resolve(it)) }
    
    fun getConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        return cache.getCancelable(gameType.id)
    }
    
    fun refreshConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        //不替换configGroup，而是替换其中的userData
        val newConfigGroup = createConfigGroup(gameType)
        val configGroup = cache.getCancelable(gameType.id)
        newConfigGroup.copyUserDataTo(configGroup)
        return configGroup
    }
    
    private fun createConfigGroup(gameType: ParadoxGameType?): CwtConfigGroup {
        val info = CwtConfigGroupInfo(gameType.id)
        val configGroup = CwtConfigGroup(info, gameType, project)
        initConfigGroup(configGroup)
        return configGroup
    }
    
    private fun initConfigGroup(configGroup: CwtConfigGroup) {
        //需要在背景进度指示器中显示初始化的进度
        
        val name = configGroup.name
        thisLogger().info("Initialize CWT config group '$name'...")
        val start = System.currentTimeMillis()
        
        val indicator = BackgroundableProcessIndicator(project, PlsBundle.message("configGroup.init", name), null, "", false)
        val callable =  Callable {
            val dataProviders = CwtConfigGroupDataProvider.EP_NAME.extensionList
            dataProviders.all f@{ dataProvider ->
                dataProvider.process(configGroup)
            }
        }
        ReadAction.nonBlocking(callable).wrapProgress(indicator).expireWhen { project.isDisposed }.executeSynchronously()
        
        val end = System.currentTimeMillis()
        thisLogger().info("Initialize CWT config group '$name' finished in ${end - start} ms.")
    }
}
