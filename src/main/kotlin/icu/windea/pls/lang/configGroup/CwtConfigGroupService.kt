package icu.windea.pls.lang.configGroup

import com.google.common.cache.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

@Service(Service.Level.PROJECT)
class CwtConfigGroupService(
    private val project: Project
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
        val supports = CwtConfigGroupDataProvider.EP_NAME.extensionList
        supports.all f@{ support ->
            support.process(configGroup)
        }
    }
}
