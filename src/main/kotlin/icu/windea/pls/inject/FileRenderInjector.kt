package icu.windea.pls.inject

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.tree.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.lang.*
import net.bytebuddy.*
import net.bytebuddy.dynamic.*
import net.bytebuddy.dynamic.loading.*
import net.bytebuddy.implementation.*
import net.bytebuddy.matcher.*
import net.bytebuddy.pool.*

/**
 * 渲染文件节点时，为游戏或模组根目录提供提供额外的信息文本。
 */
class FileRenderInjector : CodeInjector {
    //com.intellij.openapi.fileChooser.tree.FileRenderer
    //com.intellij.openapi.fileChooser.tree.FileRenderer.customize
    
    override fun inject() {
        //val ideClassLoader = Application::class.java.classLoader
        val classLoader = javaClass.classLoader
        val type = TypePool.Default.of(classLoader).describe("com.intellij.openapi.fileChooser.tree.FileRenderer").resolve()
        val classFileLocator = ClassFileLocator.ForClassLoader.of(classLoader)
        val method = javaClass.methods.find { it.name == "customize" }!!
        ByteBuddy()
            .redefine<Any>(type, classFileLocator)
            .method(ElementMatchers.named("customize"))
            .intercept(MethodDelegation.to(this))
            .make()
            .load(classLoader, ClassLoadingStrategy.Default.INJECTION)
        
        //val method = javaClass.methods.find { it.name == "customize" }!!
        //val targetClass = FileRenderer::class.java
        //val classLoader = targetClass.classLoader
        //ByteBuddy()
        //    .with(Implementation.Context.Disabled.Factory.INSTANCE)
        //    .redefine(targetClass)
        //    .method(ElementMatchers.named("customize"))
        //    .intercept(MethodCall.invoke(method).on(this).withThis().withAllArguments())
        //    .make()
        //    .load(classLoader, ClassReloadingStrategy.fromInstalledAgent())
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