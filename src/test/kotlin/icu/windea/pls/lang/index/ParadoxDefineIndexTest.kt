package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefineIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    private fun configureDefineFile(@TestDataFile testDataPath: String) {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/${testDataPath.substringAfterLast('/')}")
        myFixture.configureByFile(testDataPath)
    }

    @Test
    fun testDefineIndex_Basic() {
        configureDefineFile("features/index/common/defines/defines_basic_stellaris.test.txt")

        // act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val namespaces = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "NGameplay", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, namespaces.size)
        Assert.assertEquals("NGameplay", namespaces.single().name)

        val marineKey = PlsIndexUtil.createDefineVariableKey("NGameplay", "MARINE")
        val variables = StubIndex.getElements(PlsIndexKeys.DefineVariable, marineKey, project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, variables.size)
        Assert.assertEquals("MARINE", variables.single().name)

        val fleetPowerKey = PlsIndexUtil.createDefineVariableKey("NGameplay", "FLEET_POWER")
        val variables2 = StubIndex.getElements(PlsIndexKeys.DefineVariable, fleetPowerKey, project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, variables2.size)
        Assert.assertEquals("FLEET_POWER", variables2.single().name)
    }

    @Test
    fun testDefineIndex_Complex() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        // act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)

        // assert namespaces
        val namespaces1 = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "NGameplay", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, namespaces1.size)
        Assert.assertEquals("NGameplay", namespaces1.single().name)
        val namespaces2 = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "NEconomy", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, namespaces2.size)
        Assert.assertEquals("NEconomy", namespaces2.single().name)

        // assert variables
        val key1 = PlsIndexUtil.createDefineVariableKey("NGameplay", "MARINE")
        val key2 = PlsIndexUtil.createDefineVariableKey("NGameplay", "FLEET_POWER")
        val key3 = PlsIndexUtil.createDefineVariableKey("NEconomy", "ENERGY")
        val key4 = PlsIndexUtil.createDefineVariableKey("NEconomy", "MINERALS")
        val key5 = PlsIndexUtil.createDefineVariableKey("NEconomy", "MARINE")
        Assert.assertEquals(1, StubIndex.getElements(PlsIndexKeys.DefineVariable, key1, project, scope, ParadoxScriptProperty::class.java).size)
        Assert.assertEquals(1, StubIndex.getElements(PlsIndexKeys.DefineVariable, key2, project, scope, ParadoxScriptProperty::class.java).size)
        Assert.assertEquals(1, StubIndex.getElements(PlsIndexKeys.DefineVariable, key3, project, scope, ParadoxScriptProperty::class.java).size)
        Assert.assertEquals(1, StubIndex.getElements(PlsIndexKeys.DefineVariable, key4, project, scope, ParadoxScriptProperty::class.java).size)
        Assert.assertEquals(1, StubIndex.getElements(PlsIndexKeys.DefineVariable, key5, project, scope, ParadoxScriptProperty::class.java).size)

        // NOT_A_DEFINE should not be indexed as namespace or variable
        val namespaces3 = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "NOT_A_DEFINE", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertTrue(namespaces3.isEmpty())
        val variables3 = StubIndex.getElements(PlsIndexKeys.DefineVariable, PlsIndexUtil.createDefineVariableKey("NOT_A_DEFINE", "ANY"), project, scope, ParadoxScriptProperty::class.java)
        Assert.assertTrue(variables3.isEmpty())
    }

    @Test
    fun testDefineIndex_Edge() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)

        // valid namespace should be indexed
        val namespaces1 = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "NGameplay", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, namespaces1.size)

        // parameterized namespace should not be indexed
        val namespaces2 = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "\$NS$", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertTrue(namespaces2.isEmpty())
        val namespaces3 = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "N\$NS$", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertTrue(namespaces3.isEmpty())

        // top-level property not block should not be indexed as namespace
        val namespaces4 = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "N_NOT_BLOCK", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertTrue(namespaces4.isEmpty())

        // nested variable should not be indexed as define variable
        val nestedKey = PlsIndexUtil.createDefineVariableKey("NGameplay", "INSIDE")
        val nestedVariables = StubIndex.getElements(PlsIndexKeys.DefineVariable, nestedKey, project, scope, ParadoxScriptProperty::class.java)
        Assert.assertTrue(nestedVariables.isEmpty())
    }
}
