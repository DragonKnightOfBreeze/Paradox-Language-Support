package icu.windea.pls.lang.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.listeners.vfs.*
import icu.windea.pls.model.*

/**
 * 用于提供项目特定的本地CWT规则分组。
 * 
 * 对应的规则文件位于项目根目录中的`.config/${gameType}`目录下。
 * 
 * @see ProjectBasedCwtConfigFileListener
 */
class ProjectBasedCwtConfigGroupProvider: FileBasedCwtConfigGroupProvider {
    override fun getCwtConfigFiles(project: Project, gameType: ParadoxGameType?): List<VirtualFile> {
        val projectRootDir = project.guessProjectDir() ?: return emptyList()
        val rootPath = ".config/${gameType.id}"
        val rootDir = VfsUtil.findRelativeFile(projectRootDir, rootPath) ?: return emptyList()
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