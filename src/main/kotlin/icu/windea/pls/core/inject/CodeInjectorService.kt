package icu.windea.pls.core.inject

import javassist.*

/**
 * 用于应用动态代码注入。
 *
 * @see CodeInjector
 */
class CodeInjectorService {
    val statusMap = mutableMapOf<CodeInjector, Boolean>()
    
    init {
        doInject()
    }
    
    fun doInject() {
        //这里不应当有任何报错
        val pool by lazy { getPool() }
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            if(statusMap.get(codeInjector) != true) {
                synchronized(codeInjector) {
                    if(statusMap.get(codeInjector) != true) {
                        codeInjector.inject(pool)
                        statusMap.put(codeInjector, true)
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