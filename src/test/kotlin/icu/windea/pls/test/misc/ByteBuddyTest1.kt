package icu.windea.pls.test.misc

import net.bytebuddy.*
import net.bytebuddy.agent.*
import net.bytebuddy.dynamic.*
import net.bytebuddy.dynamic.loading.*
import net.bytebuddy.implementation.*
import net.bytebuddy.matcher.*
import net.bytebuddy.pool.*
import org.junit.*

class ByteBuddyTest1  {
    //目标类型未加载 + rebase + 方法调用 - 测试通过
    
    @Test
    fun test() {
        inject()
        
        TestBean().customize(1)
        
        //hello 1
        //hello 1 !!!
    }
    
    private fun inject() {
        ByteBuddyAgent.install()
        val classLoader = javaClass.classLoader
        val typePool = TypePool.Default.of(classLoader)
        val classFileLocator = ClassFileLocator.ForClassLoader.of(classLoader)
        val method = javaClass.declaredMethods.find { it.name == "customize" }!!
        ByteBuddy()
            .rebase<Any>(typePool.describe("icu.windea.pls.ByteBuddyTest1\$TestBean").resolve(), classFileLocator)
            .method(ElementMatchers.named("customize"))
            .intercept(MethodCall.invoke(method).on(this).withThis().withAllArguments())
            .make()
            .load(classLoader, ClassLoadingStrategy.Default.INJECTION)
    }
    
    fun Any.customize(n: Int) {
        callSelf(n)
        println("hello $n !!!")
    }
    
    fun Any.callSelf(vararg args: Any?): Any? {
        val declaredMethods = this.javaClass.declaredMethods
        val method = declaredMethods.find { it.name.startsWith("customize\$original") }!!
        method.trySetAccessible()
        return method.invoke(this, *args)
    }
    
    class TestBean {
        fun customize(n: Int) {
            println("hello $n")
        }
    }
}
