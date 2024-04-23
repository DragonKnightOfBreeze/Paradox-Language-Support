package icu.windea.pls.ep.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

abstract class CwtConfigGroupFileProviderBase: CwtConfigGroupFileProvider {
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val gameTypeId = configGroup.gameType.id
        val rootDirectory = getRootDirectory(configGroup.project) ?: return true
        if(gameTypeId != "core") rootDirectory.findChild("core")?.let { doProcessFiles(it, consumer) }
        rootDirectory.findChild(gameTypeId)?.let { doProcessFiles(it, consumer) }
        return true
    }
    
    private fun doProcessFiles(rootDirectory: VirtualFile, consumer: (String, VirtualFile) -> Boolean) {
        if(!rootDirectory.isDirectory) return
        VfsUtil.visitChildrenRecursively(rootDirectory, object : VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if(file.extension?.lowercase() == "cwt") {
                    val path = VfsUtil.getRelativePath(file, rootDirectory) ?: return true
                    consumer(path, file)
                }
                return true
            }
        })
    }
    
    override fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup? {
        val rootDirectory = getRootDirectory(project) ?: return null
        val relativePath = VfsUtil.getRelativePath(file, rootDirectory) ?: return null
        val gameTypeId = relativePath.substringBefore('/')
        if(gameTypeId == "core") {
            return getConfigGroup(project, null)
        } else {
            val gameType = ParadoxGameType.resolve(gameTypeId) ?: return null
            return getConfigGroup(project, gameType)
        }
    }
}

/**
 * 用于支持插件内置的CWT规则分组。
 *
 * 对应的规则文件位于插件jar包中的`config/${gameType}`目录下。
 */
class BuiltInCwtConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override fun isBuiltIn(): Boolean {
        return true
    }
    
    override fun getRootDirectory(project: Project): VirtualFile? {
        val rootPath = "/config"
        val rootUrl = rootPath.toClasspathUrl()
        return VfsUtil.findFileByURL(rootUrl)
    }
}

/**
 * 用于提供项目特定的本地CWT规则分组。
 *
 * 对应的规则文件位于项目根目录中的`.config/${gameType}`目录下。
 */
class ProjectCwtConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override fun getRootDirectory(project: Project): VirtualFile? {
        val projectRootDirectory = project.guessProjectDir() ?: return null
        val rootPath = ".config"
        return VfsUtil.findRelativeFile(projectRootDirectory, rootPath)
    }
}

