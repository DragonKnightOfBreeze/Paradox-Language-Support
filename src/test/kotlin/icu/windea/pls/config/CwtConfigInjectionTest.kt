package icu.windea.pls.config

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsStringConstants
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

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
            val injectOption = p.optionConfigs.filterIsInstance<CwtOptionConfig>().single { it.key == "inject" }
            assertEquals("common/test/injection_source.cwt@injected_group/*", injectOption.value)
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

        // a top-level block value '{}' cannot carry child configs, injection should be ignored
        run {
            val values = targetFileConfig.values
            assertEquals(1, values.size)
            val v = values.single()
            assertEquals(PlsStringConstants.blockFolder, v.value)
            assertNotNull(v.configs)
            assertTrue(v.configs!!.isEmpty())

            assertTrue(v.properties.orEmpty().isEmpty())
            assertNull(v.parentConfig)

            assertNotNull(v.pointer.element)
            assertEquals(targetFile.name, v.pointer.element!!.containingFile.name)

            val injectOption = v.optionConfigs.filterIsInstance<CwtOptionConfig>().single { it.key == "inject" }
            assertEquals("common/test/injection_source.cwt@injected_group/*", injectOption.value)
        }
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
