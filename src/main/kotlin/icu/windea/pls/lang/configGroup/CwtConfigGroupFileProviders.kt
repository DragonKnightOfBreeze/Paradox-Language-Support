package icu.windea.pls.lang.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

/**
 * 用于支持插件内置的CWT规则分组。
 *
 * 对应的规则文件位于插件jar包中的`config/${gameType}`目录下。
 */
class BuiltInCwtConfigGroupFileProvider: CwtConfigGroupFileProvider {
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val rootPath = "/config/${configGroup.gameType.id}"
        val rootUrl = rootPath.toClasspathUrl()
        val rootDir = VfsUtil.findFileByURL(rootUrl) ?: return true
        withProgressIndicator {
            text = PlsBundle.message("configGroup.collectBuiltinFiles")
            text2 = ""
            isIndeterminate = true
        }
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
class ProjectCwtConfigGroupFileProvider: CwtConfigGroupFileProvider {
    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val projectRootDir = configGroup.project.guessProjectDir() ?: return true
        val rootPath = ".config/${configGroup.gameType.id}"
        val rootDir = VfsUtil.findRelativeFile(projectRootDir, rootPath) ?: return true
        withProgressIndicator {
            text = PlsBundle.message("configGroup.collectFiles", rootPath)
            text2 = ""
            isIndeterminate = true
        }
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