@file:Suppress("UnstableApiUsage", "unused")

package icu.windea.pls.inject.injectors

import com.intellij.openapi.fileChooser.tree.FileNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.model.ParadoxRootInfo

/**
 * @see com.intellij.openapi.fileChooser.tree.FileRenderer
 * @see com.intellij.openapi.fileChooser.tree.FileRenderer.customize
 */
@InjectionTarget("com.intellij.openapi.fileChooser.tree.FileRenderer")
class FileRenderCodeInjector : CodeInjectorBase() {
    // 渲染文件节点时，为游戏或模组目录提供提供额外的信息文本

    @InjectMethod(pointer = InjectMethod.Pointer.AFTER, static = true)
    fun customize(renderer: SimpleColoredComponent, value: Any) {
        runSafely {
            appendQualifiedName(renderer, value)
        }
    }

    private fun appendQualifiedName(renderer: SimpleColoredComponent, value: Any) {
        val file = when {
            value is FileNode -> value.file
            value is VirtualFile -> value
            else -> return
        }
        val rootInfo = file.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return
        if (rootInfo.rootFile != file) return

        val comment = rootInfo.qualifiedName
        renderer.append(" $comment", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        return
    }
}
