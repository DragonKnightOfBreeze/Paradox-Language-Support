package icu.windea.pls.lang.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.model.ParadoxGameType
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
class CwtConfigSymbolIndexTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markRootDirectory("features/index")
        markConfigDirectory("features/index/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun clear() = clearIntegrationTest()

    // region Declarations

    @Test
    fun testConfigSymbolIndex_Declarations() {
        myFixture.configureByFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        assertSymbol(scope, CwtConfigTypes.Type.id, "test_type", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "test_subtype", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "test_enum", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValueType.id, "test_value", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.SingleAlias.id, "test_single_alias", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "test_modifier", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertNoSymbol(scope, CwtConfigTypes.Alias.id, "some_test_modifier")
        assertSymbol(scope, CwtConfigTypes.Alias.id, "modifier", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Modifier.id, "modifier_const", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "trigger", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Trigger.id, "trigger_const", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Alias.id, "effect", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Effect.id, "effect_const", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertNoSymbol(scope, CwtConfigTypes.Trigger.id, "enum[not_const]")
        assertSymbol(scope, CwtConfigTypes.Alias.id, "alias_no_colon", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Directive.id, "test_directive", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
    }

    // endregion

    // region Reference Expressions

    @Test
    fun testConfigSymbolIndex_ReferenceExpressions() {
        myFixture.configureByFile("features/index/.config/core/config_symbols.test.cwt")

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
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type2", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "sub1", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "sub2", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertNoSymbol(scope, CwtConfigTypes.Enum.id, "ignored_enum")
        assertNoSymbol(scope, CwtConfigTypes.Alias.id, "ignored_alias")

        // referenced in alias[*:*] tail (for alias config type)
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type_in_alias", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "ref_sub_in_alias", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "enum_in_alias", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.DynamicValue.id, "dynamic_in_alias", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)

        // quoted expression should still be indexed (quoteOffset)
        assertSymbol(scope, CwtConfigTypes.Enum.id, "quoted_enum", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Type.id, "quoted_type", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Subtype.id, "quoted_sub", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)

        // not a full match
        assertNoSymbol(scope, CwtConfigTypes.Enum.id, "bad_enum")

        // should not index empty name
        assertNoEmptyName(scope)
    }

    // endregion

    // region Read/Write Access

    @Test
    fun testConfigSymbolIndex_ReadWriteAccess() {
        myFixture.configureByFile("features/index/.config/core/config_symbols.test.cwt")

        val scope = GlobalSearchScope.projectScope(project)
        assertSymbol(scope, CwtConfigTypes.Type.id, "test_type", ReadWriteAccessDetector.Access.Write, ParadoxGameType.Core)
        assertSymbol(scope, CwtConfigTypes.Type.id, "ref_type", ReadWriteAccessDetector.Access.Read, ParadoxGameType.Core)
    }

    // endregion

    @Suppress("SameParameterValue")
    private fun assertSymbol(scope: GlobalSearchScope, type: String, name: String, access: ReadWriteAccessDetector.Access, gameType: ParadoxGameType) {
        val infos = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ConfigSymbol, type, scope).flatten()
        val info = infos.find { it.name == name }
        Assert.assertNotNull("Expected symbol '$name' of type '$type'", info)
        Assert.assertEquals(access, info!!.readWriteAccess)
        Assert.assertEquals(gameType, info.gameType)
    }

    private fun assertNoSymbol(scope: GlobalSearchScope, type: String, name: String) {
        val infos = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ConfigSymbol, type, scope).flatten()
        Assert.assertNull("Did not expect symbol '$name' of type '$type'", infos.find { it.name == name })
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
            val infos = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ConfigSymbol, type, scope).flatten()
            Assert.assertTrue("Should not index empty name for type '$type'", infos.none { it.name.isEmpty() })
        }
    }
}
