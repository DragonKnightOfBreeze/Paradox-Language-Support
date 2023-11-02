package icu.windea.pls.lang.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

/**
 * 用于提供插件内置的CWT规则分组。
 * 
 * 对应的规则文件位于插件包中的`config/${gameType}`目录下。
 */
class BuiltInCwtConfigGroupProvider: FileBasedCwtConfigGroupProvider {
    override fun getCwtConfigFiles(project: Project, gameType: ParadoxGameType?): List<VirtualFile> {
        val rootPath = "/config/${gameType.id}"
        val rootUrl = rootPath.toClasspathUrl()
        val rootDir = VfsUtil.findFileByURL(rootUrl) ?: return emptyList()
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