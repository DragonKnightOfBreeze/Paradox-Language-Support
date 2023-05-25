@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.inject.injectors

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.tree.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import icu.windea.pls.lang.*

/**
 * 渲染文件节点时，为游戏或模组根目录提供提供额外的信息文本。
 */
@InjectTarget("com.intellij.openapi.fileChooser.tree.FileRenderer")
class FileRenderCodeInjector : BaseCodeInjector() {
    //com.intellij.openapi.fileChooser.tree.FileRenderer
    //com.intellij.openapi.fileChooser.tree.FileRenderer.customize
    
    @Inject(Inject.Pointer.AFTER)
    fun Any.customize(renderer: SimpleColoredComponent, value: Any, selected: Boolean, focused: Boolean) {
        try {
            val file = when {
                value is FileNode -> value.file
                value is VirtualFile -> value
                else -> return
            }
            val rootInfo = ParadoxCoreHandler.resolveRootInfo(file)
            if(rootInfo != null && rootInfo.rootFile == file) {
                val comment = rootInfo.qualifiedName
                renderer.append(" " + comment, SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
        } catch(e: Exception) {
            thisLogger().warn(e)
        }
    }
}
