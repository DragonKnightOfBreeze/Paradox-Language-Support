package icu.windea.pls.lang.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.lang.search.CwtConfigSymbolSearch
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.CwtConfigSymbolIndexInfo
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtConfigSymbolSearcherTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markRootDirectory("features/index")
        markConfigDirectory("features/index/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun clear() = clearIntegrationTest()

    private fun configureConfigFile(testDataPath: String) {
        myFixture.configureByFile(testDataPath)
    }

    @Test
    fun testConfigSymbolSearcher_Declarations() {
        configureConfigFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        assertSymbol(scope, CwtConfigTypes.Type.id, "test_type", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "test_subtype", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "test_enum", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValueType.id, "test_value", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.SingleAlias.id, "test_single_alias", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "test_modifier", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Directive.id, "test_directive", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Link.id, "test_link", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.LocalisationLink.id, "test_loc_link", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
    }

    @Test
    fun testConfigSymbolSearcher_ReferenceExpressions() {
        configureConfigFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "ref_enum", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValue.id, "ref_value", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValue.id, "ref_value_set", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValue.id, "ref_dynamic", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "ref_alias", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "ref_alias_left", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "ref_alias_keys", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.SingleAlias.id, "ref_single_alias", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "ref_subtype", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
    }

    @Test
    fun testConfigSymbolSearcher_MultipleTypes() {
        configureConfigFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        val results = CwtConfigSymbolSearch.search(
            null,
            setOf(CwtConfigTypes.Type.id, CwtConfigTypes.Subtype.id),
            ParadoxGameType.Stellaris,
            project,
            scope
        ).findAll().toList()

        val names = results.map { it.name }.toSet()
        Assert.assertTrue(names.contains("test_type"))
        Assert.assertTrue(names.contains("test_subtype"))
    }

    @Test
    fun testConfigSymbolSearcher_ReadWriteAccess() {
        configureConfigFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        assertSymbol(scope, CwtConfigTypes.Type.id, "test_type", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
    }

    private fun assertSymbol(scope: GlobalSearchScope, type: String, name: String, access: ReadWriteAccessDetector.Access, gameType: ParadoxGameType) {
        val info = search(type, name, scope)
        Assert.assertEquals(access, info.readWriteAccess)
        Assert.assertEquals(gameType, info.gameType)
    }

    private fun search(type: String, name: String, scope: GlobalSearchScope): CwtConfigSymbolIndexInfo {
        val result = CwtConfigSymbolSearch.search(name, type, ParadoxGameType.Stellaris, project, scope).findFirst()
        Assert.assertNotNull("Expected symbol '$name' of type '$type'", result)
        return result!!
    }
}
