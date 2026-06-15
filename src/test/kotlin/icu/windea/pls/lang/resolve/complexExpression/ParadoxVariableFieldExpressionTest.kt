package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
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

    private fun resolve(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris, incomplete: Boolean = false): ParadoxVariableFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxVariableFieldExpression.resolve(text, null, configGroup)
    }

    @Test
    fun test_basic_chain() {
        val s = "root.owner.some_variable"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>(s, 0 to s.length) {
            node<ParadoxScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxScopeNode>("owner", 5 to 10)
            node<ParadoxOperatorNode>(".", 10 to 11)
            node<ParadoxDataSourceNode>("some_variable", 11 to 24)
        }
        exp.check(dsl)
    }

    @Test
    fun test_barrier_noFurtherSplit() {
        val s = "root.owner|x.y"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>(s, 0 to s.length) {
            node<ParadoxScopeNode>("root", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDataSourceNode>("owner|x.y", 5 to 14)
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty_incompleteDiff() {
        Assert.assertNull(resolve("", incomplete = false))
        val exp = resolve("", incomplete = true)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("", 0 to 0) {
            node<ParadoxDataSourceNode>("", 0 to 0)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_simple() {
        val s = "this.event_target:target"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target", 0 to 24) {
            node<ParadoxSystemScopeNode>("this", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDataSourceNode>("event_target:target", 5 to 24)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope() {
        val s = "this.event_target:target@root.var"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@root.var", 0 to 33) {
            node<ParadoxSystemScopeNode>("this", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5 to 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5 to 18)
                node<ParadoxScopeValueNode>("target@root", 18 to 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18 to 29) {
                        node<ParadoxDynamicValueNode>("target", 18 to 24)
                        node<ParadoxMarkerNode>("@", 24 to 25)
                        node<ParadoxScopeFieldExpression>("root", 25 to 29) {
                            node<ParadoxSystemScopeNode>("root", 25 to 29)
                        }
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 29 to 30)
            node<ParadoxDataSourceNode>("var", 30 to 33)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope_in_middle() {
        val s = "this.event_target:target@root.var"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@root.var", 0 to 33) {
            node<ParadoxSystemScopeNode>("this", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5 to 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5 to 18)
                node<ParadoxScopeValueNode>("target@root", 18 to 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18 to 29) {
                        node<ParadoxDynamicValueNode>("target", 18 to 24)
                        node<ParadoxMarkerNode>("@", 24 to 25)
                        node<ParadoxScopeFieldExpression>("root", 25 to 29) {
                            node<ParadoxSystemScopeNode>("root", 25 to 29)
                        }
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 29 to 30)
            node<ParadoxDataSourceNode>("var", 30 to 33)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withScope_inMiddle() {
        val s = "this.event_target:target@root.owner.var"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@root.owner.var", 0 to 39) {
            node<ParadoxSystemScopeNode>("this", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("event_target:target@root", 5 to 29) {
                node<ParadoxScopePrefixNode>("event_target:", 5 to 18)
                node<ParadoxScopeValueNode>("target@root", 18 to 29) {
                    node<ParadoxDynamicValueExpression>("target@root", 18 to 29) {
                        node<ParadoxDynamicValueNode>("target", 18 to 24)
                        node<ParadoxMarkerNode>("@", 24 to 25)
                        node<ParadoxScopeFieldExpression>("root", 25 to 29) {
                            node<ParadoxSystemScopeNode>("root", 25 to 29)
                        }
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 29 to 30)
            node<ParadoxStaticScopeNode>("owner", 30 to 35)
            node<ParadoxOperatorNode>(".", 35 to 36)
            node<ParadoxDataSourceNode>("var", 36 to 39)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withFollowingAt() {
        val s = "this.event_target:target@.var"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@.var", 0 to 29) {
            node<ParadoxSystemScopeNode>("this", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("event_target:target@", 5 to 25) {
                node<ParadoxScopePrefixNode>("event_target:", 5 to 18)
                node<ParadoxScopeValueNode>("target@", 18 to 25) {
                    node<ParadoxDynamicValueExpression>("target@", 18 to 25) {
                        node<ParadoxDynamicValueNode>("target", 18 to 24)
                        node<ParadoxMarkerNode>("@", 24 to 25)
                        node<ParadoxErrorTokenNode>("", 25 to 25)
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 25 to 26)
            node<ParadoxDataSourceNode>("var", 26 to 29)
        }
        exp.check(dsl)
    }

    @Test
    fun test_nestedDynamicValueExpression_withFollowingAt_inMiddle() {
        val s = "this.event_target:target@.owner.var"
        val exp = resolve(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxVariableFieldExpression>("this.event_target:target@.owner.var", 0 to 35) {
            node<ParadoxSystemScopeNode>("this", 0 to 4)
            node<ParadoxOperatorNode>(".", 4 to 5)
            node<ParadoxDynamicScopeNode>("event_target:target@", 5 to 25) {
                node<ParadoxScopePrefixNode>("event_target:", 5 to 18)
                node<ParadoxScopeValueNode>("target@", 18 to 25) {
                    node<ParadoxDynamicValueExpression>("target@", 18 to 25) {
                        node<ParadoxDynamicValueNode>("target", 18 to 24)
                        node<ParadoxMarkerNode>("@", 24 to 25)
                        node<ParadoxErrorTokenNode>("", 25 to 25)
                    }
                }
            }
            node<ParadoxOperatorNode>(".", 25 to 26)
            node<ParadoxStaticScopeNode>("owner", 26 to 31)
            node<ParadoxOperatorNode>(".", 31 to 32)
            node<ParadoxDataSourceNode>("var", 32 to 35)
        }
        exp.check(dsl)
    }
}
