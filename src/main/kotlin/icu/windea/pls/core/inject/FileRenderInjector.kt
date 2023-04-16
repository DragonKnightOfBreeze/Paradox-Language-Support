package icu.windea.pls.core.inject

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.tree.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import javassist.*

/**
 * 渲染文件节点时，为游戏或模组根目录提供提供额外的信息文本。
 * 
 * @see icu.windea.pls.core.ParadoxProjectViewDecorator
 */
class FileRenderInjector: CodeInjector {
    override fun inject(pool: ClassPool) {
        val targetClassName = "com.intellij.openapi.fileChooser.tree.FileRenderer"
        val injectorClassName = javaClass.name
        //pool.appendClassPath(ClassClassPath(baseClass))
        val clazz = pool.get(targetClassName)
        val customizeMethod = clazz.getDeclaredMethod("customize")
        val code = """
        {
            try {
                System.out.println(11111);
                //com.intellij.openapi.extensions.PluginId pid = com.intellij.openapi.extensions.PluginId.getId("${PlsConstants.pluginId}");
                //ClassLoader cl = com.intellij.ide.plugins.PluginManager.getInstance().findEnabledPlugin(pid).getClassLoader();
                //$injectorClassName t = ($injectorClassName) Class.forName("$injectorClassName", false, cl).newInstance();
                ////$injectorClassName t = ($injectorClassName) cl.loadClass("$injectorClassName").newInstance();
                $injectorClassName t = new $injectorClassName();
                //t.customize($$);
            } catch(Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        """.trimIndent()
        customizeMethod.insertAfter(code)
        clazz.toClass()
        clazz.detach()
    }
    
    //com.intellij.openapi.fileChooser.tree.FileRenderer.customize
    
    @Suppress("UNUSED_PARAMETER")
    @InjectMethod
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