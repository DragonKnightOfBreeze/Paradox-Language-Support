@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.inject.injectors

import com.intellij.openapi.fileChooser.tree.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

/**
 * @see com.intellij.openapi.fileChooser.tree.FileRenderer
 * @see com.intellij.openapi.fileChooser.tree.FileRenderer.customize
 */
@InjectTarget("com.intellij.openapi.fileChooser.tree.FileRenderer")
class FileRenderCodeInjector : CodeInjectorBase() {
    //渲染文件节点时，为游戏或模组根目录提供提供额外的信息文本
    
    @InjectMethod(pointer = InjectMethod.Pointer.AFTER, static = true)
    fun customize(renderer: SimpleColoredComponent, value: Any, selected: Boolean, focused: Boolean) {
        doCustomizeCatching(value, renderer)
    }
    
    @InjectMethod(pointer = InjectMethod.Pointer.AFTER, static = true)
    fun customize(renderer: SimpleColoredComponent, value: Any) {
        doCustomizeCatching(value, renderer)
    }
    
    private fun doCustomizeCatching(value: Any, renderer: SimpleColoredComponent) {
        disableLogger {
            runCatchingCancelable {
                if(doCustomize(value, renderer)) return
            }
        }
    }
    
    private fun doCustomize(value: Any, renderer: SimpleColoredComponent): Boolean {
        val file = when {
            value is FileNode -> value.file
            value is VirtualFile -> value
            else -> return true
        }
        val rootInfo = file.rootInfo
        if(rootInfo != null && rootInfo.rootFile == file) {
            val comment = rootInfo.qualifiedName
            renderer.append(" $comment", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
        return false
    }
}
