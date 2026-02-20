package icu.windea.pls.lang.resolve

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
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
