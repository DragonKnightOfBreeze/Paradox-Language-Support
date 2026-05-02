package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.lang.search.CwtConfigSymbolSearch
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
import icu.windea.pls.model.ParadoxGameType

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtConfigSymbolSearcherTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/index")
        markConfigDirectory("features/index/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Declarations

    @Test
    fun testConfigSymbolSearcher_Declarations() {
        myFixture.configureByFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        assertSymbol(scope, CwtConfigTypes.Type.id, "test_type", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "test_subtype", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "test_enum", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValueType.id, "test_value", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.SingleAlias.id, "test_single_alias", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "test_modifier", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertNoSymbol(scope, CwtConfigTypes.Alias.id, "some_test_modifier")
        assertSymbol(scope, CwtConfigTypes.Alias.id, "modifier", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Modifier.id, "modifier_const", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "trigger", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Trigger.id, "trigger_const", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "effect", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Effect.id, "effect_const", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertNoSymbol(scope, CwtConfigTypes.Trigger.id, "enum[not_const]")
        assertSymbol(scope, CwtConfigTypes.Alias.id, "alias_no_colon", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Directive.id, "test_directive", ReadWriteAccess.Write, ParadoxGameType.Core)
    }

    // endregion

    // region Reference Expressions

    @Test
    fun testConfigSymbolSearcher_ReferenceExpressions() {
        myFixture.configureByFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "ref_enum", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValue.id, "ref_value", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValue.id, "ref_value_set", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValue.id, "ref_dynamic", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "ref_alias", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "ref_alias_left", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "ref_alias_keys", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.SingleAlias.id, "ref_single_alias", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "ref_subtype", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type2", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "sub1", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "sub2", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertNoSymbol(scope, CwtConfigTypes.Enum.id, "ignored_enum")
        assertNoSymbol(scope, CwtConfigTypes.Alias.id, "ignored_alias")

        // referenced in alias[*:*] tail (for alias config type)
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type_in_alias", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "ref_sub_in_alias", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "enum_in_alias", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValue.id, "dynamic_in_alias", ReadWriteAccess.Read, ParadoxGameType.Core)

        // quoted expression should still be indexed (quoteOffset)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "quoted_enum", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Type.id, "quoted_type", ReadWriteAccess.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "quoted_sub", ReadWriteAccess.Read, ParadoxGameType.Core)

        // not a full match
        assertNoSymbol(scope, CwtConfigTypes.Enum.id, "bad_enum")

        // should not index empty name
        assertNoEmptyName(scope)
    }

    // endregion

    // region Multiple Types

    @Test
    fun testConfigSymbolSearcher_MultipleTypes() {
        myFixture.configureByFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        val results = CwtConfigSymbolSearch.search(
            null,
            setOf(CwtConfigTypes.Type.id, CwtConfigTypes.Subtype.id),
            gameType,
            project,
            scope
        ).findAll().toList()

        val names = results.map { it.name }.toSet()
        Assert.assertTrue(names.contains("test_type"))
        Assert.assertTrue(names.contains("test_subtype"))
    }

    // endregion

    // region Read/Write Access

    @Test
    fun testConfigSymbolSearcher_ReadWriteAccess() {
        myFixture.configureByFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        assertSymbol(scope, CwtConfigTypes.Type.id, "test_type", ReadWriteAccess.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type", ReadWriteAccess.Read, ParadoxGameType.Core)
    }

    // endregion

    @Suppress("SameParameterValue")
    private fun assertSymbol(scope: GlobalSearchScope, type: String, name: String, access: ReadWriteAccess, gameType: ParadoxGameType) {
        val info = search(type, name, scope)
        Assert.assertEquals(access, info.readWriteAccess)
        Assert.assertEquals(gameType, info.gameType)
    }

    private fun search(type: String, name: String, scope: GlobalSearchScope): CwtConfigSymbolIndexInfo {
        val result = CwtConfigSymbolSearch.search(name, type, gameType, project, scope).findFirst()
        Assert.assertNotNull("Expected symbol '$name' of type '$type'", result)
        return result!!
    }

    private fun assertNoSymbol(scope: GlobalSearchScope, type: String, name: String) {
        val result = CwtConfigSymbolSearch.search(name, type, gameType, project, scope).findFirst()
        Assert.assertNull("Did not expect symbol '$name' of type '$type'", result)
    }

    private fun assertNoEmptyName(scope: GlobalSearchScope) {
        val types = listOf(
            CwtConfigTypes.Enum.id,
            CwtConfigTypes.DynamicValue.id,
            CwtConfigTypes.Alias.id,
            CwtConfigTypes.SingleAlias.id,
            CwtConfigTypes.Type.id,
            CwtConfigTypes.Subtype.id
        )
        types.forEach { type ->
            val result = CwtConfigSymbolSearch.search(null, type, gameType, project, scope).findAll().toList()
            Assert.assertTrue("Should not index empty name for type '$type'", result.none { it.name.isEmpty() })
        }
    }
}
