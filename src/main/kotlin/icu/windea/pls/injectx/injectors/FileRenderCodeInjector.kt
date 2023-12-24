@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.injectx.injectors

import com.intellij.openapi.fileChooser.tree.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.injectx.*
import icu.windea.pls.injectx.annotations.*
import net.bytebuddy.implementation.bind.annotation.*
import java.util.concurrent.*

/**
 * 渲染文件节点时，为游戏或模组根目录提供提供额外的信息文本。
 * 
 * @see com.intellij.openapi.fileChooser.tree.FileRenderer
 * @see com.intellij.openapi.fileChooser.tree.FileRenderer.customize
 */
@Inject("com.intellij.openapi.fileChooser.tree.FileRenderer")
open class FileRenderCodeInjector: CodeInjector {
    @InjectMethod
    @RuntimeType
    fun customize(renderer: SimpleColoredComponent, value: Any, @SuperCall methodCall: Callable<Unit>) {
        methodCall.call()
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