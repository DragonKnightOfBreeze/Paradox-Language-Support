package icu.windea.pls.bytebuddy

import net.bytebuddy.*
import net.bytebuddy.agent.*
import net.bytebuddy.dynamic.loading.*
import net.bytebuddy.implementation.*
import net.bytebuddy.matcher.*
import org.junit.*

@Suppress("unused")
class ByteBuddyTest2  {
    //目标类型已加载 + subtype + 方法调用 - 测试失败
    
    @Test(expected = Exception::class)
    fun test() {
        inject()
        
        TestBean().customize(1)
        
        //hello 1
        //hello 1 !!!
    }
    
    private fun inject() {
        ByteBuddyAgent.install()
        val classLoader = javaClass.classLoader
        val method = javaClass.declaredMethods.find { it.name == "customize" }!!
        ByteBuddy()
            .subclass(TestBean::class.java)
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
        //val declaredMethods = this.javaClass.declaredMethods
        //val method = declaredMethods.find { it.name.startsWith("customize\$original") }!!
        //method.trySetAccessible()
        //return method.invoke(this, *args)
        return null
    }
    
    class TestBean {
        fun customize(n: Int) {
            println("hello $n")
        }
    }
}
