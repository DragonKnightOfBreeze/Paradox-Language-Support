package icu.windea.pls.lang.actions

import com.intellij.ide.actions.DumbAwareCopyPathProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.fileInfo

/**
 * 复制相对于（游戏或模组的）的入口目录的路径到剪贴板。
 */
class CopyPathFromEntryProvider : DumbAwareCopyPathProvider() {
    override fun getPathToElement(project: Project, virtualFile: VirtualFile?, editor: Editor?): String? {
        val fileInfo = virtualFile?.fileInfo ?: return null
        val path = fileInfo.path.path
        val result = path
        if (result.isEmpty()) return null // 排除入口目录本身
        return result
    }
}
