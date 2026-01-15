package icu.windea.pls.inject.injectors

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase
import icu.windea.pls.config.option.CwtOptionDataHolderBase
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.CodeInjectorSupport
import icu.windea.pls.inject.annotations.InjectionTarget
import javassist.ClassClassPath
import javassist.CtNewConstructor
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Suppress("unused")
@RunWith(JUnit4::class)
class InlinedDelegateFieldCodeInjectorsTest : BasePlatformTestCase() {
    @Suppress("unused")
    private class OptionDataModel : CwtOptionDataHolderBase()

    @Suppress("unused")
    private class ConfigGroupDataModel : CwtConfigGroupDataHolderBase()

    override fun setUp() {
        super.setUp()
        CodeInjectorScope.classPool = CodeInjectorScope.getClassPool().also { it.appendClassPath(ClassClassPath(javaClass)) }
        CodeInjectorScope.codeInjectors.clear()
    }

    override fun tearDown() {
        try {
            CodeInjectorScope.classPool = null
            CodeInjectorScope.codeInjectors.clear()
        } finally {
            super.tearDown()
        }
    }

    @Test
    fun test_inlinedDelegateFields_forDataHolders() {
        val classLoader = this::class.java.classLoader

        val optionBaseClassName = "icu.windea.pls.config.option.CwtOptionDataHolderBase"
        val configGroupBaseClassName = "icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase"

        val injectors = CodeInjector.EP_NAME.extensionList
            .filter { it.javaClass.name.startsWith("icu.windea.pls.inject.injectors.ext.InlinedDelegateFieldCodeInjectors$") }

        assertTrue(
            "Cannot find any code injectors from EP: icu.windea.pls.inject.codeInjector (expected InlinedDelegateFieldCodeInjectors.*)",
            injectors.isNotEmpty()
        )

        val pool = CodeInjectorScope.classPool ?: error("ClassPool is not initialized")
        val injectedBytecode = mutableMapOf<String, ByteArray>()

        // 方案A：不调用 CtClass.toClass()，只在 CtClass 上应用 supports 并导出字节码，然后用隔离的 ClassLoader 加载。
        for (injector in injectors) {
            val injectionTarget = injector.javaClass.getAnnotation(InjectionTarget::class.java) ?: continue
            val targetClassName = injectionTarget.value

            val ctClass = pool.get(targetClassName)
            ctClass.defrost()

            injector.putUserData(CodeInjectorScope.targetClassKey, ctClass)
            CodeInjectorSupport.EP_NAME.extensionList.forEach { ep -> ep.apply(injector) }

            injectedBytecode[targetClassName] = ctClass.toBytecode()
            ctClass.detach()

            // clean up
            injector.putUserData(CodeInjectorScope.targetClassKey, null)
        }

        val loader = ByteArrayClassLoader(classLoader)
        val injectedOptionBaseClass = loader.define(optionBaseClassName, injectedBytecode.getValue(optionBaseClassName))
        val injectedConfigGroupBaseClass = loader.define(configGroupBaseClassName, injectedBytecode.getValue(configGroupBaseClassName))

        // verify injected: no delegated fields in base classes
        run {
            val hasDelegateField = injectedOptionBaseClass.declaredFields.any { it.name.endsWith("\$delegate") }
            assertFalse("Expected no delegated fields in $optionBaseClassName after injection", hasDelegateField)
        }
        run {
            val hasDelegateField = injectedConfigGroupBaseClass.declaredFields.any { it.name.endsWith("\$delegate") }
            assertFalse("Expected no delegated fields in $configGroupBaseClassName after injection", hasDelegateField)
        }

        // verify subclasses can be loaded/instantiated and accessors work
        run {
            val modelClassName = "icu.windea.pls.inject.injectors.InlinedDelegateFieldCodeInjectorsTest\$OptionDataModel_Injected"
            val ctModelClass = pool.makeClass(modelClassName)
            ctModelClass.superclass = pool.get(optionBaseClassName)
            ctModelClass.addConstructor(CtNewConstructor.defaultConstructor(ctModelClass))
            val modelClass = loader.define(modelClassName, ctModelClass.toBytecode())
            ctModelClass.detach()

            val instance = modelClass.getDeclaredConstructor().newInstance()

            val getContextConfigsType = modelClass.getMethod("getContextConfigsType")
            assertEquals("single", getContextConfigsType.invoke(instance))

            val getTag = modelClass.getMethod("getTag")
            val setTag = modelClass.getMethod("setTag", Boolean::class.javaPrimitiveType)
            assertEquals(false, getTag.invoke(instance))
            setTag.invoke(instance, true)
            assertEquals(true, getTag.invoke(instance))
        }

        run {
            val modelClassName = "icu.windea.pls.inject.injectors.InlinedDelegateFieldCodeInjectorsTest\$ConfigGroupDataModel_Injected"
            val ctModelClass = pool.makeClass(modelClassName)
            ctModelClass.superclass = pool.get(configGroupBaseClassName)
            ctModelClass.addConstructor(CtNewConstructor.defaultConstructor(ctModelClass))
            val modelClass = loader.define(modelClassName, ctModelClass.toBytecode())
            ctModelClass.detach()

            val instance = modelClass.getDeclaredConstructor().newInstance()

            val getSchemas = modelClass.getMethod("getSchemas")
            val clear = modelClass.getMethod("clear")

            val schemas1 = getSchemas.invoke(instance)
            assertNotNull(schemas1)

            clear.invoke(instance)

            val schemas2 = getSchemas.invoke(instance)
            assertNotNull(schemas2)
            assertNotSame(schemas1, schemas2)
        }
    }

    private class ByteArrayClassLoader(parent: ClassLoader) : ClassLoader(parent) {
        fun define(name: String, bytes: ByteArray): Class<*> {
            return defineClass(name, bytes, 0, bytes.size)
        }
    }
}
