package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.CwtDataTypeSets
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

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDynamicValueExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxDynamicValueExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val configs = configGroup.links.values.filter { it.configExpression?.type in CwtDataTypeSets.DynamicValue }
        if (configs.isEmpty()) error("No dynamic value configs found in links")
        if (incomplete) ChronicleThreadContext.incompleteComplexExpression.set(true) else ChronicleThreadContext.incompleteComplexExpression.remove()
        return ParadoxDynamicValueExpression.resolve(text, null, configGroup, configs)
    }

    @Test
    fun test_basic_withoutScopeSuffix() {
        val s = "some_variable"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDynamicValueExpression>(s, 0, s.length) {
            node<ParadoxDynamicValueNode>(s, 0, 13)
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_withScopeSuffix() {
        val s = "some_variable@root"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDynamicValueExpression>(s, 0, s.length) {
            node<ParadoxDynamicValueNode>("some_variable", 0, 13)
            node<ParadoxMarkerNode>("@", 13, 14)
            node<ParadoxScopeFieldExpression>("root", 14, 18) {
                node<ParadoxScopeNode>("root", 14, 18)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_withScopeSuffix_chained() {
        val s = "some_variable@root.owner"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDynamicValueExpression>(s, 0, s.length) {
            node<ParadoxDynamicValueNode>("some_variable", 0, 13)
            node<ParadoxMarkerNode>("@", 13, 14)
            node<ParadoxScopeFieldExpression>("root.owner", 14, 24) {
                node<ParadoxScopeNode>("root", 14, 18)
                node<ParadoxOperatorNode>(".", 18, 19)
                node<ParadoxScopeNode>("owner", 19, 24)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_incomplete_withFollowingAt() {
        val s = "some_variable@"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDynamicValueExpression>("some_variable@", 0, 14) {
            node<ParadoxDynamicValueNode>("some_variable", 0, 13)
            node<ParadoxMarkerNode>("@", 13, 14)
            node<ParadoxErrorTokenNode>("", 14, 14)
        }
        exp.check(dsl)
    }

    @Test
    fun test_incomplete_withFollowingDot() {
        val s = "some_variable@root."
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDynamicValueExpression>("some_variable@root.", 0, 19) {
            node<ParadoxDynamicValueNode>("some_variable", 0, 13)
            node<ParadoxMarkerNode>("@", 13, 14)
            node<ParadoxScopeFieldExpression>("root.", 14, 19) {
                node<ParadoxSystemScopeNode>("root", 14, 18)
                node<ParadoxOperatorNode>(".", 18, 19)
                node<ParadoxErrorScopeNode>("", 19, 19)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty() {
        Assert.assertNull(resolve("", ParadoxGameType.Stellaris, incomplete = false))
        val exp = resolve("", ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDynamicValueExpression>("", 0, 0) {
            node<ParadoxDynamicValueNode>("", 0, 0)
        }
        exp.check(dsl)
    }
}
