package icu.windea.pls.script

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.script.psi.ParadoxScriptFile
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptMoreSyntaxPsiTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testAdvancedNested() {
        myFixture.configureByFile("script/t_syntax_advanced_nested.txt")
        val file = myFixture.file as ParadoxScriptFile
        Assert.assertNotNull(file.block)
    }

    fun testBoundaryOnlyComments() {
        myFixture.configureByFile("script/t_syntax_only_comments.txt")
        val file = myFixture.file as ParadoxScriptFile
        Assert.assertNull(file.block)
    }

    fun testErrorUnclosedBrace() {
        myFixture.configureByFile("script/t_syntax_error_unclosed_brace.txt")
        val errors = PsiTreeUtil.findChildrenOfType(myFixture.file, PsiErrorElement::class.java)
        Assert.assertTrue(errors.isNotEmpty())
    }
}
