package icu.windea.pls.test.issues

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.inspections.script.expression.ConflictingExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.IncorrectExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.MissingExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.TooManyExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.configureByText
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#386](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/386)
 *
 * @see UnresolvedExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue386Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/386")
        markConfigDirectory("issues/386/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testInspection_AllResolved_Success() {
        enableAllNeededInspections()
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                members = {
                    key = value
                    value
                }
                properties = {
                    key = value
                }
                values = {
                    value
                }
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UnresolvedLeafNodes_NotMixed_Failed() {
        enableAllNeededInspections()
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                properties = {
                    ${error(forValue("unresolved", ""))}unresolved${errorEnd()}
                }
                values = {
                    ${error(forKey("unresolved", ""))}unresolved${errorEnd()} = unresolved_skipped
                }
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UnresolvedLeafNodes_Failed() {
        enableAllNeededInspections()
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                members = {
                    ${error(forKey("unresolved", "key"))}unresolved${errorEnd()} = unresolved_skipped
                    ${error(forValue("unresolved", "value"))}unresolved${errorEnd()}
                }
                properties = {
                    ${error(forKey("unresolved", "key"))}unresolved${errorEnd()} = unresolved_skipped
                    ${error(forValue("unresolved", ""))}unresolved${errorEnd()}
                }
                values = {
                    ${error(forKey("unresolved", ""))}unresolved${errorEnd()} = unresolved_skipped
                    ${error(forValue("unresolved", "value"))}unresolved${errorEnd()}
                }
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UnresolvedTopNodes_Failed() {
        enableAllNeededInspections()
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test = {
                ${error(forKey("unresolved_key", "members, properties, values"))}unresolved_key${errorEnd()} = value
                ${error(forKey("unresolved_key", "members, properties, values"))}unresolved_key${errorEnd()} = {}
                ${error(forKey("unresolved_key", "members, properties, values"))}unresolved_key${errorEnd()} = {
                    unresolved
                }

                ${error(forValue("unresolved_value", ""))}unresolved_value${errorEnd()}
                ${error(forValue("{...}", ""))}{}${errorEnd()}
                ${error(forValue("{...}", ""))}{
                    unresolved
                }${errorEnd()}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UnresolvedOuterNodes_Ignored() {
        enableAllNeededInspections()
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt") {
            """
            unresolved = unresolved # property value is not a block
            unresolved
            test = {
                members = {}
            }
            """.trimIndent()
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    private fun enableAllNeededInspections() {
        // enable all needed expression inspections
        myFixture.enableInspections(MissingExpressionInspection::class.java)
        myFixture.enableInspections(TooManyExpressionInspection::class.java)
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
        myFixture.enableInspections(ConflictingExpressionInspection::class.java)
        myFixture.enableInspections(IncorrectExpressionInspection::class.java)
    }

    private fun forKey(expression: String, expect: String): String {
        val expressionType = ChronicleBundle.message("expression.type.key")
        return when {
            expect.isEmpty() -> ChronicleInspectionBundle.message("inspection.unresolvedExpression.desc.1", expressionType, expression)
            else -> ChronicleInspectionBundle.message("inspection.unresolvedExpression.desc.2", expressionType, expression, expect)
        }
    }

    private fun forValue(expression: String, expect: String): String {
        val expressionType = ChronicleBundle.message("expression.type.value")
        return when {
            expect.isEmpty() -> ChronicleInspectionBundle.message("inspection.unresolvedExpression.desc.1", expressionType, expression)
            else -> ChronicleInspectionBundle.message("inspection.unresolvedExpression.desc.2", expressionType, expression, expect)
        }
    }
}
