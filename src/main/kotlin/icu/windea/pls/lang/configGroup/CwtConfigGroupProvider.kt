package icu.windea.pls.lang.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
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
    fun getConfigGroup(project: Project, gameType: ParadoxGameType?): CwtConfigGroup?
    
    object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupProvider>("icu.windea.pls.configGroupProvider")
    }
}

interface FileBasedCwtConfigGroupProvider : CwtConfigGroupProvider {
    override fun getConfigGroup(project: Project, gameType: ParadoxGameType?): CwtConfigGroup? {
        val cwtFiles = getCwtFiles(project, gameType) ?: return null
        return FileBasedCwtConfigGroup(project, gameType, cwtFiles)
    }
    
    fun getCwtFiles(project: Project, gameType: ParadoxGameType?): List<VirtualFile>?
}

/**
 * 用于提供插件内置的CWT规则分组。
 * 
 * 对应的规则文件位于插件包中的`config/${gameType}`目录下。
 */
class BuiltInCwtConfigGroupProvider: FileBasedCwtConfigGroupProvider {
    override fun getCwtFiles(project: Project, gameType: ParadoxGameType?): List<VirtualFile>? {
        val rootPath = "/config/${gameType.id}"
        val rootUrl = rootPath.toClasspathUrl()
        val rootDir = VfsUtil.findFileByURL(rootUrl) ?: return null
        val result = mutableListOf<VirtualFile>()
        VfsUtil.visitChildrenRecursively(rootDir, object: VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if(file.extension?.lowercase() == "cwt") {
                    result.add(file)
                }
                return true
            }
        })
        return result
    }
}

/**
 * 用于提供项目特定的本地CWT规则分组。
 * 
 * 对应的规则文件位于项目根目录中的`.config/${gameType}`目录下。
 */
class ProjectBasedCwtConfigGroupProvider: FileBasedCwtConfigGroupProvider {
    override fun getCwtFiles(project: Project, gameType: ParadoxGameType?): List<VirtualFile>? {
        val rootPath = ".config/${gameType.id}"
        val projectRootDir = project.guessProjectDir() ?: return null
        val rootDir = VfsUtil.findRelativeFile(projectRootDir, rootPath) ?: return null
        val result = mutableListOf<VirtualFile>()
        VfsUtil.visitChildrenRecursively(rootDir, object: VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if(file.extension?.lowercase() == "cwt") {
                    result.add(file)
                }
                return true
            }
        })
        return result
    }
}


interface CwtConfigGroup {
    val project: Project
    val gameType: ParadoxGameType?
}

class FileBasedCwtConfigGroup(
    override val project: Project,
    override val gameType: ParadoxGameType?,
    cwtFiles: List<VirtualFile>,
) : CwtConfigGroup
