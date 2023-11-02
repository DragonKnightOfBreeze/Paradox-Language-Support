package icu.windea.pls.lang.configGroup

import com.google.common.cache.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

/**
 * 用于提供CWT规则分组。
 */
@Service(Service.Level.PROJECT)
class CwtConfigGroupProvider(
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
        //按照文件路径（相对于规则文件的根目录）正序读取所有规则文件
        //后加入的规则文件会覆盖先加入的同路径的规则文件
        //后加入的数据项会覆盖先加入的同名同类型的数据项
        
        val supports = CwtConfigGroupSupport.EP_NAME.extensionList
        
        supports.all f@{ support ->
            if(support is FileBasedCwtConfigGroupSupport) return@f true
            if(support is PostCwtConfigGroupSupport) return@f true
            support.process(configGroup)
        }
        
        val allFilesAndSupports = mutableMapOf<String, Tuple2<VirtualFile, FileBasedCwtConfigGroupSupport>>()
        
        supports.all f@{ support ->
            if(support !is FileBasedCwtConfigGroupSupport) return@f true
            support.processFiles(configGroup) { path, file ->
                allFilesAndSupports[path] = tupleOf(file, support)
                true
            }
        }
        
        allFilesAndSupports.values.all f@{ (file, ep) ->
            ep.processFile(file, configGroup)
        }
        
        supports.all f@{ support ->
            if(support !is PostCwtConfigGroupSupport) return@f true
            support.process(configGroup)
        }
    }
}
