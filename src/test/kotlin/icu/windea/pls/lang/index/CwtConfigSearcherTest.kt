package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.lang.search.CwtConfigSearch
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
class CwtConfigSearcherTest : BasePlatformTestCase() {
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

    @Test
    fun testSearch_ById() {
        val typeConfig1 = CwtConfigSearch.searchById<CwtTypeConfig>("event", gameType, project).findAll().singleOrNull()!!
        Assert.assertTrue(typeConfig1.name == "event")

        val complexEnumConfig1 = CwtConfigSearch.searchById<CwtComplexEnumConfig>("component_tag", gameType, project).findAll().singleOrNull()!!
        Assert.assertTrue(complexEnumConfig1.name == "component_tag")

        val declarationConfig1 = CwtConfigSearch.searchById<CwtDeclarationConfig>("event", gameType, project).findAll().singleOrNull()!!
        Assert.assertTrue(declarationConfig1.name == "event")

        val aliasConfig1 = CwtConfigSearch.searchById<CwtAliasConfig>("trigger:if", gameType, project).findAll().singleOrNull()!!
        Assert.assertTrue(aliasConfig1.name == "trigger" && aliasConfig1.subName.equals("if", true))

        val aliasConfig2 = CwtConfigSearch.searchById<CwtAliasConfig>("trigger:IF", gameType, project).findAll().singleOrNull()!!
        Assert.assertTrue(aliasConfig2.name == "trigger" && aliasConfig2.subName.equals("if", true))

        val systemScopeConfig1 = CwtConfigSearch.searchById<CwtSystemScopeConfig>("this", gameType, project).findAll().singleOrNull()!!
        Assert.assertTrue(systemScopeConfig1.name.equals("this", true))

        val systemScopeConfig2 = CwtConfigSearch.searchById<CwtSystemScopeConfig>("THIS", gameType, project).findAll().singleOrNull()!!
        Assert.assertTrue(systemScopeConfig2.name.equals("this", true))

        val systemScopeConfig3 = CwtConfigSearch.searchById<CwtSystemScopeConfig>("This", gameType, project).findAll().singleOrNull()!!
        Assert.assertTrue(systemScopeConfig3.name.equals("this", true))
    }

    @Test
    fun testSearch_ByFilePath() {
        val typeConfig1 = CwtConfigSearch.searchByFilePath<CwtTypeConfig>("events/test.txt", gameType, project).findAll()
        assertContainsElements(typeConfig1.map { it.name }, "event_namespace", "event")

        val complexEnumConfig1 = CwtConfigSearch.searchByFilePath<CwtComplexEnumConfig>("common/component_tags/test.txt", gameType, project).findAll()
        assertContainsElements(complexEnumConfig1.map { it.name }, "component_tag")
    }
}
