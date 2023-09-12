package icu.windea.pls.lang.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

/**
 * 用于提供CWT规则分组。
 */
interface CwtConfigGroupProvider {
    /**
     * 得到规则分组。
     * @param project 对应的项目。如果不需要访问PSI，可以直接传入默认项目。
     * @param gameType 对应的游戏类型。如果为null，则会得到共用的核心规则分组。
     */
    fun getConfigGroup(project: Project, gameType: ParadoxGameType?): CwtConfigGroup
    
    /**
     * 用于追踪CWT规则分组的更改。
     */
    fun getModificationTracker(project: Project, gameType: ParadoxGameType?): ModificationTracker
    
    object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupProvider>("icu.windea.pls.configGroupProvider")
        
        
    }
}

interface FileBasedCwtConfigGroupProvider : CwtConfigGroupProvider {
    override fun getConfigGroup(project: Project, gameType: ParadoxGameType?): CwtConfigGroup {
        return FileBasedCwtConfigGroup(project, gameType, this)
    }
    
    fun getCwtConfigFiles(project: Project, gameType: ParadoxGameType?): List<VirtualFile>
}


interface CwtConfigGroup {
    val project: Project
    val gameType: ParadoxGameType?
    val provider: CwtConfigGroupProvider
}

class FileBasedCwtConfigGroup(
    override val project: Project,
    override val gameType: ParadoxGameType?,
    override val provider: FileBasedCwtConfigGroupProvider,
) : CwtConfigGroup
