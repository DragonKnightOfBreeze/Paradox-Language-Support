package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import javassist.*
import kotlin.reflect.full.*

/**
 * 提供对本地化缓存的代码注入器的支持。
 *
 * @see InjectLocalCache
 */
class LocalCacheCodeInjectorSupport : CodeInjectorSupport {
    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorService.targetClassKey) ?: return
        val injectLocalCacheList = codeInjector::class.findAnnotations<InjectLocalCache>()
        if(injectLocalCacheList.isEmpty()) return
        
        targetClass.classPool.importPackage("com.google.common.cache")
        targetClass.classPool.importPackage("icu.windea.pls.core.util")
        
        for(injectLocalCache in injectLocalCacheList) {
            val methodName = injectLocalCache.value
            val spec = injectLocalCache.spec
            
            val method = targetClass.declaredMethods.find { it.name == methodName && it.parameterTypes.size == 1 }
            if(method == null) {
                thisLogger().warn("Method ${methodName}() is not found in ${targetClass.name}")
                continue
            }
            
            val returnType = method.returnType
            if(returnType == null || returnType == CtClass.voidType) {
                thisLogger().warn("Method ${methodName}() returns nothing")
                continue
            }
            
            val loaderMethodName = "__${methodName}__"
            method.name = loaderMethodName
            
            //FIXME 这里javassist不支持匿名内部类并且不支持非静态内部类
            val cacheFieldName = "__CACHE__"
            val cacheFieldCode = """
            private static final LoadingCache $cacheFieldName = CacheBuilder.from("$spec").build(new CacheLoader() {
                public Object load(Object key) {
                    return $loaderMethodName(key);
                }
            });
            """.trimIndent()
            val cacheField = CtField.make(cacheFieldCode, targetClass)
            targetClass.addField(cacheField)
            
            val code = "{ return (\$r) $cacheFieldName.getUnchecked($1); }"
            val m = CtMethod(returnType, methodName, method.parameterTypes, targetClass)
            m.modifiers = m.modifiers and Modifier.PRIVATE
            m.setBody(code)
            targetClass.addMethod(m)
        }
    }
}
