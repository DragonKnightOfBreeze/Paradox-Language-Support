@file:Suppress("UnstableApiUsage", "unused")

package icu.windea.pls.inject.injectors

import com.intellij.openapi.fileChooser.tree.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

/**
 * @see com.intellij.openapi.fileChooser.tree.FileRenderer
 * @see com.intellij.openapi.fileChooser.tree.FileRenderer.customize
 */
@InjectTarget("com.intellij.openapi.fileChooser.tree.FileRenderer")
class FileRenderCodeInjector : CodeInjectorBase() {
    //渲染文件节点时，为游戏或模组目录提供提供额外的信息文本

    @InjectMethod(pointer = InjectMethod.Pointer.AFTER, static = true)
    fun customize(renderer: SimpleColoredComponent, value: Any) {
        runCatchingCancelable {
            if (doCustomize(renderer, value)) return
        }
    }

    private fun doCustomize(renderer: SimpleColoredComponent, value: Any): Boolean {
        val file = when {
            value is FileNode -> value.file
            value is VirtualFile -> value
            else -> return true
        }
        val rootInfo = file.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return false
        if (rootInfo.rootFile != file) return false

        val comment = rootInfo.qualifiedName
        renderer.append(" $comment", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        return false
    }
}
