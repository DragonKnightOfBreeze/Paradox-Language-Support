package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxTagsExpression
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxTagsExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxTagsExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val config = CwtValueConfig.createMock(configGroup, "value[tag]")
        return mark(incomplete) { ParadoxTagsExpression.resolve(text, null, configGroup, config) }
    }

    @Test
    fun test_basic() {
        val s = "tag1,tag2"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("tag1,tag2", 0, 9) {
            node<ParadoxDynamicValueNode>("tag1", 0, 4)
            node<ParadoxMarkerNode>(",", 0, 5)
            node<ParadoxDynamicValueNode>("tag2", 5, 9)
        }

        exp.check(dsl)
    }

    @Test
    fun test_single() {
        val s = "tag1"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("tag1", 0, 4) {
            node<ParadoxDynamicValueNode>("tag1", 0, 4)
        }
        exp.check(dsl)
    }

    @Test
    fun test_multiple() {
        val s = "tag1,tag2,tag3"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("tag1,tag2,tag3", 0, 14) {
            node<ParadoxDynamicValueNode>("tag1", 0, 4)
            node<ParadoxMarkerNode>(",", 0, 5)
            node<ParadoxDynamicValueNode>("tag2", 5, 9)
            node<ParadoxMarkerNode>(",", 5, 10)
            node<ParadoxDynamicValueNode>("tag3", 10, 14)
        }
        exp.check(dsl)
    }

    @Test
    fun test_inverted() {
        val s = "tag1,not(tag2)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("tag1,not(tag2)", 0, 14) {
            node<ParadoxDynamicValueNode>("tag1", 0, 4)
            node<ParadoxMarkerNode>(",", 0, 5)
            node<ParadoxNegatedDynamicValueNode>("not(tag2)", 5, 14) {
                node<ParadoxKeywordNode>("not", 5, 8)
                node<ParadoxMarkerNode>("(", 8, 9)
                node<ParadoxDynamicValueNode>("tag2", 9, 13)
                node<ParadoxMarkerNode>(")", 13, 14)
            }
        }

        exp.check(dsl)
    }

    @Test
    fun test_invertedAll() {
        val s = "not(tag1),not(tag2)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("not(tag1),not(tag2)", 0, 19) {
            node<ParadoxNegatedDynamicValueNode>("not(tag1)", 0, 9) {
                node<ParadoxKeywordNode>("not", 0, 3)
                node<ParadoxMarkerNode>("(", 3, 4)
                node<ParadoxDynamicValueNode>("tag1", 4, 8)
                node<ParadoxMarkerNode>(")", 8, 9)
            }
            node<ParadoxMarkerNode>(",", 0, 10)
            node<ParadoxNegatedDynamicValueNode>("not(tag2)", 10, 19) {
                node<ParadoxKeywordNode>("not", 10, 13)
                node<ParadoxMarkerNode>("(", 13, 14)
                node<ParadoxDynamicValueNode>("tag2", 14, 18)
                node<ParadoxMarkerNode>(")", 18, 19)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_inverted_withBlank() {
        val s = "tag1,not (   tag2 )"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("tag1,not (   tag2 )", 0, 19) {
            node<ParadoxDynamicValueNode>("tag1", 0, 4)
            node<ParadoxMarkerNode>(",", 0, 5)
            node<ParadoxNegatedDynamicValueNode>("not (   tag2 )", 5, 19) {
                node<ParadoxKeywordNode>("not", 5, 8)
                node<ParadoxBlankNode>(" ", 8, 9)
                node<ParadoxMarkerNode>("(", 9, 10)
                node<ParadoxBlankNode>("   ", 10, 13)
                node<ParadoxDynamicValueNode>("tag2", 13, 17)
                node<ParadoxBlankNode>(" ", 17, 18)
                node<ParadoxMarkerNode>(")", 18, 19)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_missingTag() {
        val s = "tag,,not(tag2)"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("tag,,not(tag2)", 0, 14) {
            node<ParadoxDynamicValueNode>("tag", 0, 3)
            node<ParadoxMarkerNode>(",", 0, 4)
            node<ParadoxMarkerNode>(",", 4, 5)
            node<ParadoxNegatedDynamicValueNode>("not(tag2)", 5, 14) {
                node<ParadoxKeywordNode>("not", 5, 8)
                node<ParadoxMarkerNode>("(", 8, 9)
                node<ParadoxDynamicValueNode>("tag2", 9, 13)
                node<ParadoxMarkerNode>(")", 13, 14)
            }
        }

        exp.check(dsl)
    }

    @Test
    fun test_onlyCommas() {
        val s = ", ,   , "
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>(", ,   , ", 0, 8) {
            node<ParadoxMarkerNode>(",", 0, 1)
            node<ParadoxBlankNode>(" ", 1, 2)
            node<ParadoxMarkerNode>(",", 2, 3)
            node<ParadoxBlankNode>("   ", 3, 6)
            node<ParadoxMarkerNode>(",", 6, 7)
            node<ParadoxBlankNode>(" ", 7, 8)
        }

        exp.check(dsl)
    }


    @Test
    fun test_withBlank() {
        val s = "tag1,   not( tag2  ),tag3"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("tag1,   not( tag2  ),tag3", 0, 25) {
            node<ParadoxDynamicValueNode>("tag1", 0, 4)
            node<ParadoxMarkerNode>(",", 0, 5)
            node<ParadoxBlankNode>("   ", 5, 8)
            node<ParadoxNegatedDynamicValueNode>("not( tag2  )", 8, 20) {
                node<ParadoxKeywordNode>("not", 8, 11)
                node<ParadoxMarkerNode>("(", 11, 12)
                node<ParadoxBlankNode>(" ", 12, 13)
                node<ParadoxDynamicValueNode>("tag2", 13, 17)
                node<ParadoxBlankNode>("  ", 17, 19)
                node<ParadoxMarkerNode>(")", 19, 20)
            }
            node<ParadoxMarkerNode>(",", 8, 21)
            node<ParadoxDynamicValueNode>("tag3", 21, 25)
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty() {
        Assert.assertNull(resolve("", ParadoxGameType.Stellaris, incomplete = false))
        val exp = resolve("", ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxTagsExpression>("", 0, 0) {
            node<ParadoxDynamicValueNode>("", 0, 0)
        }
        exp.check(dsl)
    }
}
