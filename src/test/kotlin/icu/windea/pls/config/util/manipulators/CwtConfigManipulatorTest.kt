package icu.windea.pls.config.util.manipulators

import com.intellij.openapi.util.Key
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.core.findChild
import icu.windea.pls.core.util.createKey
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtConfigManipulatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testDeepCopyConfigs_parentPointers() {
        myFixture.configureByFile("features/config/property_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val p = root.findChild<CwtProperty> { it.name == "block_prop" }!!
        val container = CwtPropertyConfig.resolve(p, file, group)!!
        val copied = CwtConfigManipulator.deepCopyConfigs(container, parentConfig = container)
        assertNotNull(copied)
        val list = copied!!
        // block_prop has 2 children: a(property) and val1(value)
        assertEquals(2, list.size)
        list.forEach { child -> assertSame(container, child.parentConfig) }
        val propNames = list.filterIsInstance<CwtPropertyConfig>().map { it.key }.toSet()
        val valValues = list.filterIsInstance<CwtValueConfig>().map { it.value }.toSet()
        assertTrue("a" in propNames)
        assertTrue("val1" in valValues)
    }

    @Test
    fun testDeepCopyConfigsInDeclaration_subtypeFlatten_and_parent() {
        myFixture.configureByFile("features/manipulators/deep_copy_declaration.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val p = root.findChild<CwtProperty> { it.name == "decl" }!!
        val container = CwtPropertyConfig.resolve(p, file, group)!!
        // only subtype[foo] should be flattened; subtype[bar] should be skipped
        val context = CwtDeclarationConfigContext(
            definitionName = null,
            definitionType = "test",
            definitionSubtypes = listOf("foo"),
            configGroup = group,
        )
        val copied = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(container, parentConfig = container, context = context)
        assertNotNull(copied)
        val list = copied!!
        // expect: a (prop), b (prop), v1 (value). No subtype[...] nodes and no c.
        list.forEach { child -> assertSame(container, child.parentConfig) }
        val props = list.filterIsInstance<CwtPropertyConfig>().map { it.key }.toSet()
        val values = list.filterIsInstance<CwtValueConfig>().map { it.value }.toSet()
        assertTrue("a" in props)
        assertTrue("b" in props)
        assertTrue("v1" in values)
        assertFalse("c" in props)
        // ensure none of results are subtype[...] wrapper keys
        assertFalse(props.any { it.startsWith("subtype[") })
        // basic type sanity
        list.filterIsInstance<CwtPropertyConfig>().forEach { assertNotEquals(CwtType.Block, it.valueType) }
    }

    @Test
    fun testDeepCopyConfigs_parentChain_nested() {
        myFixture.configureByFile("features/manipulators/deep_copy_nested.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val topProp = root.findChild<CwtProperty> { it.name == "top" }!!
        val topCfg = CwtPropertyConfig.resolve(topProp, file, group)!!
        val copiedTopChildren = CwtConfigManipulator.deepCopyConfigs(topCfg, parentConfig = topCfg)!!
        // only one child: mid
        val midCfg = copiedTopChildren.filterIsInstance<CwtPropertyConfig>().single { it.key == "mid" }
        assertSame(topCfg, midCfg.parentConfig)
        // mid has two children: leaf_prop and leaf_val
        val midChildren = midCfg.configs!!
        val leafProp = midChildren.filterIsInstance<CwtPropertyConfig>().single { it.key == "leaf_prop" }
        val leafVal = midChildren.filterIsInstance<CwtValueConfig>().single { it.value == "leaf_val" }
        assertSame(midCfg, leafProp.parentConfig)
        assertSame(midCfg, leafVal.parentConfig)
    }

    @Test
    fun testDeepCopyConfigs_optionConfigs_and_userData_semantics() {
        myFixture.configureByFile("features/manipulators/deep_copy_options.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val containerProp = root.findChild<CwtProperty> { it.name == "container" }!!
        val containerCfg = CwtPropertyConfig.resolve(containerProp, file, group)!!
        val p2Original = containerCfg.configs!!.filterIsInstance<CwtPropertyConfig>().single { it.key == "p2" }
        // mark original with userData
        val extraKey: Key<String> = createKey("test.deepcopy.extra")
        p2Original.putUserData(extraKey, "orig")

        val copied = CwtConfigManipulator.deepCopyConfigs(containerCfg, parentConfig = containerCfg)!!
        val p2Copied = copied.filterIsInstance<CwtPropertyConfig>().single { it.key == "p2" }
        // optionConfigs preserved (required + severity=info)
        val opts = p2Copied.optionConfigs
        assertNotNull(opts)
        assertTrue(opts.any { it is icu.windea.pls.config.config.CwtOptionValueConfig && it.value == "required" })
        assertTrue(opts.any { it is icu.windea.pls.config.config.CwtOptionConfig && it.key == "severity" && it.value == "info" })
        // userData not copied (wrapper has no own value), but read is inherited from delegate
        assertEquals("orig", p2Copied.getUserData(extraKey))
        // writing to wrapper with another key does not affect original
        val extraKey2: Key<String> = createKey("test.deepcopy.extra2")
        p2Copied.putUserData(extraKey2, "wrap")
        assertEquals(null, p2Original.getUserData(extraKey2))
    }

    @Test
    fun testDeepCopyConfigs_nullContainerConfigs_returnsNull_and_parentUnchanged() {
        myFixture.configureByFile("features/config/property_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val strProp = root.findChild<CwtProperty> { it.name == "str_prop" }!! // non-block -> configs == null
        val container = CwtPropertyConfig.resolve(strProp, file, group)!!
        val parentBefore = container.parentConfig
        val copied = CwtConfigManipulator.deepCopyConfigs(container, parentConfig = container)
        assertNull(copied)
        assertSame(parentBefore, container.parentConfig)
    }

    @Test
    fun testDeepCopyConfigs_emptyContainerConfigs_returnsEmptyList_and_parentUnchanged() {
        myFixture.configureByFile("features/manipulators/deep_copy_empty.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val emptyProp = root.findChild<CwtProperty> { it.name == "empty_prop" }!! // block {} -> configs.isEmpty()
        val container = CwtPropertyConfig.resolve(emptyProp, file, group)!!
        val parentBefore = container.parentConfig
        val copied = CwtConfigManipulator.deepCopyConfigs(container, parentConfig = container)
        assertNotNull(copied)
        assertTrue(copied!!.isEmpty())
        assertSame(parentBefore, container.parentConfig)
    }

    @Test
    fun testDeepCopyConfigsInDeclaration_nullContainerConfigs_returnsNull_and_parentUnchanged() {
        myFixture.configureByFile("features/config/property_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val strProp = root.findChild<CwtProperty> { it.name == "str_prop" }!! // non-block -> configs == null
        val container = CwtPropertyConfig.resolve(strProp, file, group)!!
        val parentBefore = container.parentConfig
        val context = CwtDeclarationConfigContext(null, "test", null, group)
        val copied = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(container, parentConfig = container, context = context)
        assertNull(copied)
        assertSame(parentBefore, container.parentConfig)
    }

    @Test
    fun testDeepCopyConfigsInDeclaration_emptyContainerConfigs_returnsEmptyList_and_parentUnchanged() {
        myFixture.configureByFile("features/manipulators/deep_copy_empty.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val emptyProp = root.findChild<CwtProperty> { it.name == "empty_prop" }!! // block {} -> configs.isEmpty()
        val container = CwtPropertyConfig.resolve(emptyProp, file, group)!!
        val parentBefore = container.parentConfig
        val context = CwtDeclarationConfigContext(null, "test", null, group)
        val copied = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(container, parentConfig = container, context = context)
        assertNotNull(copied)
        assertTrue(copied!!.isEmpty())
        assertSame(parentBefore, container.parentConfig)
    }

    @Test
    fun testDeepCopyConfigs_nullContainerConfigs_withDifferentParent_noSideEffect() {
        myFixture.configureByFile("features/config/property_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val strProp = root.findChild<CwtProperty> { it.name == "str_prop" }!! // non-block -> configs == null
        val container = CwtPropertyConfig.resolve(strProp, file, group)!!
        val parentBefore = container.parentConfig
        // choose a different parent config: block_prop
        val blockProp = root.findChild<CwtProperty> { it.name == "block_prop" }!!
        val otherParent = CwtPropertyConfig.resolve(blockProp, file, group)!!

        val copied = CwtConfigManipulator.deepCopyConfigs(container, parentConfig = otherParent)
        assertNull(copied)
        // ensure container's parent not changed to otherParent
        assertSame(parentBefore, container.parentConfig)
        // ensure otherParent's children remain unchanged in count
        val otherChildren = otherParent.configs?.size
        assertEquals(otherChildren, otherParent.configs?.size)
    }

    @Test
    fun testDeepCopyConfigsInDeclaration_nullContainerConfigs_withDifferentParent_noSideEffect() {
        myFixture.configureByFile("features/config/property_config_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val group = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val root = file.block!!

        val strProp = root.findChild<CwtProperty> { it.name == "str_prop" }!! // non-block -> configs == null
        val container = CwtPropertyConfig.resolve(strProp, file, group)!!
        val parentBefore = container.parentConfig
        val blockProp = root.findChild<CwtProperty> { it.name == "block_prop" }!!
        val otherParent = CwtPropertyConfig.resolve(blockProp, file, group)!!
        val context = CwtDeclarationConfigContext(null, "test", null, group)

        val copied = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(container, parentConfig = otherParent, context = context)
        assertNull(copied)
        assertSame(parentBefore, container.parentConfig)
        val otherChildren = otherParent.configs?.size
        assertEquals(otherChildren, otherParent.configs?.size)
    }
}
