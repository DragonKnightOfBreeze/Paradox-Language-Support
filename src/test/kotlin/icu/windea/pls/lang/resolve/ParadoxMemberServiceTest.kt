package icu.windea.pls.lang.resolve

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.openapi.util.Ref
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.CharTable
import com.intellij.util.diff.FlyweightCapableTreeStructure
import icu.windea.pls.core.children
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.lang.index.stubs.ParadoxScriptStubDefinition
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.util.builders.ParadoxScriptTextBuilder.parameter as p

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxMemberServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    private fun configureScriptFile(text: String): ParadoxScriptFile {
        myFixture.configureByText("test.txt", text.trimIndent())
        return myFixture.file as ParadoxScriptFile
    }

    private fun buildLightTree(file: ParadoxScriptFile): LighterAST {
        val structure = ParadoxScriptStubDefinition().parseContentsLight(file.node)
        val charTable = object : CharTable {
            override fun intern(text: CharSequence): CharSequence = text

            override fun intern(text: CharSequence, start: Int, end: Int): CharSequence {
                return text.subSequence(start, end)
            }
        }
        return TestLighterAST(structure, charTable)
    }

    private class TestLighterAST(
        private val delegate: FlyweightCapableTreeStructure<LighterASTNode>,
        charTable: CharTable
    ) : LighterAST(charTable) {
        override fun getChildren(element: LighterASTNode): MutableList<LighterASTNode> {
            @Suppress("UNCHECKED_CAST")
            val ref = Ref<Array<LighterASTNode>>()
            val count = delegate.getChildren(element, ref)
            if (count <= 0) return mutableListOf()
            val array = ref.get() ?: return mutableListOf()
            return array.asList().subList(0, count).toMutableList()
        }

        override fun getParent(element: LighterASTNode): LighterASTNode? {
            return delegate.getParent(element)
        }

        override fun getRoot(): LighterASTNode {
            return delegate.root
        }
    }

    private fun findProperty(file: ParadoxScriptFile, name: String): ParadoxScriptProperty {
        return PsiTreeUtil.findChildrenOfType(file, ParadoxScriptProperty::class.java)
            .firstOrNull { it.name == name }
            ?: throw AssertionError("Property not found: $name")
    }

    private fun findBlockValue(file: ParadoxScriptFile, text: String): ParadoxScriptValue {
        return PsiTreeUtil.findChildrenOfType(file, ParadoxScriptValue::class.java)
            .firstOrNull { it.value == text && it.isBlockMember() }
            ?: throw AssertionError("Block member value not found: $text")
    }

    private fun findPropertyNode(tree: LighterAST, name: String): LighterASTNode {
        val root = tree.root
        val stack = ArrayDeque<LighterASTNode>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            if (node.tokenType == ParadoxScriptElementTypes.PROPERTY) {
                val nodeName = ParadoxScriptLightTreeUtil.getNameFromPropertyNode(node, tree)
                if (nodeName == name) return node
            }
            node.children(tree).forEach { stack.add(it) }
        }
        throw AssertionError("Property node not found: $name")
    }

    private fun findBlockMemberValueNode(tree: LighterAST, text: String): LighterASTNode {
        val root = tree.root
        val stack = ArrayDeque<LighterASTNode>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            val tokenType = node.tokenType
            if (ParadoxScriptTokenSets.VALUES.contains(tokenType) && tree.getParent(node)?.tokenType == ParadoxScriptElementTypes.BLOCK) {
                val value = when (tokenType) {
                    ParadoxScriptElementTypes.STRING -> ParadoxScriptLightTreeUtil.getValueFromStringNode(node, tree)
                    else -> node.toString()
                }
                if (value == text) return node
            }
            node.children(tree).forEach { stack.add(it) }
        }
        throw AssertionError("Block member value node not found: $text")
    }

    @Test
    fun psi_getPath_and_getRootKeys() {
        val file = configureScriptFile(
            """
            root = {
              k1 = {
                k2 = {
                  k3 = { k4 = v4 }
                  list = { "a" "b" }
                }
              }
            }
            """
        )

        val k4 = findProperty(file, "k4")
        val a = findBlockValue(file, "a")

        run {
            val path = ParadoxMemberService.getPath(k4)
            Assert.assertEquals("root/k1/k2/k3/k4", path!!.path)
        }
        run {
            val path = ParadoxMemberService.getPath(a)
            Assert.assertEquals("root/k1/k2/list/-", path!!.path)
        }
        run {
            val rootKeys = ParadoxMemberService.getRootKeys(k4)
            Assert.assertEquals(listOf("root", "k1", "k2", "k3"), rootKeys)
        }
        run {
            val rootKeys = ParadoxMemberService.getRootKeys(a)
            Assert.assertEquals(listOf("root", "k1", "k2", "list"), rootKeys)
        }
        run {
            val limited = ParadoxMemberService.getPath(k4, limit = 2)
            Assert.assertEquals(ParadoxMemberPath.resolve(listOf("k3", "k4")), limited)
        }
        run {
            val tooDeep = ParadoxMemberService.getPath(k4, maxDepth = 2)
            Assert.assertNull(tooDeep)
        }
    }

    @Test
    fun psi_getKeyPrefixes_and_getKeyPrefix() {
        val file = configureScriptFile(
            """
            root = {
              "p1" # comment
              "p2"
              target = 1
            }
            """
        )

        val target = findProperty(file, "target")

        run {
            val prefixes = ParadoxMemberService.getKeyPrefixes(target)
            Assert.assertEquals(listOf("p1", "p2"), prefixes)
        }
        run {
            val prefix = ParadoxMemberService.getKeyPrefix(target)
            Assert.assertEquals("p2", prefix)
        }
        run {
            val limited = ParadoxMemberService.getKeyPrefixes(target, limit = 1)
            Assert.assertEquals(listOf("p2"), limited)
        }
        run {
            val tooDeep = ParadoxMemberService.getKeyPrefixes(target, maxDepth = 1)
            Assert.assertNull(tooDeep)
        }
    }

    @Test
    fun lighterAst_getPath_and_getRootKeys() {
        val file = configureScriptFile(
            """
            root = {
              k1 = {
                k2 = {
                  k3 = { k4 = v4 }
                  list = { "a" "b" }
                }
              }
            }
            """
        )
        val virtualFile = file.virtualFile!!
        val tree = buildLightTree(file)

        val k4Node = findPropertyNode(tree, "k4")
        val aNode = findBlockMemberValueNode(tree, "a")

        run {
            val path = ParadoxMemberService.getPath(k4Node, tree, virtualFile)
            Assert.assertEquals("root/k1/k2/k3/k4", path!!.path)
        }
        run {
            val path = ParadoxMemberService.getPath(aNode, tree, virtualFile)
            Assert.assertEquals("root/k1/k2/list/-", path!!.path)
        }
        run {
            val rootKeys = ParadoxMemberService.getRootKeys(k4Node, tree, virtualFile)
            Assert.assertEquals(listOf("root", "k1", "k2", "k3"), rootKeys)
        }
        run {
            val rootKeys = ParadoxMemberService.getRootKeys(aNode, tree, virtualFile)
            Assert.assertEquals(listOf("root", "k1", "k2", "list"), rootKeys)
        }
        run {
            val limited = ParadoxMemberService.getPath(k4Node, tree, virtualFile, limit = 2)
            Assert.assertEquals(ParadoxMemberPath.resolve(listOf("k3", "k4")), limited)
        }
        run {
            val tooDeep = ParadoxMemberService.getPath(k4Node, tree, virtualFile, maxDepth = 2)
            Assert.assertNull(tooDeep)
        }
    }

    @Test
    fun lighterAst_getKeyPrefixes_and_getKeyPrefix() {
        val file = configureScriptFile(
            """
            root = {
              "p1" # comment
              "p2"
              target = 1
            }
            """
        )
        val tree = buildLightTree(file)

        val targetNode = findPropertyNode(tree, "target")

        run {
            val prefixes = ParadoxMemberService.getKeyPrefixes(targetNode, tree)
            Assert.assertEquals(listOf("p1", "p2"), prefixes)
        }
        run {
            val prefix = ParadoxMemberService.getKeyPrefix(targetNode, tree)
            Assert.assertEquals("p2", prefix)
        }
        run {
            val limited = ParadoxMemberService.getKeyPrefixes(targetNode, tree, limit = 1)
            Assert.assertEquals(listOf("p2"), limited)
        }
        run {
            val tooDeep = ParadoxMemberService.getKeyPrefixes(targetNode, tree, maxDepth = 1)
            Assert.assertNull(tooDeep)
        }
        run {
            val file2 = configureScriptFile(
                """
                root = {
                  "p${p("PARAM")}"
                  target = 1
                }
                """
            )
            val tree2 = buildLightTree(file2)
            val node2 = findPropertyNode(tree2, "target")
            val prefixes2 = ParadoxMemberService.getKeyPrefixes(node2, tree2)
            Assert.assertNull(prefixes2)
        }
    }

    @Test
    fun injectRootKeys_shouldAffectPathAndRootKeys() {
        val file = configureScriptFile("root = { k1 = { k2 = { k3 = { k4 = v4 } } } }")
        val virtualFile = file.virtualFile!!
        ParadoxAnalysisInjector.injectRootKeys(virtualFile, listOf("injected1", "injected2"))

        val k4 = findProperty(file, "k4")
        run {
            val path = ParadoxMemberService.getPath(k4)
            Assert.assertEquals("injected1/injected2/root/k1/k2/k3/k4", path!!.path)
        }
        run {
            val rootKeys = ParadoxMemberService.getRootKeys(k4)
            Assert.assertEquals(listOf("injected1", "injected2", "root", "k1", "k2", "k3"), rootKeys)
        }

        val tree = buildLightTree(file)
        val k4Node = findPropertyNode(tree, "k4")
        run {
            val path = ParadoxMemberService.getPath(k4Node, tree, virtualFile)
            Assert.assertEquals("injected1/injected2/root/k1/k2/k3/k4", path!!.path)
        }
        run {
            val rootKeys = ParadoxMemberService.getRootKeys(k4Node, tree, virtualFile)
            Assert.assertEquals(listOf("injected1", "injected2", "root", "k1", "k2", "k3"), rootKeys)
        }

        ParadoxAnalysisInjector.injectRootKeys(virtualFile, emptyList())
    }

    @Test
    fun injectRootKeys_shouldNotAffectLimitedPath() {
        val file = configureScriptFile("root = { k1 = { k2 = { k3 = { k4 = v4 } } } }")
        val virtualFile = file.virtualFile!!
        ParadoxAnalysisInjector.injectRootKeys(virtualFile, listOf("injected"))

        val k4 = findProperty(file, "k4")
        run {
            val limited = ParadoxMemberService.getPath(k4, limit = 2)
            Assert.assertEquals("k3/k4", limited!!.path)
        }

        val tree = buildLightTree(file)
        val k4Node = findPropertyNode(tree, "k4")
        run {
            val limited = ParadoxMemberService.getPath(k4Node, tree, virtualFile, limit = 2)
            Assert.assertEquals("k3/k4", limited!!.path)
        }

        ParadoxAnalysisInjector.injectRootKeys(virtualFile, emptyList())
    }

    @Test
    fun keyPrefixes_parameterized_shouldBehaveDifferentlyBetweenPsiAndLighterAst() {
        val file = configureScriptFile("root = { \"p${p("PARAM")}\" target = 1 }")

        val target = findProperty(file, "target")
        run {
            val prefixes = ParadoxMemberService.getKeyPrefixes(target)
            Assert.assertTrue(prefixes!!.isEmpty())
        }

        val tree = buildLightTree(file)
        val targetNode = findPropertyNode(tree, "target")
        run {
            val prefixes = ParadoxMemberService.getKeyPrefixes(targetNode, tree)
            Assert.assertNull(prefixes)
        }
    }

    @Test
    fun nonMemberElement_shouldReturnEmpty() {
        val file = configureScriptFile("root = { k1 = 1 }")

        run {
            val path = ParadoxMemberService.getPath(file)
            Assert.assertEquals("", path!!.path)
        }
        run {
            val rootKeys = ParadoxMemberService.getRootKeys(file)
            Assert.assertTrue(rootKeys!!.isEmpty())
        }
    }
}
