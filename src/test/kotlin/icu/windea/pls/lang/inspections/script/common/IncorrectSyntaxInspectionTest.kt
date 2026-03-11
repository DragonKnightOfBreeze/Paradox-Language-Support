package icu.windea.pls.lang.inspections.script.common

import com.intellij.openapi.util.registry.Registry
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see IncorrectSyntaxInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectSyntaxInspectionTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections/script/incorrectSyntax")
        markConfigDirectory("features/inspections/script/incorrectSyntax/.config")
        initConfigGroups(project, gameType)
        myFixture.enableInspections(IncorrectSyntaxInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testComparisonOperator() {
        markFileInfo(gameType, "common/test/comparisonOperator.test.txt")
        myFixture.configureByFile("features/inspections/script/incorrectSyntax/common/test/comparison_operator.test.txt")
        myFixture.checkHighlighting(true, false, false)
    }

    @Test
    fun testComparisonOperator_checkComparisonOperators() {
        val registryValue = Registry.get("pls.settings.config.checkComparisonOperators")
        registryValue.setValue(true)
        try {
            markFileInfo(gameType, "common/test_entities/comparison_operator_advanced.test.txt")
            myFixture.configureByFile("features/inspections/script/incorrectSyntax/common/test_entities/comparison_operator_advanced.test.txt")
            myFixture.checkHighlighting(true, false, false)
        } finally {
            registryValue.resetToDefault()
        }
    }
}
