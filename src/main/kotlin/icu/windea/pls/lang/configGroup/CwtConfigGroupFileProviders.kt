package icu.windea.pls.lang.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*

/**
 * 用于支持插件内置的CWT规则分组。
 *
 * 对应的规则文件位于插件jar包中的`config/${gameType}`目录下。
 */
class BuiltInCwtConfigGroupFileProvider : CwtConfigGroupFileProvider {
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val rootDirectory = getRootDirectory() ?: return true
        if(configGroup.name != "core") rootDirectory.findChild("core")?.let { doProcessFiles(it, consumer) }
        rootDirectory.findChild(configGroup.name)?.let { doProcessFiles(it, consumer) }
        return true
    }
    
    private fun getRootDirectory(): VirtualFile? {
        val rootPath = "/config"
        val rootUrl = rootPath.toClasspathUrl()
        return VfsUtil.findFileByURL(rootUrl)
    }
    
    private fun doProcessFiles(rootDir: VirtualFile, consumer: (String, VirtualFile) -> Boolean) {
        if(!rootDir.isDirectory) return
        withProgressIndicator {
            text = PlsBundle.message("configGroup.collectBuiltinFiles")
            text2 = ""
            isIndeterminate = true
        }
        VfsUtil.visitChildrenRecursively(rootDir, object : VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if(file.extension?.lowercase() == "cwt") {
                    val path = file.relativePathTo(rootDir)
                    consumer(path, file)
                }
                return true
            }
        })
    }
    
    override fun isChanged(configGroup: CwtConfigGroup, filePaths: Set<String>): Boolean {
        return false
    }
}

/**
 * 用于提供项目特定的本地CWT规则分组。
 *
 * 对应的规则文件位于项目根目录中的`.config/${gameType}`目录下。
 */
class ProjectCwtConfigGroupFileProvider : CwtConfigGroupFileProvider {
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val rootDirectory = getRootDirectory(configGroup) ?: return true
        if(configGroup.name != "core") rootDirectory.findChild("core")?.let { doProcessFiles(it, consumer) }
        rootDirectory.findChild(configGroup.name)?.let { doProcessFiles(it, consumer) }
        return true
    }
    
    private fun getRootDirectory(configGroup: CwtConfigGroup): VirtualFile? {
        val projectRootDir = configGroup.project.guessProjectDir() ?: return null
        val rootPath = ".config"
        return VfsUtil.findRelativeFile(projectRootDir, rootPath)
    }
    
    private fun doProcessFiles(rootDir: VirtualFile, consumer: (String, VirtualFile) -> Boolean) {
        if(!rootDir.isDirectory) return
        withProgressIndicator {
            text = PlsBundle.message("configGroup.collectFiles", rootDir.path)
            text2 = ""
            isIndeterminate = true
        }
        VfsUtil.visitChildrenRecursively(rootDir, object : VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if(file.extension?.lowercase() == "cwt") {
                    val path = file.relativePathTo(rootDir)
                    consumer(path, file)
                }
                return true
            }
        })
    }
    
    override fun isChanged(configGroup: CwtConfigGroup, filePaths: Set<String>): Boolean {
        val rootDirectory = getRootDirectory(configGroup) ?: return false
        val rootPath = rootDirectory.path
        return filePaths.any { filePath -> rootPath.matchesPath(filePath) }
    }
}

