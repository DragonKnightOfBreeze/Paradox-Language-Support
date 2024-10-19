package icu.windea.pls.ep.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

fun CwtConfigGroupFileProvider.isBuiltIn(): Boolean {
    return this is BuiltInCwtConfigGroupFileProvider
}

abstract class CwtConfigGroupFileProviderBase : CwtConfigGroupFileProvider {
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
 * 用于提供插件内置的规则分组。
 *
 * 对应的路径：`config/${gameType}`（位于插件jar包中）
 */
class BuiltInCwtConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override fun getRootDirectory(project: Project): VirtualFile? {
        val rootPath = "/config"
        val rootUrl = rootPath.toClasspathUrl()
        val file = VfsUtil.findFileByURL(rootUrl)
        return file?.takeIf { it.isDirectory }
    }
    
    override fun getHintMessage() = PlsBundle.message("configGroup.hint.1")
    
    override fun getNotificationMessage() = PlsBundle.message("configGroup.notification.1")
}

/**
 * 用于提供全局的本地规则分组。
 *
 * 对应的路径：`{rootPath}/{gameType}`[^1]（可在插件的配置页面中配置`rootPath`对应的文件路径）
 */
class LocalCwtConfigGroupFileProvider: CwtConfigGroupFileProviderBase() {
    override fun getRootDirectory(project: Project): VirtualFile? {
        val directory = getSettings().localConfigDirectory
        val absoluteDirectory = directory?.normalizePath()?.orNull() ?: return null
        val path = absoluteDirectory.toPathOrNull() ?: return null
        val file = VfsUtil.findFile(path, true)
        return file?.takeIf { it.isDirectory }
    }
    
    override fun getHintMessage() = PlsBundle.message("configGroup.hint.2")
    
    override fun getNotificationMessage() = PlsBundle.message("configGroup.notification.2")
}

/**
 * 用于提供项目的本地规则分组。
 *
 * 对应的路径：`.config/${gameType}`（位于项目根目录中）
 */
class ProjectCwtConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override fun getRootDirectory(project: Project): VirtualFile? {
        val projectRootDirectory = project.guessProjectDir() ?: return null
        val rootPath = ".config"
        val file = VfsUtil.findRelativeFile(projectRootDirectory, rootPath)
        return file?.takeIf { it.isDirectory }
    }
    
    override fun getHintMessage() = PlsBundle.message("configGroup.hint.3")
    
    override fun getNotificationMessage() = PlsBundle.message("configGroup.notification.3")
}

