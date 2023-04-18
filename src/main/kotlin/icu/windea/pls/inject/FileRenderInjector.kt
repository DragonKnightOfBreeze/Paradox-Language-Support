package icu.windea.pls.inject

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.tree.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.lang.*
import net.bytebuddy.*
import net.bytebuddy.dynamic.loading.*
import net.bytebuddy.implementation.*
import net.bytebuddy.matcher.*

/**
 * 渲染文件节点时，为游戏或模组根目录提供提供额外的信息文本。
 */
class FileRenderInjector : CodeInjector {
    //com.intellij.openapi.fileChooser.tree.FileRenderer
    //com.intellij.openapi.fileChooser.tree.FileRenderer.customize
    
    override fun inject() {
        val targetClass = FileRenderer::class.java
        ByteBuddy()
            .rebase(targetClass)
            .method(ElementMatchers.named("customize"))
            .intercept(MethodCall.invokeSuper().withAllArguments().andThen(MethodDelegation.to(this)))
            .make()
            .load(targetClass.classLoader, ClassReloadingStrategy.fromInstalledAgent())
    }
    
    @Inject
    @Suppress("UNUSED_PARAMETER")
    fun customize(renderer: SimpleColoredComponent, value: Any, selected: Boolean, focused: Boolean) {
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