package icu.windea.pls.lang.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

/**
 * 用于提供CWT规则分组。
 */
interface CwtConfigGroupProvider {
    /**
     * 得到规则分组。
     * @param gameType 对应的游戏类型。如果为null，则会得到共用的核心规则分组。
     * @param project 对应的项目。如果不需要访问PSI，可以直接传入默认项目。
     */
    fun getConfigGroup(gameType: ParadoxGameType?, project: Project): CwtConfigGroup
    
    object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupProvider>("icu.windea.pls.configGroupProvider")
    }
}

interface CwtConfigGroup : UserDataHolder {
    val gameType: ParadoxGameType?
    val project: Project
    
    object Keys : KeyHolder
}

val CwtConfigGroup.Keys.provider by createKey<CwtConfigGroupProvider>("cwt.configGroup.provider")

var CwtConfigGroup.provider by CwtConfigGroup.Keys.provider

interface FileBasedCwtConfigGroupProvider : CwtConfigGroupProvider {
    override fun getConfigGroup(gameType: ParadoxGameType?, project: Project): CwtConfigGroup {
        return FileBasedCwtConfigGroup(gameType, project)
    }
    
    fun getCwtConfigFiles(project: Project, gameType: ParadoxGameType?): List<VirtualFile>
}

class FileBasedCwtConfigGroup(
    override val gameType: ParadoxGameType?,
    override val project: Project,
) : UserDataHolderBase(), CwtConfigGroup