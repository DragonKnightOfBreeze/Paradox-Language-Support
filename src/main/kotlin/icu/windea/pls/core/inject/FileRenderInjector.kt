package icu.windea.pls.core.inject

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.tree.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.lang.*
import javassist.*

/**
 * 渲染文件节点时，为游戏或模组根目录提供提供额外的信息文本。
 *
 * @see icu.windea.pls.core.ParadoxProjectViewDecorator
 */
class FileRenderInjector : CodeInjector {
    override val id: String = "FileRenderInjector"
    
    @JvmField val methods = javaClass.methods.filter { 
        it.isAnnotationPresent(InjectMethod::class.java)
    }
    
    override fun inject(pool: ClassPool) {
        pool.importPackage("java.util")
        pool.importPackage("java.lang.reflect")
        pool.importPackage("com.intellij.openapi.application")
        pool.importPackage("com.intellij.openapi.util")
        
        val targetClassName = "com.intellij.openapi.fileChooser.tree.FileRenderer"
        val targetClass = pool.get(targetClassName)
        val customizeMethod = targetClass.getDeclaredMethod("customize")
        val d = "$"
        val code = """
        {
            try {
                com.intellij.openapi.util.Key key = com.intellij.openapi.util.Key.findKeyByName("PLS_CODE_INJECTORS");
                if(key == null) { return; }
                Map codeInjectors = (Map) ApplicationManager.getApplication().getUserData(key);
                Object injector = codeInjectors.get("${id}");
                if(injector == null) { return; }
                Class injectorClass = injector.getClass();
                Object[] args = ${d}args;
                System.out.println(Arrays.toString(args));
                Field methodsField = injectorClass.getDeclaredField("methods");
                List methods = (List) methodsField.get(injector);
                Method method = (Method) methods.get(0);
                System.out.println(method.getName());
                method.invoke(injector, args);
            } catch(Throwable e) {
                throw e;
            }
        }
        """.trimIndent()
        customizeMethod.insertAfter(code)
        targetClass.toClass()
        targetClass.detach()
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