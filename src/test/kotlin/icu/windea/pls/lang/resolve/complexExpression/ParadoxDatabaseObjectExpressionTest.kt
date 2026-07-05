package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxDatabaseObjectExpression
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDatabaseObjectExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxDatabaseObjectExpression? {
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        return mark(incomplete) { ParadoxDatabaseObjectExpression.resolve(text, null, configGroup) }
    }

    @Test
    fun test_basic_twoSegments() {
        val s = "civic:some_civic"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDatabaseObjectExpression>(s, 0, s.length) {
            node<ParadoxDatabaseObjectTypeNode>("civic", 0, 5)
            node<ParadoxMarkerNode>(":", 5, 6)
            node<ParadoxDatabaseObjectValueNode>("some_civic", 6, 16) {
                node<ParadoxDatabaseObjectNode>("some_civic", 6, 16)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_threeSegments() {
        val s = "civic:some_civic:some_swapped_civic"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDatabaseObjectExpression>(s, 0, s.length) {
            node<ParadoxDatabaseObjectTypeNode>("civic", 0, 5)
            node<ParadoxMarkerNode>(":", 5, 6)
            node<ParadoxDatabaseObjectValueNode>("some_civic", 6, 16) {
                node<ParadoxDatabaseObjectNode>("some_civic", 6, 16)
            }
            node<ParadoxMarkerNode>(":", 16, 17)
            node<ParadoxDatabaseObjectValueNode>("some_swapped_civic", 17, 35) {
                node<ParadoxDatabaseObjectNode>("some_swapped_civic", 17, 35)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_job() {
        val s = "job:job_soldier"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDatabaseObjectExpression>(s, 0, s.length) {
            node<ParadoxDatabaseObjectTypeNode>("job", 0, 3)
            node<ParadoxMarkerNode>(":", 3, 4)
            node<ParadoxDatabaseObjectValueNode>("job_soldier", 4, 15) {
                node<ParadoxDatabaseObjectNode>("job_soldier", 4, 15)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty() {
        Assert.assertNull(resolve("", ParadoxGameType.Stellaris, incomplete = false))
        val exp = resolve("", ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDatabaseObjectExpression>("", 0, 0) {
            node<ParadoxDatabaseObjectTypeNode>("", 0, 0)
        }
        exp.check(dsl)
    }
}
