package icu.windea.pls.inject

import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import javassist.*

/**
 * 用于应用动态代码注入。
 *
 * @see CodeInjector
 */
class CodeInjectorService {
    companion object {
        val codeInjectorsKey = Key.create<Map<String, CodeInjector>>("PLS_CODE_INJECTORS")
    }
    
    val injectors = mutableMapOf<String, CodeInjector>()
    val statusMap = mutableMapOf<String, Boolean>()
    
    init {
        prepareInject()
        inject()
    }
    
    fun prepareInject() {
        val application = ApplicationManager.getApplication()
        if(application.getUserData(codeInjectorsKey) == null) {
            application.putUserData(codeInjectorsKey, injectors)
        }
    }
    
    fun inject() {
        //这里不应当有任何报错
        val pool by lazy { getPool() }
        CodeInjector.EP_NAME.extensionList.forEach { injector ->
            val id = injector.id
            injectors.put(id, injector)
            if(statusMap.get(id) != true) {
                synchronized(injector) {
                    if(statusMap.get(id) != true) {
                        injector.inject(pool)
                        statusMap.put(id, true)
                    }
                }
            }
        }
    }
    
    private fun getPool(): ClassPool {
        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(this.javaClass))
        val classPathList = System.getProperty("java.class.path")
        val separator = if(System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            try {
                pool.appendClassPath(it)
            } catch(e: Exception) {
                //ignore
            }
        }
        return pool
    }
}