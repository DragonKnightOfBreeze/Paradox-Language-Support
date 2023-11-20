package icu.windea.pls.lang.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.model.*

/**
 * 用于支持插件内置的CWT规则分组。
 *
 * 对应的规则文件位于插件jar包中的`config/${gameType}`目录下。
 */
class BuiltInCwtConfigGroupFileProvider : CwtConfigGroupFileProvider {
    override fun getRootDirectories(project: Project): Set<VirtualFile> {
        val rootPath = "/config"
        val rootUrl = rootPath.toClasspathUrl()
        return VfsUtil.findFileByURL(rootUrl).toSingletonSetOrEmpty()
    }
    
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val gameTypeId = configGroup.gameType.id
        val rootDirectories = getRootDirectories(configGroup.project)
        rootDirectories.forEach { rootDirectory ->
            if(gameTypeId != "core") rootDirectory.findChild("core")?.let { doProcessFiles(it, configGroup, consumer) }
            rootDirectory.findChild(gameTypeId)?.let { doProcessFiles(it, configGroup, consumer) }
        }
        return true
    }
    
    private fun doProcessFiles(rootDirectory: VirtualFile, configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean) {
        if(!rootDirectory.isDirectory) return
        val gameTypeId = configGroup.gameType.id
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
    
    override fun getConfigGroups(project: Project, file: VirtualFile): Set<CwtConfigGroup> {
        val rootDirectories = getRootDirectories(project)
        val relativePath = rootDirectories.firstNotNullOfOrNull { VfsUtil.getRelativePath(file, it) } ?: return emptySet()
        val gameTypeId = relativePath.substringBefore('/')
        if(gameTypeId == "core") {
            val configGroups = mutableSetOf<CwtConfigGroup>()
            configGroups += getConfigGroup(project, null)
            ParadoxGameType.values.forEach { gameType ->
                configGroups += getConfigGroup(project, gameType)
            }
            return configGroups
        } else {
            val gameType = ParadoxGameType.resolve(gameTypeId) ?: return emptySet()
            val configGroup = getConfigGroup(project, gameType)
            return configGroup.toSingletonSet()
        }
    }
}

/**
 * 用于提供项目特定的本地CWT规则分组。
 *
 * 对应的规则文件位于项目根目录中的`.config/${gameType}`目录下。
 */
class ProjectCwtConfigGroupFileProvider : CwtConfigGroupFileProvider {
    override fun getRootDirectories(project: Project): Set<VirtualFile> {
        val projectRootDirectory = project.guessProjectDir() ?: return emptySet()
        val rootPath = ".config"
        return VfsUtil.findRelativeFile(projectRootDirectory, rootPath).toSingletonSetOrEmpty()
    }
    
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val gameTypeId = configGroup.gameType.id
        val rootDirectories = getRootDirectories(configGroup.project)
        rootDirectories.forEach { rootDirectory ->
            if(gameTypeId != "core") rootDirectory.findChild("core")?.let { doProcessFiles(it, configGroup, consumer) }
            rootDirectory.findChild(gameTypeId)?.let { doProcessFiles(it, configGroup, consumer) }
        }
        return true
    }
    
    private fun doProcessFiles(rootDirectory: VirtualFile, configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean) {
        if(!rootDirectory.isDirectory) return
        val gameTypeId = configGroup.gameType.id
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
    
    override fun getConfigGroups(project: Project, file: VirtualFile): Set<CwtConfigGroup> {
        val rootDirectories = getRootDirectories(project)
        val relativePath = rootDirectories.firstNotNullOfOrNull { VfsUtil.getRelativePath(file, it) } ?: return emptySet()
        val gameTypeId = relativePath.substringBefore('/')
        if(gameTypeId == "core") {
            val configGroups = mutableSetOf<CwtConfigGroup>()
            configGroups += getConfigGroup(project, null)
            ParadoxGameType.values.forEach { gameType ->
                configGroups += getConfigGroup(project, gameType)
            }
            return configGroups
        } else {
            val gameType = ParadoxGameType.resolve(gameTypeId) ?: return emptySet()
            val configGroup = getConfigGroup(project, gameType)
            return configGroup.toSingletonSet()
        }
    }
}

