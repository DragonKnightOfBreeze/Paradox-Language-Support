package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxStringLiteralNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.lang.util.PlsCoreManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScopeFieldExpressionTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private fun initConfigGroup() = PlsFacade.getConfigGroupService().let { svc ->
        val groups = svc.getConfigGroups(project).values
        runBlocking { svc.init(groups, project) }
        // Use Vic3 because repository includes Vic3 CWT configs
        svc.getConfigGroup(project, ParadoxGameType.Vic3)
    }

    private fun parse(text: String): ParadoxScopeFieldExpression? {
        val group = initConfigGroup()
        // Allow incomplete expressions so resolver won't early-return null on first error node
        PlsCoreManager.incompleteComplexExpression.set(true)
        return try {
            ParadoxScopeFieldExpression.resolve(text, TextRange(0, text.length), group)
        } finally {
            PlsCoreManager.incompleteComplexExpression.remove()
        }
    }

    fun testSingleScopeNode_root() {
        val e = parse("root")
        Assert.assertNotNull(e)
        e!!
        Assert.assertTrue("No errors expected", e.errors.isEmpty())
        Assert.assertEquals(1, e.scopeNodes.size)
        Assert.assertEquals("root", e.scopeNodes.first().text)
    }

    fun testDotSegmentation_basic() {
        val e = parse("root.owner")!!
        val nodes = e.nodes
        // Expect: ScopeLinkNode("root"), OperatorNode("."), ScopeLinkNode("owner")
        Assert.assertEquals(3, nodes.size)
        Assert.assertTrue(nodes[0] is ParadoxScopeLinkNode)
        Assert.assertTrue(nodes[1] is ParadoxOperatorNode && (nodes[1] as ParadoxOperatorNode).text == ".")
        Assert.assertTrue(nodes[2] is ParadoxScopeLinkNode)
        Assert.assertEquals("owner", (nodes[2] as ParadoxScopeLinkNode).text)
    }

    fun testParentheses_NoSplitInside_ButSplitAfter() {
        val e = parse("relations(root.owner).owner")!!
        val nodes = e.nodes
        // Exactly one top-level dot split (after ')')
        val dotCount = nodes.filterIsInstance<ParadoxOperatorNode>().count { it.text == "." }
        Assert.assertEquals(1, dotCount)
        // First segment keeps inner dot within parentheses
        Assert.assertTrue(nodes.first() is ParadoxScopeLinkNode)
        Assert.assertTrue((nodes.first() as ParadoxScopeLinkNode).text.contains("(root.owner)"))
        // Last scope text should be 'owner'
        Assert.assertEquals("owner", e.scopeNodes.last().text)
    }

    fun testBarrier_At_NoFurtherSplit() {
        val e = parse("root.owner@x.y")!!
        val dotCount = e.nodes.filterIsInstance<ParadoxOperatorNode>().count { it.text == "." }
        // Only the first dot between root and owner should be split
        Assert.assertEquals(1, dotCount)
        val scopes = e.scopeNodes.map { it.text }
        Assert.assertEquals("root", scopes.first())
        Assert.assertTrue(scopes[1].startsWith("owner"))
        Assert.assertTrue(scopes[1].contains("@"))
        Assert.assertTrue(scopes[1].endsWith("x.y") || scopes[1].contains("@x.y"))
    }

    fun testBarrier_Pipe_NoFurtherSplit() {
        val e = parse("root.owner|x.y")!!
        val dotCount = e.nodes.filterIsInstance<ParadoxOperatorNode>().count { it.text == "." }
        // Only the first dot between root and owner should be split
        Assert.assertEquals(1, dotCount)
        val scopes = e.scopeNodes.map { it.text }
        Assert.assertEquals("root", scopes.first())
        Assert.assertTrue(scopes[1].startsWith("owner"))
        Assert.assertTrue(scopes[1].contains("|"))
        Assert.assertTrue(scopes[1].endsWith("x.y") || scopes[1].contains("|x.y"))
    }

    fun testUnclosedParentheses_NoSplitAfter() {
        val e = parse("relations(root.owner")!!
        // No top-level dot split should occur because of unclosed "("
        val dotCount = e.nodes.filterIsInstance<ParadoxOperatorNode>().count { it.text == "." }
        Assert.assertEquals(0, dotCount)
        // Only one top-level scope node should exist
        Assert.assertEquals(1, e.scopeNodes.size)
    }
}
