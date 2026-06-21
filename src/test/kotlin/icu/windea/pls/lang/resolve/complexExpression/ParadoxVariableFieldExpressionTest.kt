package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.base.context.ChronicleThreadContext
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
class ParadoxVariableFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxVariableFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) ChronicleThreadContext.incompleteComplexExpression.set(true) else ChronicleThreadContext.incompleteComplexExpression.remove()
        return ParadoxVariableFieldExpression.resolve(text, null, configGroup)
    }

    @Test
    fun test_basic_chain() {
        val s = "root.owner.some_variable"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>(s, 0, s.length) {
            node<ParadoxScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxScopeNode>("owner", 5, 10)
            node<ParadoxOperatorNode>(".", 10, 11)
            node<ParadoxDataSourceNode>("some_variable", 11, 24)
        }
        exp.check(dsl)
    }

    @Test
    fun test_barrier_noFurtherSplit() {
        val s = "root.owner|x.y"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>(s, 0, s.length) {
            node<ParadoxScopeNode>("root", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDataSourceNode>("owner|x.y", 5, 14)
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty() {
        Assert.assertNull(resolve("", ParadoxGameType.Stellaris, incomplete = false))
        val exp = resolve("", ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("", 0, 0) {
            node<ParadoxDataSourceNode>("", 0, 0)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_simple() {
        val s = "this.event_target:target"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target", 0, 24) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDataSourceNode>("event_target:target", 5, 24)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope() {
        val s = "this.event_target:target@root.var"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@root.var", 0, 33) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5, 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@root", 18, 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18, 29) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxScopeFieldExpression>("root", 25, 29) {
                            node<ParadoxSystemScopeNode>("root", 25, 29)
                        }
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 29, 30)
            node<ParadoxDataSourceNode>("var", 30, 33)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope_in_middle() {
        val s = "this.event_target:target@root.var"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@root.var", 0, 33) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5, 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@root", 18, 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18, 29) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxScopeFieldExpression>("root", 25, 29) {
                            node<ParadoxSystemScopeNode>("root", 25, 29)
                        }
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 29, 30)
            node<ParadoxDataSourceNode>("var", 30, 33)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope_inMiddle() {
        val s = "this.event_target:target@root.owner.var"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@root.owner.var", 0, 39) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5, 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@root", 18, 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18, 29) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxScopeFieldExpression>("root", 25, 29) {
                            node<ParadoxSystemScopeNode>("root", 25, 29)
                        }
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 29, 30)
            node<ParadoxStaticScopeNode>("owner", 30, 35)
            node<ParadoxOperatorNode>(".", 35, 36)
            node<ParadoxDataSourceNode>("var", 36, 39)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withFollowingAt() {
        val s = "this.event_target:target@.var"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@.var", 0, 29) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@", 5, 25) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@", 18, 25) {
                    node<ParadoxDynamicValueExpression>("target@", 18, 25) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxErrorTokenNode>("", 25, 25)
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 25, 26)
            node<ParadoxDataSourceNode>("var", 26, 29)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withFollowingAt_inMiddle() {
        val s = "this.event_target:target@.owner.var"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@.owner.var", 0, 35) {
            node<ParadoxSystemScopeNode>("this", 0, 4)
            node<ParadoxOperatorNode>(".", 4, 5)
            node<ParadoxDynamicScopeNode>("event_target:target@", 5, 25) {
                node<ParadoxScopePrefixNode>("event_target:", 5, 18)
                node<ParadoxScopeValueNode>("target@", 18, 25) {
                    node<ParadoxDynamicValueExpression>("target@", 18, 25) {
                        node<ParadoxDynamicValueNode>("target", 18, 24)
                        node<ParadoxMarkerNode>("@", 24, 25)
                        node<ParadoxErrorTokenNode>("", 25, 25)
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 25, 26)
            node<ParadoxStaticScopeNode>("owner", 26, 31)
            node<ParadoxOperatorNode>(".", 31, 32)
            node<ParadoxDataSourceNode>("var", 32, 35)
        }
        exp.check(dsl)
    }
}
