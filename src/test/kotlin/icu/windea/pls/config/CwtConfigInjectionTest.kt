package icu.windea.pls.config

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.ep.config.config.CwtInjectedConfigProvider
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsStrings
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtConfigInjectionTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testInjection_appends_and_parentChain() {
        myFixture.configureByFile("features/config/injection_source.test.cwt")
        val sourceFile = myFixture.file as CwtFile

        myFixture.configureByFile("features/config/injection_target.test.cwt")
        val targetFile = myFixture.file as CwtFile

        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)

        val sourceFilePath = "common/test/injection_source.cwt"
        val targetFilePath = "common/test/injection_target.cwt"

        val sourceFileConfig = CwtFileConfig.resolve(sourceFile, configGroup, sourceFilePath)
        val targetFileConfig = CwtFileConfig.resolve(targetFile, configGroup, targetFilePath)

        val fileConfigs = CwtConfigResolverManager.getFileConfigs(configGroup)
        fileConfigs[sourceFilePath] = sourceFileConfig
        fileConfigs[targetFilePath] = targetFileConfig

        runPostProcessActions(configGroup)

        val sourceInjectedGroup = sourceFileConfig.properties.single { it.key == "injected_group" }
        val sourceInjectedChildren = sourceInjectedGroup.properties.orEmpty().associateBy { it.key }

        val targetProps = targetFileConfig.properties.associateBy { it.key }

        run {
            val p = targetProps.getValue("target_block")
            val children = p.properties.orEmpty()
            val keys = children.map { it.key }
            assertEquals(listOf("existing", "injected1", "injected2"), keys)

            val existing = children.single { it.key == "existing" }
            assertEquals("0", existing.value)

            val injected1 = children.single { it.key == "injected1" }
            val injected2 = children.single { it.key == "injected2" }
            assertEquals("1", injected1.value)
            assertEquals("2", injected2.value)
            assertSame(p, injected1.parentConfig)
            assertSame(p, injected2.parentConfig)
            assertNotSame(sourceInjectedChildren.getValue("injected1"), injected1)
            assertNotSame(sourceInjectedChildren.getValue("injected2"), injected2)

            // injected configs are deep-copied via delegated, pointer should still point to source PSI
            assertNotNull(injected1.pointer.element)
            assertNotNull(injected2.pointer.element)
            assertEquals(sourceFile.name, injected1.pointer.element!!.containingFile.name)
            assertEquals(sourceFile.name, injected2.pointer.element!!.containingFile.name)
        }

        run {
            val p = targetProps.getValue("empty_target")
            val children = p.properties.orEmpty()
            val keys = children.map { it.key }
            assertEquals(listOf("injected1", "injected2"), keys)

            val injected1 = children.single { it.key == "injected1" }
            val injected2 = children.single { it.key == "injected2" }
            assertEquals("1", injected1.value)
            assertEquals("2", injected2.value)
            assertSame(p, injected1.parentConfig)
            assertSame(p, injected2.parentConfig)
            assertNotSame(sourceInjectedChildren.getValue("injected1"), injected1)
            assertNotSame(sourceInjectedChildren.getValue("injected2"), injected2)

            assertNotNull(injected1.pointer.element)
            assertNotNull(injected2.pointer.element)
            assertEquals(sourceFile.name, injected1.pointer.element!!.containingFile.name)
            assertEquals(sourceFile.name, injected2.pointer.element!!.containingFile.name)
        }

        // no matched configs => no injection
        run {
            val p = targetProps.getValue("unmatched_target")
            val children = p.properties.orEmpty()
            assertEquals(1, children.size)
            val existing = children.single()
            assertEquals("existing", existing.key)
            assertEquals("0", existing.value)
        }

        // non-block target cannot be injected (configs == null)
        run {
            val p = targetProps.getValue("non_block")
            assertEquals("1", p.value)
            assertNull(p.configs)
            assertEquals("common/test/injection_source.cwt@injected_group/*", p.optionData.inject)
        }

        // duplicate keys are allowed: injected configs are appended (no de-dup)
        run {
            val p = targetProps.getValue("duplicate_target")
            val children = p.properties.orEmpty()
            val keys = children.map { it.key }
            assertEquals(listOf("injected1", "injected1", "injected2"), keys)

            val existingInjected1 = children[0]
            val injected1 = children[1]
            val injected2 = children[2]
            assertEquals("0", existingInjected1.value)
            assertEquals("1", injected1.value)
            assertEquals("2", injected2.value)

            assertSame(p, existingInjected1.parentConfig)
            assertSame(p, injected1.parentConfig)
            assertSame(p, injected2.parentConfig)

            assertNotNull(existingInjected1.pointer.element)
            assertNotNull(injected1.pointer.element)
            assertNotNull(injected2.pointer.element)
            assertEquals(targetFile.name, existingInjected1.pointer.element!!.containingFile.name)
            assertEquals(sourceFile.name, injected1.pointer.element!!.containingFile.name)
            assertEquals(sourceFile.name, injected2.pointer.element!!.containingFile.name)
        }

        // a top-level block value '{}' is also injectable now
        run {
            val values = targetFileConfig.values
            assertEquals(1, values.size)
            val v = values.single()
            assertEquals(PlsStrings.blockFolder, v.value)
            assertNotNull(v.configs)
            assertTrue(v.configs!!.isNotEmpty())

            assertTrue(v.properties.orEmpty().isNotEmpty())
            assertNull(v.parentConfig)

            assertNotNull(v.pointer.element)
            assertEquals(targetFile.name, v.pointer.element!!.containingFile.name)

            assertEquals("common/test/injection_source.cwt@injected_group/*", v.optionData.inject)
        }
    }

    @Test
    fun testInjection_deepRecursive_ignored() {
        // 关键思路：用测试专用 `CwtInjectedConfigProvider` 强制制造 `deepCopyConfigs` 重入
        //
        // 真正的“深递归”不是来自 `## inject` 本身（那是浅递归链），而是来自：
        // - 注入时 `deepCopyForInjection` 会调用 `CwtConfigManipulator.deepCopyConfigs(configToInject, delegatedConfig)`
        // - 在 deep copy 过程中会调用 `CwtConfigService.injectConfigs(parentConfig, result)`
        // - 如果某个 `CwtInjectedConfigProvider` 在回调里再次触发 deep copy（对同一 parentConfig），就会出现 deep recursion / reentrancy。
        //
        // 在测试里临时注册了一个 provider：
        // - 在 `injectConfigs(parentConfig, ...)` 里调用 `CwtConfigManipulator.deepCopyConfigs(parentConfig)`
        // - 由于此时 `deepCopyConfigs(parentConfig)` 正在进行中，新实现会通过 key-based recursion guard 返回 `null`
        // - 测试用 `AtomicBoolean` 记录“确实发生了递归拦截（返回空列表）”

        val recursionPrevented = AtomicBoolean(false)

        val provider = object : CwtInjectedConfigProvider {
            override fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
                val r = CwtConfigManipulator.deepCopyConfigs(parentConfig)
                if (r == emptyList<CwtMemberConfig<*>>()) recursionPrevented.set(true)
                return false
            }
        }

        CwtInjectedConfigProvider.EP_NAME.point.registerExtension(provider, testRootDisposable)

        myFixture.configureByFile("features/config/injection_deep_recursive_source.test.cwt")
        val sourceFile = myFixture.file as CwtFile

        myFixture.configureByFile("features/config/injection_deep_recursive_target.test.cwt")
        val targetFile = myFixture.file as CwtFile

        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)

        val sourceFilePath = "common/test/injection_deep_recursive_source.cwt"
        val targetFilePath = "common/test/injection_deep_recursive_target.cwt"

        val sourceFileConfig = CwtFileConfig.resolve(sourceFile, configGroup, sourceFilePath)
        val targetFileConfig = CwtFileConfig.resolve(targetFile, configGroup, targetFilePath)

        val fileConfigs = CwtConfigResolverManager.getFileConfigs(configGroup)
        fileConfigs[sourceFilePath] = sourceFileConfig
        fileConfigs[targetFilePath] = targetFileConfig

        runPostProcessActions(configGroup)

        assertTrue(recursionPrevented.get())

        val targetProps = targetFileConfig.properties.associateBy { it.key }
        val p = targetProps.getValue("deep_target")
        val children = p.properties.orEmpty()
        val keys = children.map { it.key }
        assertEquals(listOf("existing", "deep_node"), keys)

        val existing = children[0]
        val deepNode = children[1]
        assertEquals("0", existing.value)
        assertEquals(1, deepNode.properties.orEmpty().size)
        val a = deepNode.properties.orEmpty().single()
        assertEquals("a", a.key)
        assertEquals("1", a.value)
    }

    @Test
    fun testInjection_recursive_ignored() {
        myFixture.configureByFile("features/config/injection_recursive_a.test.cwt")
        val fileA = myFixture.file as CwtFile

        myFixture.configureByFile("features/config/injection_recursive_b.test.cwt")
        val fileB = myFixture.file as CwtFile

        myFixture.configureByFile("features/config/injection_recursive_target.test.cwt")
        val fileTarget = myFixture.file as CwtFile

        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)

        val pathA = "common/test/injection_recursive_a.cwt"
        val pathB = "common/test/injection_recursive_b.cwt"
        val pathTarget = "common/test/injection_recursive_target.cwt"

        val configA = CwtFileConfig.resolve(fileA, configGroup, pathA)
        val configB = CwtFileConfig.resolve(fileB, configGroup, pathB)
        val configTarget = CwtFileConfig.resolve(fileTarget, configGroup, pathTarget)

        val fileConfigs = CwtConfigResolverManager.getFileConfigs(configGroup)
        fileConfigs[pathA] = configA
        fileConfigs[pathB] = configB
        fileConfigs[pathTarget] = configTarget

        runPostProcessActions(configGroup)

        run {
            val p = configTarget.properties.single { it.key == "cycle_target" }
            val children = p.properties.orEmpty()
            assertEquals(1, children.size)
            val existing = children.single()
            assertEquals("existing", existing.key)
            assertEquals("0", existing.value)
        }
    }

    private fun runPostProcessActions(configGroup: CwtConfigGroupImpl) {
        val actions = CwtConfigResolverManager.getPostProcessActions(configGroup)
        while (actions.isNotEmpty()) {
            val action = actions.removeAt(0)
            action.run()
        }
    }
}
