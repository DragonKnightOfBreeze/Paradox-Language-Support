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
        val rootDirectories = getRootDirectories(configGroup.project)
        rootDirectories.forEach { rootDirectory ->
            if(configGroup.name != "core") rootDirectory.findChild("core")?.let { doProcessFiles(it, configGroup, consumer) }
            rootDirectory.findChild(configGroup.name)?.let { doProcessFiles(it, configGroup, consumer) }
        }
        return true
    }
    
    private fun doProcessFiles(rootDirectory: VirtualFile, configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean) {
        if(!rootDirectory.isDirectory) return
        configGroup.progressIndicator?.apply {
            text = PlsBundle.message("configGroup.collectBuiltinFiles")
            text2 = ""
            isIndeterminate = true
        }
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
    
    override fun onFileChange(project: Project, file: VirtualFile): Boolean {
        return false
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
        val rootDirectories = getRootDirectories(configGroup.project)
        rootDirectories.forEach { rootDirectory ->
            if(configGroup.name != "core") rootDirectory.findChild("core")?.let { doProcessFiles(it, configGroup, consumer) }
            rootDirectory.findChild(configGroup.name)?.let { doProcessFiles(it, configGroup, consumer) }
        }
        return true
    }
    
    private fun doProcessFiles(rootDirectory: VirtualFile, configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean) {
        if(!rootDirectory.isDirectory) return
        configGroup.progressIndicator?.apply {
            text = PlsBundle.message("configGroup.collectFiles", rootDirectory.presentableUrl)
            text2 = ""
            isIndeterminate = true
        }
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
    
    override fun onFileChange(project: Project, file: VirtualFile): Boolean {
        val rootDirectories = getRootDirectories(project)
        val relativePath = rootDirectories.firstNotNullOfOrNull { VfsUtil.getRelativePath(file, it) } ?: return false
        val gameTypeId = relativePath.substringBefore('/')
        if(gameTypeId == "core") {
            getConfigGroup(project, null).changed.set(true)
            ParadoxGameType.values.forEach { gameType ->
                getConfigGroup(project, gameType).changed.set(true)
            }
            return true
        } else {
            val gameType = ParadoxGameType.resolve(gameTypeId) ?: return false
            getConfigGroup(project, gameType).changed.set(true)
            return true
        }
    }
}

