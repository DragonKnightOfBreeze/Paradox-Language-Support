package icu.windea.pls.lang.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

/**
 * 用于支持基于CWT规则文件的CWT规则分组。
 */
abstract class FileBasedCwtConfigGroupSupport: CwtConfigGroupSupportBase() {
    override fun process(configGroup: CwtConfigGroup): Boolean {
        //do nothing
        return true
    }
    
    abstract fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean
    
    fun processFile(file: VirtualFile, configGroup: CwtConfigGroup): Boolean {
        //TODO
        return true
    }
}

/**
 * 用于支持插件内置的CWT规则分组。
 *
 * 对应的规则文件位于插件jar包中的`config/${gameType}`目录下。
 */
class BuiltInCwtConfigGroupSupport: FileBasedCwtConfigGroupSupport() {
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val rootPath = "/config/${configGroup.gameType.id}"
        val rootUrl = rootPath.toClasspathUrl()
        val rootDir = VfsUtil.findFileByURL(rootUrl) ?: return true
        VfsUtil.visitChildrenRecursively(rootDir, object: VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if(file.extension?.lowercase() == "cwt") {
                    val path = file.relativePathTo(rootDir)
                    consumer(path, file)
                }
                return true
            }
        })
        return true
    }
}

/**
 * 用于提供项目特定的本地CWT规则分组。
 *
 * 对应的规则文件位于项目根目录中的`.config/${gameType}`目录下。
 */
class ProjectBasedCwtConfigGroupSupport: FileBasedCwtConfigGroupSupport() {
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val projectRootDir = configGroup.project.guessProjectDir() ?: return true
        val rootPath = ".config/${configGroup.gameType.id}"
        val rootDir = VfsUtil.findRelativeFile(projectRootDir, rootPath) ?: return true
        VfsUtil.visitChildrenRecursively(rootDir, object: VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if(file.extension?.lowercase() == "cwt") {
                    val path = file.relativePathTo(rootDir)
                    consumer(path, file)
                }
                return true
            }
        })
        return true
    }
}
