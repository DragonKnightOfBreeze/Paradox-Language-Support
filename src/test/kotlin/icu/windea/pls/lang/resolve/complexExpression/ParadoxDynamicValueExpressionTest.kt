package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDynamicValueExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxDynamicValueExpression? {
        val group = initConfigGroup(gameType)
        val configs = group.links.values.filter { it.configExpression?.type in CwtDataTypeGroups.DynamicValue }
        if (configs.isEmpty()) error("No dynamic value configs found in links")
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxDynamicValueExpression.resolve(text, TextRange(0, text.length), group, configs)
    }

    fun testBasic_withoutScopeSuffix() {
        val s = "some_variable"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxDynamicValueExpression>(s, 0..s.length) {
            node<ParadoxDynamicValueNode>(s, 0..13)
        }
        exp.check(dsl)
    }

    fun testBasic_withScopeSuffix() {
        val s = "some_variable@root"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxDynamicValueExpression>(s, 0..s.length) {
            node<ParadoxDynamicValueNode>("some_variable", 0..13)
            node<ParadoxMarkerNode>("@", 13..14)
            node<ParadoxScopeFieldExpression>("root", 14..18) {
                node<ParadoxScopeLinkNode>("root", 14..18)
            }
        }
        exp.check(dsl)
    }

    fun testBasic_withScopeSuffix_chained() {
        val s = "some_variable@root.owner"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxDynamicValueExpression>(s, 0..s.length) {
            node<ParadoxDynamicValueNode>("some_variable", 0..13)
            node<ParadoxMarkerNode>("@", 13..14)
            node<ParadoxScopeFieldExpression>("root.owner", 14..24) {
                node<ParadoxScopeLinkNode>("root", 14..18)
                node<ParadoxOperatorNode>(".", 18..19)
                node<ParadoxScopeLinkNode>("owner", 19..24)
            }
        }
        exp.check(dsl)
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxDynamicValueExpression>("", 0..0) {
            node<ParadoxDynamicValueNode>("", 0..0)
        }
        exp.check(dsl)
    }
}
