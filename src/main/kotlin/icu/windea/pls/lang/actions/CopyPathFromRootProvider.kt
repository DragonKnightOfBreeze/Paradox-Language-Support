package icu.windea.pls.lang.actions

import com.intellij.ide.actions.DumbAwareCopyPathProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.fileInfo

class CopyPathFromRootProvider : DumbAwareCopyPathProvider() {
    //仅限游戏或模组文件

    override fun getPathToElement(project: Project, virtualFile: VirtualFile?, editor: Editor?): String? {
        val fileInfo = virtualFile?.fileInfo ?: return null
        val path = fileInfo.path.path
        if (path.isEmpty()) return null //排除根目录本身
        return path
    }
}
